package com.aerobook.event;


import com.aerobook.domain.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final Long             userId;
    private final String           recipientEmail;
    private final String           recipientPhone;
    private final NotificationType type;
    private final String           referenceId;
    private final Map<String, Object> templateVariables;

    @Builder
    public NotificationEvent(Object source,
                             Long userId,
                             String recipientEmail,
                             String recipientPhone,
                             NotificationType type,
                             String referenceId,
                             Map<String, Object> templateVariables) {
        super(source);
        this.userId            = userId;
        this.recipientEmail    = recipientEmail;
        this.recipientPhone    = recipientPhone;
        this.type              = type;
        this.referenceId       = referenceId;
        this.templateVariables = templateVariables;
    }
}