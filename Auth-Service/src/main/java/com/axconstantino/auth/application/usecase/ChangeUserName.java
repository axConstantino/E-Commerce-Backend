package com.axconstantino.auth.application.usecase;

import com.axconstantino.auth.application.command.ChangeUserNameCommand;

public interface ChangeUserName {

    void execute(ChangeUserNameCommand command);
}
