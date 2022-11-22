package com.applitools.eyesutilities.obj.serialized;

import java.util.HashMap;

/**
 * Created by yanir on 10/04/2017.
 */
public class ActualStepResult {
    private HashMap Image;
    private HashMap Thumbprint;
    private Object ImageMatchSettings;
    private Boolean IgnoreExpectedOutputSettings;
    private Boolean IsMatching;
    private Boolean AreImagesMatching;
    private HashMap AccessibilityStatus;
    private String OccurredAt;
    private Object UserInputs;
    private String Tag;
    private String windowTitle;
    private Boolean IsPrimary;
    private String ExpectedImageId;
    private String expectedThumbprintId;
    private Boolean wasDomUsed;
    private String renderId;
    private String source;
    private String variantId;
    private String knownVariantId;
    private HashMap expectedImage;
    private Object activeExpectedMismatchRegions;

    public HashMap getExpectedImage() {
        return expectedImage;
    }

    public void setExpectedImage(HashMap expectedImage) {
        this.expectedImage = expectedImage;
    }

    public Boolean getIsPrimary() {
        return IsPrimary;
    }

    public void setIsPrimary(Boolean primary) {
        IsPrimary = primary;
    }

    public Object getImage() {
        return Image;
    }

    public void setImage(HashMap image) {
        Image = image;
    }

    public Object getThumbprint() {
        return Thumbprint;
    }

    public void setThumbprint(HashMap thumbprint) {
        Thumbprint = thumbprint;
    }

    public Object getImageMatchSettings() {
        return ImageMatchSettings;
    }

    public void setImageMatchSettings(Object imageMatchSettings) {
        ImageMatchSettings = imageMatchSettings;
    }

    public Boolean getIgnoreExpectedOutputSettings() {
        return IgnoreExpectedOutputSettings;
    }

    public void setIgnoreExpectedOutputSettings(Boolean ignoreExpectedOutputSettings) {
        IgnoreExpectedOutputSettings = ignoreExpectedOutputSettings;
    }

    public Boolean getIsMatching() {
        return IsMatching;
    }

    public void setIsMatching(Boolean matching) {
        IsMatching = matching;
    }

    public Boolean getAreImagesMatching() {
        return AreImagesMatching;
    }

    public void setAreImagesMatching(Boolean areImagesMatching) {
        AreImagesMatching = areImagesMatching;
    }

    public HashMap getAccessibilityStatus() {
        return AccessibilityStatus;
    }

    public void setAccessibilityStatus(HashMap accessibilityStatus) {
        AccessibilityStatus = accessibilityStatus;
    }

    public String getOccurredAt() {
        return OccurredAt;
    }

    public void setOccurredAt(String occurredAt) {
        OccurredAt = occurredAt;
    }

    public Object getUserInputs() {
        return UserInputs;
    }

    public void setUserInputs(Object userInputs) {
        UserInputs = userInputs;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        Tag = tag;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public String getExpectedImageId() {
        return ExpectedImageId;
    }

    public void setExpectedImageId(String expectedImageId) {
        ExpectedImageId = expectedImageId;
    }

    public String getExpectedThumbprintId() {
        return expectedThumbprintId;
    }

    public void setExpectedThumbprintId(String expectedThumbprintId) {
        this.expectedThumbprintId = expectedThumbprintId;
    }

    public String getImageId() {
        return getImageId(Image);
    }

    public String getThumbprintId() {
        return getImageId(Thumbprint);
    }

    private String getImageId(HashMap imageRecord) {
        return imageRecord.get("id").toString();
    }

    public Boolean getWasDomUsed() {
        return wasDomUsed;
    }

    public void setWasDomUsed(Boolean wasDomUsed) {
        this.wasDomUsed = wasDomUsed;
    }

    public String getRenderId() {
        return renderId;
    }

    public void setRenderId(String renderId) {
        this.renderId = renderId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVariantId() { return variantId; }

    public void setVariantId(String variantId) { this.variantId = variantId; }

    public String getKnownVariantId() { return knownVariantId; }

    public void setKnownVariantId(String knownVariantId) { this.knownVariantId = knownVariantId; }

    public Object getActiveExpectedMismatchRegions() {
        return activeExpectedMismatchRegions;
    }

    public void setActiveExpectedMismatchRegions(Object activeExpectedMismatchRegions) {
        this.activeExpectedMismatchRegions = activeExpectedMismatchRegions;
    }

}
