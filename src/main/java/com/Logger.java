package com;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Logger {
    private String filename;
    private final boolean printLog;
    private final boolean enableCSV;

    public Logger(String gameName, boolean printLog, boolean enableCSV) throws IOException {
        this.printLog = printLog;
        this.enableCSV = enableCSV;
        String logFileName = gameName.substring(0, gameName.length() - 4);

        int fileIndex = 1;
        boolean fileCreated = false;

        while (!fileCreated) {
            // Create new logfile (filename-1.log) and increment index if file already exists
            filename = logFileName + "-" + fileIndex + ".log";
            File logFile = new File(filename);
            fileCreated = logFile.createNewFile();
            fileIndex++;
        }
        System.out.println("Logfile created.");

        // If CSV mode is enabled, write column names first.
        if (enableCSV) {
            try {
                String text = "timestamp;category;subject;message";
                Files.writeString(Paths.get(filename), text + "\n", StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the provided arguments in the logfile using the following format:<br>
     * [timestamp] [category] [subject]: message
     * @param category Category
     * @param subject Subject
     * @param message Log message
     */
    public void log(String category, String subject, String message) {
        String timestamp = getTimestamp();
        String text = "";

        if (!enableCSV) {
            text = "[" + timestamp + "] [" + category + "] [" + subject + "]: " + message;
        }
        else {
            text = timestamp + ";" + category + ";" + subject + ";" + message;
        }
        try {
            Files.writeString(Paths.get(filename), text + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (printLog) {
            System.out.println(text);
        }
    }

    private void logDefault(String subject, String message) {

    }

    private void logCSV(String subject, String message) {

    }

    private String getTimestamp() {
        return DateTimeFormatter
                .ofPattern("HH:mm:ss")
                .withZone(ZoneOffset.of("+01:00"))
                .format(Instant.now());
    }
}
