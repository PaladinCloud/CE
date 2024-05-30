package com.tmobile.pacman.commons.constants;

public enum PluginDisplayName {
    RAPID7("Rapid7"),
    CROWDSTRIKE("CrowdStrike"),
    AWS("AWS"),
    AZURE("Azure"),
    GCP("GCP"),
    AQUA("Aqua"),
    TENABLE("Tenable"),
    CHECKMARX("Checkmarx"),
    CONTRAST("Contrast"),
    REDHAT("Red Hat"),
    WIZ("WIZ"),
    BURPSUITE("Burp Suite"),
    QUALYS("Qualys");


    private final String displayName;

    PluginDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String getDisplayNameByString(String name) {
        for (PluginDisplayName vendor : PluginDisplayName.values()) {
            if (vendor.name().equalsIgnoreCase(name)) {
                return vendor.getDisplayName();
            }
        }
        throw new IllegalArgumentException("No enum constant for name: " + name);
    }
}
