package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Runs the JAR file of the game.<br>
 * NOTE: Currently not compatible with Windows machines.
 */
public class RunGame extends Thread {
    private final boolean isClient;
    private final String clientOrServer;
    private final String gameName;
    private final int serverPort;
    private final int clientPort;
    private final String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    private final Logger logger;
    private final String testName;

    public RunGame(boolean isClient, String gameName, int serverPort, int clientPort, Logger logger, String testName) {
        this.isClient = isClient;
        this.gameName = gameName;
        this.serverPort = serverPort;
        this.clientPort = clientPort;
        this.logger = logger;
        if (isClient) {
            this.clientOrServer = "GameClient";
        } else {
            this.clientOrServer = "GameServer";
        }
        this.testName = "Test_" + testName;

    }

    @Override
    public void run() {
        String[] command = new String[]{};

        if (os.contains("win")) {
            if (isClient) {
                command = new String[]{"cmd", "/c", "java", "-jar", gameName, "client", "localhost:" + clientPort};
            } else {
                command = new String[]{"cmd", "/c", "java", "-jar", gameName, "server", String.valueOf(serverPort)};
            }
        } else if (os.contains("mac")) {
            if (isClient) {
                command = new String[]{"/bin/bash", "-c", "java", "-jar", gameName, "client", "localhost:" + clientPort};
            } else {
                command = new String[]{"/bin/bash", "-c", "java", "-jar", gameName, "server", String.valueOf(serverPort)};
            }
        } else if (os.contains("nix") || os.contains("linux")) {
            if (isClient) {
                command = new String[]{"java", "-jar", gameName, "client", "localhost:" + clientPort};
            } else {
                command = new String[]{"java", "-jar", gameName, "server", String.valueOf(serverPort)};
            }
        } else {
            logger.log(testName, "Error", "Command not configured for this OS.");
        }

        /* Adapted from https://stackoverflow.com/a/25735681. */
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = null;
        try {
            Thread.sleep(500);
            logger.log(testName, clientOrServer, "Launching JAR.");
            p = pb.start();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = "";

        // Check whether the thread has been interrupted
        while (!interrupted()) {
            try {
                // Checking reader.ready() is necessary because otherwise the thread would be stuck
                // at reader.readLine() and could not be interrupted.
                if (reader.ready()) {
                    line = reader.readLine();

                    if (line != null) {
                        logger.log(testName, clientOrServer, line);
                    }
                }
                sleep(100); // Sleeping to give the CPU some time to relax.
            } catch (IOException | InterruptedException e) {
                //e.printStackTrace();
                break;
            }
        }

        // End process. NOTE: This does not work correctly on Windows machines because destroy()
        // only kills the cmd.exe and not its child processes!
        p.destroy();
        logger.log(testName, clientOrServer, "JAR process stopped.");
    }

    /**
     * Calls interrupt() and stops the thread, shutting down the JAR process.
     */
    public void cancel() {
        interrupt();
    }
}
