
# cs108-network-fuzzing-tool
## Introduction
This tool can be used to evaluate the text-based network protocol of Java client-server applications such as the games implemented in the computer science lecture "CS108 - Programming Project".

It applies a man-in-the-middle-approach, relaying the communication between server and client and, depending on the configured tests, modifies the messages that are sent.

The tool works exclusively with applications that are started with the following syntax:

    java -jar game.jar (client <serveraddr>:<serverport>) | (server <serverport>)

## Usage

The tool can be launched with the following command:

    java -jar network-fuzzing-too.jar game.jar gameConfig.json

It will then run a series of tests, restarting game client and game server for each test. All communication and events are logged.

### Configuration
The tool can be configured with a JSON config file of the following form:

    {
      "gameServerPort" : Int (e.g. 8880),
      "testToolPort" : Int (e.g. 8990),
      "protocolSeparator" : String (e.g. "%"),
      "commands" : ?String (e.g. ["GIVGL", "UPDAT", "CHATO"]),
      "ignoreCommands" : boolean (e.g. false),
      "specialCharacters" : ?String (e.g. ["😜", "\n", "👀", "\r"]),
      "timeout" : Int (e.g. 5),
      "printLog" : boolean (e.g. true),
      "logModeCSV" : boolean (e.g. false),
      "tests" : ?TestObjects [
        {
          "testName" : String (e.g. "Relay"),
          "isEnabled" : boolean (e.g. true),
          "duration" : Int (e.g. 20),
          "value" : String (e.g. "0")
        }
      ]
    }

#### Properties
 - **gameServerPort**: Port on which the game server will be started.
 - **testToolPort**: Port on which the tool will listen to the game client.
 - **protocolSeparator**: The character(s) used to separate the commands in the text-based protocol.
 - **commands**: Which commands to test. All other commands will be relayed without modification.
 - **ignoreCommands**: If true, reverses the way the commands are filtered,  ignoring the listed ones and modifying all others.
 - **specialCharacters**: What special characters to check in certain tests.
 - **timeout**: Number of seconds of silence from either server or client required to issue a warning in the log.
 - **printLog**: If true, prints all logged data to the console.
 - **logModeCSV**: If true, logs the data in a CSV readable format.
 - **tests**: List of all configured tests.
 - **testName**: Name of the test. Tests not properly configured in the `Run` class will default to standard Relay Test.
 - **isEnabled**: Test will be skipped if false.
 - **duration**: Duration of the test. Afterwards connections will be closed and the game JARs stopped.
 - **value**: Value that can be used to further configure the test. Example: value controls the duration of the delay in the Delay Test.

An example config is included in the resource folder.

## Test Types
The following test types are currently available:

**Note**: Here, `message` describes only the text strings containing the commands specified in `config.json`, 
all other commands will be ignored and relayed normally.

### Relay
Relays the message without modification or delay.
### Delay
Relays the message after the delay specified in `test.value`.
### Drop
Drops the message.
### Repeat
Sends the message repeatedly. The number of repetitions is specified in `test.value`.
### TransformPartial
Transforms the message, replacing a random character.
### DeletePartial
Transforms the message, deleting a random character.
### ProtocolSeparators
Adds multiple protocol separators to the message. Protocol separators are specified in the config.
### RandomString
Relays the message and also sends a new message containing a random string. The length of the string is specified in the config.
### RandomBitString
Relays the message and also sends a new message containing a random bitstring. The length of the bitstring is specified in the config.
### SpecialChars
Relays the message, appending a random character of those specified in the config.

# How can I add other tests?
The tool is built with the idea of making it as easy as possible to implement your own tests.
There are three steps involved:
1.  Create a new method in the `Run` class, implementing your own test logic.
2.  Add the test name (String) and the method call to the switch statement in the `Run.checkAndExecuteTest()` method.
3.  Add the test in the config.

