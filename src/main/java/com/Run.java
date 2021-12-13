package com;

import com.test.Test;
import com.test.TestToolConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Random;

/**
 *  (Threaded) Class that runs the actual tests.
 *  Incoming messages are picked up with receiveFromClient() / receiveFromServer(),
 *  test actions are applied with checkAndExecuteTest(),
 *  results are sent back with sendToClient() / sendToServer().
 */
public class Run extends Thread {
    Connection connection;
    boolean isClient;
    private final Test test;
    private final Logger logger;
    private final String clientOrServer;

    // Config main data
    private final String protocolSeparator;
    private final String[] commands;
    private final boolean ignoreCommands;
    private final String[] specialCharacters;

    // Config test data
    private final String testName;
    private final String testValue;

    private final String testNameLog;

    public Run(Connection connection, boolean isClient, Test test, Logger logger, TestToolConfig configTest) {
        this.isClient = isClient;
        this.connection = connection;
        this.test = test;
        this.logger = logger;
        if (isClient) {
            this.clientOrServer = "Client";
        } else {
            this.clientOrServer = "Server";
        }

        // Config main data
        this.testName = test.getTestName();
        this.testValue = test.getValue();

        // Config test data
        this.protocolSeparator = configTest.getProtocolSeparator();
        this.commands = configTest.getCommands();
        this.ignoreCommands = configTest.isIgnoreCommands();
        this.specialCharacters = configTest.getSpecialCharacters();
        this.testNameLog = "Test_" + testName;
    }

    @Override
    public void run() {
        if (isClient) {
            processServer();
        } else {
            processClient();
        }
    }

    /**
     * Sleeps the thread for the specified amount of time.
     */
    void waitDelay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes input from the game client and - depending on the current running test - relays
     * it to the game server.
     */
    void processClient() {
        while (!interrupted()) {
            try {
                String data = connection.receiveFromClient();

                if (!data.isEmpty()) {
                    // Check whether to only modify commands or everything else
                    if ((!ignoreCommands && isCommand(data)) || (ignoreCommands && !isCommand(data))) {
                        data = checkAndExecuteTest(data);
                    }
                    connection.sendToServer(data);
                }
            } catch (IOException | InterruptedException e) {
                //System.out.println("processClient error: " + e);
                logger.log(testNameLog, "General", "Client thread stopped.");
                break;
            }
        }
    }

    /**
     * Processes input from the game server and - depending on the current running test - relays
     * it to the game client.
     */
    void processServer() {
        while (!interrupted()) {
            try {
                String data = connection.receiveFromServer();

                if (!data.isEmpty()) {
                    // Check whether to only modify commands or everything else
                    if ((!ignoreCommands && isCommand(data)) || (ignoreCommands && !isCommand(data))) {
                        data = checkAndExecuteTest(data);
                    }
                    connection.sendToClient(data);
                }
            } catch (IOException | InterruptedException e) {
                //System.out.println("processServer error: " + e);
                logger.log(testNameLog, "General", "Server thread stopped.");
                break;
            }
        }
    }

    /**
     * Interrupts the thread and breaks the while loops.
     */
    public void cancel() {
        interrupt();
    }

