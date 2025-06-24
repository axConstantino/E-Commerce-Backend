package com.axconstantino.auth.web.dto;

import com.axconstantino.auth.application.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeEmailRequest {

    @NotBlank
    @ValidPassword
    private String currentPassword;

    @NotBlank
    @Email
    private String newEmail;
}
