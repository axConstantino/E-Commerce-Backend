package com.axconstantino.auth.web.dto;

import com.axconstantino.auth.application.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 6, message = "Code must be exactly 6 digits")
    @Pattern(regexp = "\\d{6}", message = "Code must contain only digits")
    private String code;

    @NotBlank
    @ValidPassword
    private String newPassword;
}
