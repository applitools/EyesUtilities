package com.applitools.obj.serialized;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MergeBranchResponse {
    private boolean merged;
    private int conflicts;

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public int getConflicts() {
        return conflicts;
    }

    public void setConflicts(int conflicts) {
        this.conflicts = conflicts;
    }
}
