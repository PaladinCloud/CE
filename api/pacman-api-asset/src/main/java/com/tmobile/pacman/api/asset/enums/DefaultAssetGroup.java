package com.tmobile.pacman.api.asset.enums;

import java.util.Arrays;
import java.util.Optional;

public enum DefaultAssetGroup {
    AWS("aws"), AZURE("azure"), GCP("gcp");
    private String name;

    DefaultAssetGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<DefaultAssetGroup> byNameIgnoreCase(String givenName) {
        return Arrays.stream(values()).filter(it -> it.name.equalsIgnoreCase(givenName)).findAny();
    }
}
