package com.axconstantino.auth.application.usecase;

import com.axconstantino.auth.application.command.ResetPasswordCommand;

public interface ResetPassword {
    void execute(ResetPasswordCommand command);
}
