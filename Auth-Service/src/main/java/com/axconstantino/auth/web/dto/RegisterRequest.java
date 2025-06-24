package com.axconstantino.auth.web.dto;

import com.axconstantino.auth.application.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 16, message = "Username must be between 2 and 16 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_.]+$",
            message = "Username can only contain letters, numbers, underscores (_) and periods (.)"
    )
    private String userName;


    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank
    @ValidPassword
    private String password;
}
