package org.shrtr.core.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.shrtr.core.domain.entities.Link;
import org.shrtr.core.domain.entities.LinkMetric;
import org.shrtr.core.domain.entities.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final Producer<String, String> kafkaProducer;
    private final Consumer<String, String> kafkaConsumer;
    private final Admin admin;
    private final ObjectMapper objectMapper;

    private static final List<String> entityTopics = List.of(User.class, Link.class, LinkMetric.class)
            .stream()
            .flatMap(clazz -> {
                return List.of("-created", "-updated", "-deleted")
                        .stream()
                        .map(tail -> clazz.getSimpleName() + tail);
            }).collect(Collectors.toList());
    private static final List<String> businessTopics = List.of("user-registered");
    @PostConstruct
    public void init() throws ExecutionException, InterruptedException {
        List<String> topics = new ArrayList<>();
        topics.addAll(entityTopics);
        topics.addAll(businessTopics);
        ListTopicsResult listTopicsResult = admin.listTopics();
        Set<String> strings = listTopicsResult.names().get();
        topics.forEach(topic -> {
            if (!strings.contains(topic)) {
                int partitions = 1;
                short replicationFactor = 1;
                CreateTopicsResult result = admin.createTopics(Collections.singleton(
                        new NewTopic(topic, partitions, replicationFactor)));
                KafkaFuture<Void> future = result.values().get(topic);
                try {
                    future.get();
                    log.info("Topic {} created", topic);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        kafkaConsumer.subscribe(topics);
        Executors.newSingleThreadScheduledExecutor()
                .execute(() -> {
                    while (true) {
                        ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));

                        for (ConsumerRecord<String, String> record : records) {
                            log.info("New Event! {} {}: {}", record.topic(), record.offset(), record.value());
                        }
                    }
                });


    }

    public void userCreated(User user) {
        try {
            ProducerRecord<String, String> event = new ProducerRecord<>(
                    "user-registered",
                    objectMapper.writeValueAsString(user) // parse json
            );
            kafkaProducer.send(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private int partitionOf(User user) {
        return 1;
    }

    public void entityEvent(Object entity, String topicTail) {
        try {
            ProducerRecord<String, String> event = new ProducerRecord<>(
                    entity.getClass().getSimpleName() + topicTail,
                    objectMapper.writeValueAsString(entity) // parse json
            );
            kafkaProducer.send(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void entityCreated(Object entity) {
        entityEvent(entity, "-created");
    }
    public void entityDeleted(Object entity) {
        entityEvent(entity, "-deleted");
    }

    public void entityUpdated(Object entity) {
        entityEvent(entity, "-updated");
    }
}
