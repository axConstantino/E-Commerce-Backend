package com.axconstantino.auth.web.dto;

import com.axconstantino.auth.application.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeUserNameRequest {
    @NotBlank
    @ValidPassword
    private String currentPassword;

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 16, message = "Username must be between 2 and 16 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9_.]+$",
            message = "Username can only contain letters, numbers, underscores (_) and periods (.)"
    )
    private String newUsername;
}
