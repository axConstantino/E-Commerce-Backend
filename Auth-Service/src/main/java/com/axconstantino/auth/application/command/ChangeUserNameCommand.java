package com.axconstantino.auth.application.command;

import java.util.UUID;

public record ChangeUserNameCommand(UUID userId, String currentPassword, String newName) {}
