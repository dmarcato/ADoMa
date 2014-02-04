package com.megadevs.adoma.events;

import com.megadevs.adoma.AdomaKey;

public class BaseAdomaKeyEvent {

    private AdomaKey adomaKey;

    public BaseAdomaKeyEvent(AdomaKey adomaKey) {
        this.adomaKey = adomaKey;
    }

    public AdomaKey getAdomaKey() {
        return adomaKey;
    }
}
