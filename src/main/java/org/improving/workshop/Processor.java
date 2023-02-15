package org.improving.workshop;

import org.apache.kafka.streams.StreamsBuilder;
import org.improving.workshop.samples.CustomerStreamCount;
import org.improving.workshop.samples.PurchaseEventTicket;
import org.improving.workshop.samples.TopCustomerArtists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Processor {

    @Autowired
    public void process(StreamsBuilder builder) {
        StreamTemplate.run(builder);
        //CustomerStreamCount.run(builder);
        //PurchaseEventTicket.run(builder);
        //TopCustomerArtists.run(builder);
    }
}
