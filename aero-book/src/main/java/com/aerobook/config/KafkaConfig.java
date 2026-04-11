package com.aerobook.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // Topic names — used by producer and consumer
    public static final String TOPIC_BOOKING_CONFIRMED  = "aerobook.booking.confirmed";
    public static final String TOPIC_PAYMENT_SUCCESS    = "aerobook.payment.success";
    public static final String TOPIC_PAYMENT_FAILED     = "aerobook.payment.failed";
    public static final String TOPIC_FLIGHT_DELAYED     = "aerobook.flight.delayed";
    public static final String TOPIC_FLIGHT_CANCELLED   = "aerobook.flight.cancelled";
    public static final String TOPIC_TIER_UPGRADED      = "aerobook.loyalty.tier.upgraded";

    // Dead letter topics
    public static final String TOPIC_DLQ_NOTIFICATIONS  = "aerobook.notifications.dlq";

    @Bean
    public NewTopic bookingConfirmedTopic() {
        return TopicBuilder.name(TOPIC_BOOKING_CONFIRMED)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentSuccessTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_SUCCESS)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_FAILED)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic flightDelayedTopic() {
        return TopicBuilder.name(TOPIC_FLIGHT_DELAYED)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic flightCancelledTopic() {
        return TopicBuilder.name(TOPIC_FLIGHT_CANCELLED)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic tierUpgradedTopic() {
        return TopicBuilder.name(TOPIC_TIER_UPGRADED)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationsDlqTopic() {
        return TopicBuilder.name(TOPIC_DLQ_NOTIFICATIONS)
                .partitions(1).replicas(1).build();
    }
}