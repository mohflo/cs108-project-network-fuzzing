package com;

import com.test.Test;
import com.test.TestToolConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 *  Connects to game server/client and processes the messages sent between them.
 *  Launches the server/client threads for testing.
 */
public class Connection {
    private String testName;
    private Test test;
    Server server;
    private TestToolConfig configTest;

    Socket socketToServer;
    Socket socket;

    PrintWriter outClient;
    BufferedReader inClient;
    PrintWriter outServer;
    BufferedReader inServer;

    Logger logger;

    int gameServerPort;

    private Run serverThread;
    private Run clientThread;

    public Connection(Socket socket, Server server, int gameServerPort, Logger logger, Test test, TestToolConfig configTest) throws IOException {
        this.server = server;
        this.testName = "Test_" + test.getTestName();
        this.gameServerPort = gameServerPort;
        this.logger = logger;
        this.socket = socket;
        this.test = test;
        this.configTest = configTest;
    }

    public void sendToClient(String data) throws IOException {
        this.outClient.println(data);
        if (!data.isEmpty()) {
            logger.log(testName, "-> Client", data);
        }
    }

    public String receiveFromClient() throws IOException, InterruptedException {
        if (this.inClient.ready()) {
            String line = this.inClient.readLine();
            if (!line.isEmpty()) {
                logger.log(testName, "Client ->", line);
            }
            return line;
        }
        else {
            Thread.sleep(100);
            return "";
        }
    }

    public void sendToServer(String data) throws IOException {
        this.outServer.println(data);
        if (!data.isEmpty()) {
            logger.log(testName, "-> Server", data);
        }
    }

    public String receiveFromServer() throws IOException, InterruptedException {
        if (this.inServer.ready()) {
            String line = this.inServer.readLine();
            if (!line.isEmpty()) {
                logger.log(testName, "Server ->", line);
            }
            return line;
        }
        else {
            Thread.sleep(100);
            return "";
        }
    }

    void init() throws IOException {
        socketToServer = new Socket("localhost", gameServerPort);
        //System.out.println(id + " connected to server");

        this.socket.setSoTimeout(0);
        this.socketToServer.setSoTimeout(0);

        this.outClient = new PrintWriter(this.socket.getOutputStream(), true, StandardCharsets.UTF_8);
        this.inClient = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
        this.outServer = new PrintWriter(this.socketToServer.getOutputStream(), true, StandardCharsets.UTF_8);
        this.inServer = new BufferedReader(new InputStreamReader(this.socketToServer.getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Launch client and server threads.
     */
    public void start() {
        try {
            init();
            clientThread = new Run(this, true, test, logger, configTest);
            serverThread = new Run(this, false, test, logger, configTest);
            clientThread.start();
            serverThread.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Interrupts client and server threads, closes readers and sockets.
     */
    public void cancel() {
        clientThread.cancel();
        serverThread.cancel();

        try {
            outClient.close();
            inClient.close();
            outServer.close();
            inServer.close();

            socket.close();
            socketToServer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
