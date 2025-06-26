package com.axconstantino.auth.domain.event;

import java.util.UUID;

public record UserDeletedEvent(UUID userId) {
}
