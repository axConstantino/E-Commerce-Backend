package com.axconstantino.auth.application.usecase;

import com.axconstantino.auth.application.command.ChangePasswordCommand;

public interface ChangePassword {
    void execute(ChangePasswordCommand command);
}
