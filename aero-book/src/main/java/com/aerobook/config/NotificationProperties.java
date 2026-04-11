package com.aerobook.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aerobook.notification")
public class NotificationProperties {
    private String  fromEmail;
    private String  fromName;
    private boolean kafkaEnabled;
}