package com.megadevs.adoma.events;

import com.megadevs.adoma.AdomaKey;

public class ErrorEvent extends BaseAdomaKeyEvent {

    private String message;

    public ErrorEvent(AdomaKey adomaKey, String message) {
        super(adomaKey);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
