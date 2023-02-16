package org.improving.workshop;

import org.improving.workshop.samples.PurchaseEventTicket;
import org.improving.workshop.samples.TopCustomerArtists;
import org.msse.demo.mockdata.customer.address.Address;
import org.msse.demo.mockdata.customer.email.Email;
import org.msse.demo.mockdata.customer.phone.Phone;
import org.msse.demo.mockdata.customer.profile.Customer;
import org.msse.demo.mockdata.music.artist.Artist;
import org.msse.demo.mockdata.music.event.Event;
import org.msse.demo.mockdata.music.stream.Stream;
import org.msse.demo.mockdata.music.ticket.Ticket;
import org.msse.demo.mockdata.music.venue.Venue;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(org.springframework.aot.hint.RuntimeHints hints, ClassLoader classLoader) {
        // Register method for reflection
        //Method method = ReflectionUtils.findMethod(Stream.class, "sayHello", String.class);
        //hints.reflection().registerMethod(method, ExecutableMode.INVOKE);

        // Register resources
        //hints.resources().registerPattern("my-resource.txt");

        // Register serialization
        hints.serialization().registerType(Stream.class);
        hints.serialization().registerType(Address.class);
        hints.serialization().registerType(Artist.class);
        hints.serialization().registerType(Customer.class);
        hints.serialization().registerType(Email.class);
        hints.serialization().registerType(Event.class);
        hints.serialization().registerType(Phone.class);
        hints.serialization().registerType(Stream.class);
        hints.serialization().registerType(Ticket.class);
        hints.serialization().registerType(Venue.class);

        hints.serialization().registerType(PurchaseEventTicket.EventTicket.class);
        hints.serialization().registerType(PurchaseEventTicket.EventStatus.class);
        hints.serialization().registerType(PurchaseEventTicket.EventTicketConfirmation.class);
        hints.serialization().registerType(TopCustomerArtists.SortedCounterMap.class);

        // Register proxy
        //hints.proxies().registerJdkProxy(MyInterface.class);
    }

}
