package com.test;

public class Test {

    private String testName;
    private boolean isEnabled;
    private int duration;
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTestName() {
        return testName;
    }

    public String getTestNameLog() { return "Test_" + testName; }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
