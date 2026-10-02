package com.applitools.eyesutilities.gui;

import java.util.List;
import java.util.Map;

/** Body of POST /api/run. `values` is keyed by CLI short flag (no dash). */
public final class UtilityRequest {
    public String command;
    public Map<String, Object> values;
    public List<String> urls;
    /** The view key for result commands, the API key for everything else. Never persisted. */
    public String key;
}
