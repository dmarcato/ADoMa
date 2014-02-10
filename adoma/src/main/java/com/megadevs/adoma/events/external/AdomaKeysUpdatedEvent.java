package com.megadevs.adoma.events.external;

import com.megadevs.adoma.AdomaKey;

import java.util.List;

public class AdomaKeysUpdatedEvent {

    private final List<AdomaKey> keys;

    public AdomaKeysUpdatedEvent(List<AdomaKey> keys) {
        this.keys = keys;
    }

    public List<AdomaKey> getKeys() {
        return keys;
    }
}
