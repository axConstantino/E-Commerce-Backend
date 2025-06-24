package com.axconstantino.auth.web.dto;

import com.axconstantino.auth.application.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateRequest {
    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank
    @ValidPassword
    private String password;
}
