package com.test;

import java.util.List;

/**
 *   TestToolConfig object as specified in JSON config.
 */
public class TestToolConfig {
    private int gameServerPort;
    private int testToolPort;
    private String protocolSeparator;
    private String[] commands;
    private boolean ignoreCommands;
    private String[] specialCharacters;
    private boolean printLog;
    private boolean logModeCSV;
    private List<Test> tests;

    public int getGameServerPort() {
        return gameServerPort;
    }

    public void setGameServerPort(int gameServerPort) {
        this.gameServerPort = gameServerPort;
    }

    public int getTestToolPort() {
        return testToolPort;
    }

    public void setTestToolPort(int testToolPort) {
        this.testToolPort = testToolPort;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }

    public String getProtocolSeparator() {
        return protocolSeparator;
    }

    public String[] getCommands() {
        return commands;
    }

    public boolean isIgnoreCommands() {
        return ignoreCommands;
    }

    public String[] getSpecialCharacters() {
        return specialCharacters;
    }

    public boolean isPrintLog() {
        return printLog;
    }

    public boolean isLogModeCSV() {
        return logModeCSV;
    }
}