    /**
     * Evaluates whether the provided string contains a command from the commands array.
     * @param message String that is to be evaluated
     * @return true if the message contains a command
     */
    boolean isCommand(String message) {
        for (String s: commands) {   // Could use stream here - maybe useful with more items?
            if (message.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks which test is currently running and executes the corresponding test method.
     * @param message Message string.
     * @return Modified message (if applicable), depending on the current test.
     */
    String checkAndExecuteTest(String message) {
        String newMessage = "";
        try{
            switch (testName) {
                case "Relay" -> newMessage = testRelay(message);
                case "Delay" -> newMessage = testDelay(message);
                case "Drop" -> newMessage = testDrop(message);
                case "Repeat" -> newMessage = testRepeat(message);
                case "TransformPartial" -> newMessage = testTransformPartial(message);
                case "DeletePartial" -> newMessage = testDeletePartial(message);
                case "ProtocolSeparators" -> newMessage = testProtocolSeparators(message);
                case "RandomString" -> newMessage = testRandomString(message);
                case "RandomBitString" -> newMessage = testRandomBitString(message);
                case "SpecialChars" -> newMessage = testSpecialChars(message);
                default -> newMessage = testRelay(message);
            }
        } catch (IOException e) {
            //e.printStackTrace();
            logger.log(testNameLog, "General", e.getMessage());
        }
        return newMessage;
    }

    /**
     * Relay Test: Relays the message without modification.
     * @param message Message string.
     * @return Unmodified message.
     */
    String testRelay(String message) {
        return message;
    }

    /**
     * Delay Test: Relays the message after a delay that is specified in the test config.
     * @param message Message string.
     * @return Unmodified message after a delay.
     */
    String testDelay(String message) {
        int duration = Integer.parseInt(testValue);
        if (duration > 0) {
            logger.log(testNameLog, clientOrServer, "Delaying for " + duration  + " sec.");
            waitDelay(duration * 1000);
        }
        return message;
    }

    /**
     * Drop Test: Drops the message, changing it to an empty string.
     * @param message Message string.
     * @return Empty string.
     */
    String testDrop(String message) {
        logger.log(testNameLog, clientOrServer, "Dropping message " + message);
        return "";
    }

    /**
     * Repeat Test: Sends the provided message repeatedly for a number of times specified in the config.
     * @param message Message string.
     * @return Unmodified message.
     */
    String testRepeat(String message) throws IOException {
        int times = Integer.parseInt(testValue) - 1;
        if (times > 0) {
            logger.log(testNameLog, clientOrServer,
                    "Repeating message " + message + " " + (times + 1) + " times.");
            for (int i = times; i > 0; i--) {
                if (isClient) {
                    connection.sendToClient(message);
                }
                else {
                    connection.sendToServer(message);
                }
            }
        }
        return message;
    }

    /**
     * TransformPartial Test: Replaces a random character in the message.
     * @param message  Message string.
     * @return
     */
    String testTransformPartial(String message) {
        char[] chars = message.toCharArray();
        int random = (int)(Math.random() * message.length());

        // Replacement characters to choose from:
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:,;-";
        Random ran = new Random();
        char randomChar = alphabet.charAt(ran.nextInt(alphabet.length()));
        chars[random] = randomChar;
        String newMessage = new String(chars);
        logger.log(testNameLog, clientOrServer,
                "Message " + message + " partially transformed into " + newMessage);
        return newMessage;
    }

    /**
     * DeletePartial Test: Deletes a random character of the message.
     * @param message Message string.
     * @return Message with random character removed.
     */
    String testDeletePartial(String message) {
        int random = (int)(Math.random() * (message.length()-1));
        if (random == 0) { // In case the random number generated is 0.
            random = 1;
        }
        String newMessage = message.substring(0, random) + message.substring(random + 1);
        logger.log(testNameLog, clientOrServer,
                "Message " + message + " partially deleted to " + newMessage);
        return newMessage;
    }

    /**
     * ProtocolSeparators Test: Adds multiple protocol separators to the message.
     * @param message Message string.
     * @return Message with multiple protocol separators added.
     */
    String testProtocolSeparators(String message) {
        int random = (int)(Math.random() * 10) + 1; // +1 for if random generated number is 0
        message = message + String.valueOf(protocolSeparator).repeat(Math.max(0, random));
        logger.log(testNameLog, clientOrServer,
                "Added multiple protocol separators to message " + message);
        return message + protocolSeparator;
    }

    /**
     * RandomString Test: Relays the message and also sends a random string afterwards.
     * @param message Message string.
     * @return Unmodified message.
     */
    String testRandomString(String message) throws IOException {
        // Adapted from https://www.baeldung.com/java-random-string
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int randomStringLength = Integer.parseInt(testValue);
        Random random = new Random();

        String randomString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(randomStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        logger.log(testNameLog, clientOrServer,
                "Sending random string " + randomString);

        if (isClient) {
            connection.sendToClient(randomString);
        }
        else {
            connection.sendToServer(randomString);
        }
        return message;
    }

    /**
     * RandomBitString Test: Relays the message and also sends a random (bit)string afterwards.
     * @param message Message string.
     * @return Unmodified message.
     */
    String testRandomBitString(String message) throws IOException {
        // Adapted from https://www.baeldung.com/java-random-string
        byte[] array = new byte[Integer.parseInt(testValue)]; // Length is bounded by value
        new Random().nextBytes(array);
        String randomBitString = new String(array, StandardCharsets.UTF_8);

        logger.log(testNameLog, clientOrServer,
                "Sending random bitstring " + randomBitString);

        if (isClient) {
            connection.sendToClient(randomBitString);
        }
        else {
            connection.sendToServer(randomBitString);
        }
        return message;
    }

    /**
     * SpecialChars Test: Transforms the command, replacing the command argument with randomly chosen
     * special character of those specified in the config.
     * @param message Message string.
     * @return Message with special character added.
     */
    String testSpecialChars(String message) {
        // Search for protocol separator
        int indexSeparator = message.indexOf(protocolSeparator);

        // If no protocol separator is found, add one
        if (indexSeparator == -1) {
            message = message + protocolSeparator;
        }

        // Choose a special character fom the config list
        int randomSpecialCharIndex = (int)(Math.random() * (specialCharacters.length - 1));

        logger.log(testNameLog, clientOrServer,
                "Adding special character " + specialCharacters[randomSpecialCharIndex] +
                        " to message " + message);
        return message + specialCharacters[randomSpecialCharIndex];
    }
}
