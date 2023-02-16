package org.improving.workshop.samples;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.improving.workshop.KafkaConfig;
import org.msse.demo.mockdata.music.event.Event;
import org.msse.demo.mockdata.music.ticket.Ticket;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.io.Serializable;
import java.util.UUID;

import static org.apache.kafka.streams.state.Stores.persistentKeyValueStore;

@Slf4j
public class PurchaseEventTicket {
    // MUST BE PREFIXED WITH "kafka-workshop-"
    public static final String OUTPUT_TOPIC = "kafka-workshop-ticket-response";

    public static final JsonSerde<EventStatus> REMAINING_TICKETS_JSON_SERDE = new JsonSerde<>(EventStatus.class);
    public static final JsonSerde<PurchaseEventTicket.EventTicketConfirmation> TICKET_CONFIRMATION_JSON_SERDE = new JsonSerde<>(EventTicketConfirmation.class);

    public static void configureTopology(final StreamsBuilder builder) {
        // store events in a table so that the ticket can reference them to find capacity
        KTable<String, Event> eventsTable = builder
                .table(
                        KafkaConfig.TOPIC_DATA_DEMO_EVENTS,
                        Materialized
                            .<String, Event>as(persistentKeyValueStore("events"))
                            .withKeySerde(Serdes.String())
                            .withValueSerde(KafkaConfig.SERDE_EVENT_JSON)
                );

        // capture the backside of the table to log a confirmation that the Event was received
        eventsTable.toStream().peek((key, event) -> log.info("Event '{}' registered for artist '{}' at venue '{}' with a capacity of {}.", key, event.artistid(), event.venueid(), event.capacity()));

        builder
            .stream(KafkaConfig.TOPIC_DATA_DEMO_TICKETS, Consumed.with(Serdes.String(), KafkaConfig.SERDE_TICKET_JSON))
            .peek((ticketId, ticketRequest) -> log.info("Ticket Requested: {}", ticketRequest))

            // rekey by eventid so we can join against the event ktable
            .selectKey((ticketId, ticketRequest) -> ticketRequest.eventid(), Named.as("rekey-by-eventid"))

            // join the incoming ticket to the event that it is for
            .join(
                    eventsTable,
                    (eventId, ticket, event) -> new EventTicket(ticket, event)
            )
            .groupByKey()
            .aggregate(
                    // initializer (doesn't have key, value supplied so the actual initialization is in the aggregator)
                    EventStatus::new,

                    // aggregator
                    (eventId, ticketRequest, eventStatus) -> {
                        // initialize (if necessary)
                        if (!eventStatus.initialized) {
                            eventStatus.initialize(ticketRequest.event);
                        }

                        // decrement remaining tickets by 1 (and increment totalRequested)
                        eventStatus.decrementRemainingTickets();

                        // set the requested ticket so that it's available downstream
                        eventStatus.setCurrentTicketRequest(ticketRequest.ticket);

                        return eventStatus;
                    },

                    // ktable (materialized) configuration
                    Materialized
                            .<String, EventStatus>as(persistentKeyValueStore("event-status-table"))
                            .withKeySerde(Serdes.String())
                            .withValueSerde(REMAINING_TICKETS_JSON_SERDE)
            )

            .toStream()
            .split()
            // sold out branch
            .branch(
                    (eventId, eventStatus) -> !eventStatus.hasRemainingTickets(),
                    Branched.withConsumer(ks -> ks
                            .peek((eventId, eventStatus) -> log.info("Ticket Rejected. '{}' has SOLD OUT!", eventId))
                            .mapValues((eventStatus) ->
                                    EventTicketConfirmation
                                            .builder()
                                            .confirmationId(UUID.randomUUID().toString())
                                            .confirmationStatus("REJECTED")
                                            .event(eventStatus.event)
                                            .ticketRequest(eventStatus.currentTicketRequest)
                                            .remainingTickets(eventStatus.remaining)
                                            .build()
                            )
                            .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), TICKET_CONFIRMATION_JSON_SERDE))
                    )
            )
            // confirmed, but limited availability
            .branch(
                    (eventId, remainingTickets) -> remainingTickets.remainingPercentage() <= 20.0,
                    Branched.withConsumer(ks -> ks
                            .peek((eventId, remainingTickets) -> {
                                if (remainingTickets.remainingPercentage() == 0.0) {
                                    log.info("Ticket Confirmed. '{}' event is now sold out.", eventId);
                                } else {
                                    log.info("Ticket Confirmed. '{}' is almost sold out, {} ticket(s) remain.", eventId, remainingTickets.remaining);
                                }
                            })
                            .mapValues((eventStatus) ->
                                    EventTicketConfirmation
                                            .builder()
                                            .confirmationId(UUID.randomUUID().toString())
                                            .confirmationStatus("CONFIRMED")
                                            .event(eventStatus.event)
                                            .ticketRequest(eventStatus.currentTicketRequest)
                                            .remainingTickets(eventStatus.remaining)
                                            .build()
                            )
                            .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), TICKET_CONFIRMATION_JSON_SERDE))
                    )
            )
            // confirmed
            .branch(
                    (eventId, remainingTickets) -> remainingTickets.remainingPercentage() > 20,
                    Branched.withConsumer(ks -> ks
                            .peek((eventId, eventStatus) -> log.info("Ticket Confirmed. {} tickets remain for '{}'", eventStatus.remaining, eventId))
                            .mapValues((eventStatus) ->
                                    EventTicketConfirmation
                                            .builder()
                                            .confirmationId(UUID.randomUUID().toString())
                                            .confirmationStatus("CONFIRMED")
                                            .event(eventStatus.event)
                                            .ticketRequest(eventStatus.currentTicketRequest)
                                            .remainingTickets(eventStatus.remaining)
                                            .build()
                            )
                            .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), TICKET_CONFIRMATION_JSON_SERDE))
                    )
            )
            .noDefaultBranch();
    }

    @Data
    @AllArgsConstructor
    public static class EventTicket implements Serializable {
        private Ticket ticket;
        private Event event;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventTicketConfirmation implements Serializable {
        private String confirmationStatus;
        private String confirmationId;
        private Ticket ticketRequest;
        private Event event;
        private double remainingTickets;
    }

    @Data
    @AllArgsConstructor
    public static class EventStatus implements Serializable {
        private boolean initialized;
        private Event event;
        private double totalRequested;
        private double remaining;

        // stores only the most recently requested ticket
        private Ticket currentTicketRequest;

        public EventStatus() {
            initialized = false;
        }

        public void initialize(Event event) {
            this.event = event;
            this.remaining = event.capacity();
            this.initialized = true;
        }

        public void decrementRemainingTickets() {
            this.totalRequested++;
            this.remaining = this.remaining - 1;
        }

        public boolean hasRemainingTickets() {
            return remaining >= 0;
        }

        public double remainingPercentage() {
            if (remaining < 0) {
                return 0;
            }

            return (remaining / event.capacity()) * 100;
        }
    }
}