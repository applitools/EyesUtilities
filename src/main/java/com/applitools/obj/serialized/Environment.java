package com.applitools.obj.serialized;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Environment {
    private String inferred;
    private String os;
    private String hostingApp;
    private Map<String, String> displaySize;
    private String deviceInfo;

    //private class
    public String getInferred() {
        return inferred;
    }

    public void setInferred(String inferred) {
        this.inferred = inferred;
    }

    public String getDeviceInfo() { return deviceInfo; }

    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public String getOs() { return os; }

    public void setOs(String os) {
        this.os = os;
    }

    public String getHostingApp() {
        return hostingApp;
    }

    public void setHostingApp(String hostingApp) {
        this.hostingApp = hostingApp;
    }

    public Map<String, String> getDisplaySize() {
        return displaySize;
    }

    public String getDisplaySizeStr() {
        return String.format("%sx%s", displaySize.get("width"), displaySize.get("height"));
    }

    public void setDisplaySize(Map<String, String> displaySize) {
        this.displaySize = displaySize;
    }


}
