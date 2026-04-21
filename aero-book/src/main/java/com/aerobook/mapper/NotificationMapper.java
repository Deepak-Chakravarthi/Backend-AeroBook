package com.aerobook.mapper;



import com.aerobook.entity.Notification;
import com.aerobook.domain.dto.response.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Notification mapper.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    /**
     * To response notification response.
     *
     * @param notification the notification
     * @return the notification response
     */
    @Mapping(target = "userId", source = "user.id")
    NotificationResponse toResponse(Notification notification);
}