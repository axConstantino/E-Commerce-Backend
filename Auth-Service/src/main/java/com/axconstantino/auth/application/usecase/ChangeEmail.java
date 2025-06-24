package com.axconstantino.auth.application.usecase;

import com.axconstantino.auth.application.command.ChangeEmailCommand;

public interface ChangeEmail {

    void execute(ChangeEmailCommand command);
}
