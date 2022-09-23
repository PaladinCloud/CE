
package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.List;
import java.util.Map;

/**
 * The Class KMSKeyVH.
 */
public class KMSKeyVH extends GCPVH{

    private String keyRingName;
    private String name;
    private String cryptoBackend;
    private String purpose;
    private boolean importOnly;
    private int labelsCount;
    private Map<String,String> labels;
    private List<Bindings> bindings;

    private long rotationPeriod;

    public String getKeyRingName() {
        return keyRingName;
    }

    public void setKeyRingName(String keyRingName) {
        this.keyRingName = keyRingName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCryptoBackend() {
        return cryptoBackend;
    }

    public void setCryptoBackend(String cryptoBackend) {
        this.cryptoBackend = cryptoBackend;
    }

    public boolean isImportOnly() {
        return importOnly;
    }

    public void setImportOnly(boolean importOnly) {
        this.importOnly = importOnly;
    }

    public int getLabelsCount() {
        return labelsCount;
    }

    public void setLabelsCount(int labelsCount) {
        this.labelsCount = labelsCount;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public List<Bindings> getBindings() {
        return bindings;
    }

    public void setBindings(List<Bindings> bindings) {
        this.bindings = bindings;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public long getRotationPeriod() {
        return rotationPeriod;
    }

    public void setRotationPeriod(long rotationPeriod) {
        this.rotationPeriod = rotationPeriod;
    }

    @Override
    public String toString() {
        return "KMSKeyVH{" +
                "keyRingName='" + keyRingName + '\'' +
                ", name='" + name + '\'' +
                ", cryptoBackend='" + cryptoBackend + '\'' +
                ", importOnly=" + importOnly +
                ", labelsCount=" + labelsCount +
                ", labelsMap=" + labels +
                ", bindingsList=" + bindings +
                '}';
    }
}