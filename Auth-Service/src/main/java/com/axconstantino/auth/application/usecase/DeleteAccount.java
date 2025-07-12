package com.axconstantino.auth.application.usecase;

import java.util.UUID;

public interface DeleteAccount {
    void execute(UUID userID);
}
