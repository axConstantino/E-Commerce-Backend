package com.axconstantino.auth.application.usecase;

import java.util.UUID;

public interface DeleteUser {
    void execute(UUID userID);
}
