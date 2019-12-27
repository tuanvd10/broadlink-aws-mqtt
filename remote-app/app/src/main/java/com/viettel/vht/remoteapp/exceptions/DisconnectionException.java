package com.viettel.vht.remoteapp.exceptions;

import androidx.annotation.Nullable;

public class DisconnectionException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "Cannot connect to the aws!\n" + super.getMessage();
    }
}
