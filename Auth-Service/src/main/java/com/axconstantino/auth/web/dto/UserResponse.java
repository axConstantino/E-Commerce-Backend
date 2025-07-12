package com.axconstantino.auth.web.dto;

import com.axconstantino.auth.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String userName;
    private String email;
    private Set<Role> roles;
    private boolean active;
    private boolean emailVerified;
}
