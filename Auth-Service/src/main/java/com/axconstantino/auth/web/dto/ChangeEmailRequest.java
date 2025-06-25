package com.axconstantino.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeEmailRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Email
    private String newEmail;
}
