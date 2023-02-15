package org.improving.workshop;


import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.kafka.support.serializer.JsonSerde;

public class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register serialization
        //hints.serialization().registerType(new TypeReference<JsonSerde>() {});
    }

}
