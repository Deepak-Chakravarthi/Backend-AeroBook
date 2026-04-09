package com.aerobook.domain.dto.request.get;


import com.aerobook.entity.User;
import com.aerobook.domain.enums.UserStatus;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import static com.aerobook.domain.enums.RoleType.resolveRoleType;

@Getter
@Builder
public class UserGetRequest {

    private final Long   id;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String status;
    private final String role;

    public Specification<User> toSpecification() {
        return SpecificationBuilder.<User>builder()
                .addEquals("id", id)
                .addEquals("username", username)
                .addEquals("email", email)
                .addLike("firstName", firstName)
                .addLike("lastName", lastName)
                .addEnumEquals("status", status, UserStatus.class)
                .addJoinEquals("roles", "name", role != null
                        ? resolveRoleType(role) : null)
                .build();
    }


}