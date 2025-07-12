package com.axconstantino.auth.web.dto;

import com.axconstantino.auth.domain.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotNull(message = "Role cannot be null")
    private Role role;
}