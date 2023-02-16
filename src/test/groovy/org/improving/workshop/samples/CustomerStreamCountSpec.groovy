package org.improving.workshop.samples


import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.improving.workshop.KafkaConfig
import org.msse.demo.mockdata.music.stream.Stream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.config.KafkaStreamsConfiguration
import spock.lang.Specification

import static org.improving.workshop.utils.DataFaker.STREAMS

@SpringBootTest
class CustomerStreamCountSpec extends Specification {
    @Autowired
    KafkaStreamsConfiguration kafkaStreamsConfiguration

    TopologyTestDriver driver

    // inputs
    TestInputTopic<String, Stream> inputTopic

    // outputs
    TestOutputTopic<String, Long> outputTopic

    def 'setup'() {
        // instantiate new builder
        StreamsBuilder streamsBuilder = new StreamsBuilder()

        // build the CustomerStreamCount topology (by reference)
        CustomerStreamCount.configureTopology(streamsBuilder)

        // build the TopologyTestDriver
        driver = new TopologyTestDriver(streamsBuilder.build(), kafkaStreamsConfiguration.asProperties());

        inputTopic = driver.createInputTopic(
                KafkaConfig.TOPIC_DATA_DEMO_STREAMS,
                Serdes.String().serializer(),
                KafkaConfig.SERDE_STREAM_JSON.serializer()
        )

        outputTopic = driver.createOutputTopic(
                CustomerStreamCount.OUTPUT_TOPIC,
                Serdes.String().deserializer(),
                Serdes.Long().deserializer()
        )

    }

    def 'cleanup'() {
        driver.close()
    }

    def "customer stream counter"() {
        given: 'multiple customer streams received by the topology'
        inputTopic.pipeInput(UUID.randomUUID().toString(), STREAMS.generate("1", "2"))
        inputTopic.pipeInput(UUID.randomUUID().toString(), STREAMS.generate("1", "3"))
        inputTopic.pipeInput(UUID.randomUUID().toString(), STREAMS.generate("1", "4"))
        inputTopic.pipeInput(UUID.randomUUID().toString(), STREAMS.generate("2", "3"))

        when: 'reading the output records'
        def outputRecords = outputTopic.readRecordsToList()

        then: '4 records were received'
        outputRecords.size() == 4

        // customer 1, stream 1
        outputRecords[0].key() == "1"
        outputRecords[0].value() == 1L

        // customer 1, stream 2
        outputRecords[1].key() == "1"
        outputRecords[1].value() == 2L

        // customer 1, stream 3
        outputRecords[2].key() == "1"
        outputRecords[2].value() == 3L

        // customer 2, stream 1
        outputRecords[3].key() == "2"
        outputRecords[3].value() == 1L
    }
}
