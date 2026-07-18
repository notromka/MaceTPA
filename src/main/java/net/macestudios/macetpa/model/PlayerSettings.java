package net.macestudios.macetpa.model;

public final class PlayerSettings {
    private boolean tpaEnabled;
    private boolean tpahereEnabled;
    private boolean tpaautoEnabled;
    private boolean confirmRequest;

    public PlayerSettings(boolean tpaEnabled, boolean tpahereEnabled, boolean tpaautoEnabled, boolean confirmRequest) {
        this.tpaEnabled = tpaEnabled;
        this.tpahereEnabled = tpahereEnabled;
        this.tpaautoEnabled = tpaautoEnabled;
        this.confirmRequest = confirmRequest;
    }

    public boolean isTpaEnabled() {
        return tpaEnabled;
    }

    public void setTpaEnabled(boolean tpaEnabled) {
        this.tpaEnabled = tpaEnabled;
    }

    public boolean isTpahereEnabled() {
        return tpahereEnabled;
    }

    public void setTpahereEnabled(boolean tpahereEnabled) {
        this.tpahereEnabled = tpahereEnabled;
    }

    public boolean isTpaautoEnabled() {
        return tpaautoEnabled;
    }

    public void setTpaautoEnabled(boolean tpaautoEnabled) {
        this.tpaautoEnabled = tpaautoEnabled;
    }

    public boolean isConfirmRequest() {
        return confirmRequest;
    }

    public void setConfirmRequest(boolean confirmRequest) {
        this.confirmRequest = confirmRequest;
    }
}
