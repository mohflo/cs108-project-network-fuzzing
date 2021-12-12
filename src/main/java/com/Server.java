package com;

import com.test.Config;
import com.test.Test;
import com.test.TestToolConfig;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 *  Server class with main method.
 */
public class Server {
    ServerSocket serverSocket;
    Logger logger;

    public Server(int port, Logger logger, String testName) throws IOException {
        logger.log(testName, "General", "Waiting for client to connect.");
        serverSocket = new ServerSocket(port);
        //System.out.println(serverSocket.getInetAddress());
        this.serverSocket.setSoTimeout(0);
    }

    public Socket accept() throws IOException {
        return serverSocket.accept();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        // Checking main arguments
        if (args.length != 2) {
            System.out.println("Invalid arguments. Please use the following syntax:");
            System.out.println("java -jar <network-fuzzing.jar> <game.jar> <gameConfig.jar>");
            return;
        }

        // Check whether files exist
        File game = new File(args[0]);
        File config = new File(args[1]);

        if (!game.exists()) {
            System.out.println("Cannot find game file " + args[0]);
            return;
        }
        if (!config.exists()) {
            System.out.println("Cannot find config file " + args[1]);
            return;
        }

        String gameName = args[0];

        // Read config data
        TestToolConfig configTest = Config.getTestConfig(args[1]);
        int gameServerPort = configTest.getGameServerPort();
        int gameClientPort = configTest.getTestToolPort();
        List<Test> tests = configTest.getTests();

        Logger logger = new Logger(gameName, configTest.isPrintLog(), configTest.isLogModeCSV());

        // Loop over all the configured tests
        for (Test test : tests) {
            String testName = test.getTestName();
            String testNameLog = test.getTestNameLog();

            // Skipping disabled tests.
            if (!test.isEnabled()) {
                continue;
            }

            int testDuration = test.getDuration() * 1000;
            Server server = new Server(gameClientPort, logger, testName);

            // Starting JAR processes
            RunGame serverJAR = new RunGame(false, gameName, gameServerPort,
                                            gameClientPort, logger, testName);
            RunGame clientJAR = new RunGame(true, gameName, gameServerPort,
                                            gameClientPort, logger, testName);
            serverJAR.start();
            clientJAR.start();

            logger.log(testNameLog, "Client", "Waiting for client connection.");
            Socket socket = server.accept();
            Client client = new Client(socket, server, gameServerPort, logger, test, configTest);
            logger.log(testNameLog, "Client", "Client connected.");
            logger.log(testNameLog, "General", "Started.");

            try {
                client.socket = socket;
                client.start();
            } catch (Exception e) {
                System.out.println("Client error: " + e.getMessage());
            }

            // Wait for the test to finish
            Thread.sleep(testDuration);
            logger.log(testNameLog, "General", "Test duration passed.");

            // Stop socket threads
            client.cancel();
            logger.log(testNameLog, "General", "Sockets closed.");

            // Stop JAR processes
            clientJAR.cancel();
            serverJAR.cancel();

            Thread.sleep(500);
        }
    }
}












