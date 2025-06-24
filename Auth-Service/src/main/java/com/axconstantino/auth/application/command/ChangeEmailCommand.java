package com.axconstantino.auth.application.command;

import java.util.UUID;

public record ChangeEmailCommand(UUID userId, String currentPassword, String newEmail) {

}
