package org.improving.workshop;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.msse.demo.mockdata.customer.address.Address;
import org.msse.demo.mockdata.customer.email.Email;
import org.msse.demo.mockdata.customer.phone.Phone;
import org.msse.demo.mockdata.customer.profile.Customer;
import org.msse.demo.mockdata.music.artist.Artist;
import org.msse.demo.mockdata.music.event.Event;
import org.msse.demo.mockdata.music.stream.Stream;
import org.msse.demo.mockdata.music.ticket.Ticket;
import org.msse.demo.mockdata.music.venue.Venue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
@EnableKafka
@EnableKafkaStreams
@ImportRuntimeHints(RuntimeHints.class)
public class KafkaConfig {

    // addresses
    public static final String TOPIC_DATA_DEMO_ADDRESSES = "data-demo-addresses";
    public static final JsonSerde<Address> SERDE_ADDRESS_JSON = new JsonSerde<>(Address.class);
    // artists
    public static final String TOPIC_DATA_DEMO_ARTISTS = "data-demo-artists";
    public static final JsonSerde<Artist> SERDE_ARTIST_JSON = new JsonSerde<>(Artist.class);
    // customers
    public static final String TOPIC_DATA_DEMO_CUSTOMERS = "data-demo-customers";
    public static final JsonSerde<Customer> SERDE_CUSTOMER_JSON = new JsonSerde<>(Customer.class);
    // emails
    public static final String TOPIC_DATA_DEMO_EMAILS = "data-demo-emails";
    public static final JsonSerde<Email> SERDE_EMAIL_JSON = new JsonSerde<>(Email.class);
    // events
    public static final String TOPIC_DATA_DEMO_EVENTS = "data-demo-events";
    public static final JsonSerde<Event> SERDE_EVENT_JSON = new JsonSerde<>(Event.class);
    // phones
    public static final String TOPIC_DATA_DEMO_PHONES = "data-demo-phones";
    public static final JsonSerde<Phone> SERDE_PHONE_JSON = new JsonSerde<>(Phone.class);
    // streams
    public static final String TOPIC_DATA_DEMO_STREAMS = "data-demo-streams";
    public static final JsonSerde<Stream> SERDE_STREAM_JSON = new JsonSerde<>(Stream.class);
    // tickets
    public static final String TOPIC_DATA_DEMO_TICKETS = "data-demo-tickets";
    public static final JsonSerde<Ticket> SERDE_TICKET_JSON = new JsonSerde<>(Ticket.class);
    // venues
    public static final String TOPIC_DATA_DEMO_VENUES = "data-demo-venues";
    public static final JsonSerde<Venue> SERDE_VENUE_JSON = new JsonSerde<>(Venue.class);

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${spring.kafka.streams.application-id}")
    private String applicationId;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        // Give the Streams application a unique name, so it reprocess all the message each startup.
        props.put(APPLICATION_ID_CONFIG, applicationId + "-" + UUID.randomUUID());
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // Records should be flushed every 10 seconds. This is less than the default
        // in order to keep this example interactive.
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        // For illustrative purposes we disable record caches.
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        return new KafkaStreamsConfiguration(props);
    }
}
