package adx.server;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import adx.exceptions.AdXException;
import adx.messages.ACKMessage;
import adx.messages.ConnectServerMessage;
import adx.messages.EndOfDayMessage;
import adx.messages.InitialMessage;
import adx.structures.BidBundle;
import adx.structures.Campaign;
import adx.util.Logging;
import adx.util.Sampling;
import adx.util.Startup;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

/**
 * A simple server for the AdX game.
 * 
 * @author Enrique Areyan Viqueira
 */
public class GameServer {

  private static final int DURATION_DAY = 10;
  
  /**
   * Server Kryo object.
   */
  private final Server gameServer;

  /**
   * Maps that contains all the connections.
   */
  protected Map<String, Connection> namesToConnections;
  protected Map<Connection, String> connectionsToNames;

  /**
   * A boolean that indicates whether the server accepts new players.
   */
  private boolean acceptingNewPlayers;

  /**
   * Current game.
   */
  private int gameNumber = 0;

  /**
   * An object that maintains the state of the server.
   */
  private ServerState serverState;

  /**
   * Server constructor.
   * 
   * @param port
   * @throws IOException
   */
  public GameServer(int port) throws IOException {

    Logging.log("[-] Server Initialized at " + Instant.now());
    this.namesToConnections = new ConcurrentHashMap<String, Connection>();
    this.connectionsToNames = new ConcurrentHashMap<Connection, String>();
    this.acceptingNewPlayers = true;
    this.gameServer = new Server();
    this.gameServer.start();
    this.gameServer.bind(port, port);
    Startup.start(this.gameServer.getKryo());
    this.gameServer.addListener(new Listener() {
      public void received(Connection connection, Object message) {
        // Logging.log("Received a connection");
        try {
          if (message instanceof ConnectServerMessage) {
            handleJoinGameMessage((ConnectServerMessage) message, connection);
          } else if (message instanceof BidBundle) {
            handleBidBundleMessage((BidBundle) message, connection);
          } else if (message instanceof KeepAlive) {
            // Internal kryo message, ignore.
          } else {
            Logging.log("[x] Received an unknown message from " + connection + ", here it is " + message);
          }
        } catch (Exception e) {
          Logging.log("An exception occurred while trying to parse a message in the server");
          e.printStackTrace();
        }
      }
    });
    this.gameNumber++;
    this.serverState = new ServerState(this.gameNumber);
  }

  /**
   * Handles a BidBundle message.
   * 
   * @param bidBundle
   * @param connection
   */
  private void handleBidBundleMessage(BidBundle bidBundle, Connection connection) {
    Logging.log("[-] Received the following bid bundle: \n\t\t " + bidBundle + ", from " + connection);
    if (this.serverState.addBidBundle(bidBundle.getDay(), this.connectionsToNames.get(connection), bidBundle)) {
      connection.sendTCP(new ACKMessage(true, "Bid bundle for day " + bidBundle.getDay() + " received OK."));
    } else {
      connection.sendTCP(new ACKMessage(false, "Bid bundle for day " + bidBundle.getDay() + " too late."));
    }
  }

  /**
   * Handles a Join Game message.
   * 
   * @param joinGameMessage
   * @param agentConnection
   * @throws Exception
   */
  private void handleJoinGameMessage(ConnectServerMessage joinGameMessage, Connection agentConnection) throws Exception {
    if (!this.acceptingNewPlayers) {
      joinGameMessage.setServerResponse("Not accepting agents");
      joinGameMessage.setStatusCode(3);
      agentConnection.sendTCP(joinGameMessage);
      return;
    }
    String agentName = joinGameMessage.getAgentName();
    String agentPassword = joinGameMessage.getAgentPassword();
    Logging.log("\t[-] Trying to register agent: " + agentName + ", with password: " + agentPassword);
    String serverResponse;
    int statusCode;
    if (this.namesToConnections.containsKey(agentName)) {
      Logging.log("\t\t[x] Agent " + agentName + " is already registered");
      serverResponse = "Already Registered";
      statusCode = 0;
    } else if (areAgentCredentialsValid(agentName, agentPassword)) {
      Logging.log("\t\t[-] Agent credentials are valid, agent registered");
      this.namesToConnections.put(agentName, agentConnection);
      this.connectionsToNames.put(agentConnection, agentName);
      this.serverState.saveAgentName(agentName);
      serverResponse = "OK";
      statusCode = 1;
    } else {
      Logging.log("\t\t[-] Could not register agent: credentials are not valid");
      serverResponse = "Invalid Credentials";
      statusCode = 2;
    }
    joinGameMessage.setServerResponse(serverResponse);
    joinGameMessage.setStatusCode(statusCode);
    agentConnection.sendTCP(joinGameMessage);
  }

  // A better way is to have all the credentials in memory once at startup
  private synchronized boolean areAgentCredentialsValid(String agentName, String agentPassword) {
    // For development purposes, this map contains the allowable agents
    // along with their passwords. This should be obtained from a database.
    HashMap<String, String> agentsInfo = new HashMap<String, String>();
    agentsInfo.put("enrique0", "123456");
    agentsInfo.put("enrique1", "123456");
    agentsInfo.put("enrique2", "123456");
    return agentsInfo.containsKey(agentName) && agentsInfo.get(agentName).equals(agentPassword);
  }

  /**
   * Send the initial message to all registered agents.
   */
  private synchronized void sendInitialMessage() {
    Logging.log("[-] Sending initial message to:");
    for (Entry<String, Connection> agent : this.namesToConnections.entrySet()) {
      Logging.log("\t[-] " + agent.getKey() + "," + agent.getValue());
      Connection agentConnection = agent.getValue();
      try {
        Campaign c = Sampling.sampleInitialCampaign();
        this.serverState.registerCampaign(c.getId(), agent.getKey());
        agentConnection.sendTCP(new InitialMessage(c, this.gameNumber));
      } catch (AdXException e) {
        Logging.log("Error trying to sample an initial campaign.");
        e.printStackTrace();
      }
    }
  }

  /**
   * Send the end of day message to all agents.
   */
  private synchronized void sendEndOfDayMessage() {
    Logging.log("[-] Sending end of day message: ");
    Instant timeEndOfDay = Instant.now().plusSeconds(DURATION_DAY);
    Logging.log("\t[-] Time Elapsed " + Duration.between(Instant.now(), timeEndOfDay).getSeconds() + " s, " + " of game time, we are on day " + this.serverState.getCurrentDay());
    this.serverState.currentDayEnd = timeEndOfDay;
    EndOfDayMessage endOfDayMessage = new EndOfDayMessage(this.serverState.getCurrentDay(), timeEndOfDay.toString());
    for (Entry<String, Connection> agent : this.namesToConnections.entrySet()) {
      agent.getValue().sendTCP(endOfDayMessage);
    }
  }

  /**
   * Runs the game.
   */
  private void runAdXGame() {
    // First order of business is to accept connections for a fixed amount of time
    Instant deadlineForNewPlayers = Instant.now().plusSeconds(DURATION_DAY);
    Logging.log("[-] Accepting connections until " + deadlineForNewPlayers);
    while (Instant.now().isBefore(deadlineForNewPlayers));
    // Do not accept any new agents beyond deadline. Play with present agents.
    this.acceptingNewPlayers = false;
    // Check if there is at least one agent to play the game.
    if (this.namesToConnections.size() > 0) {
      Instant endTime = Instant.now().plusSeconds(DURATION_DAY);
      this.sendInitialMessage();
      this.sendEndOfDayMessage();
      // Play game
      while (true) {
        if (Instant.now().isAfter(endTime)) {
          // Time is up for the present day, stop accepting bids for this day
          // and run corresponding auctions.
          this.serverState.printServerState();
          endTime = Instant.now().plusSeconds(DURATION_DAY);
          this.serverState.advanceDay();
          this.sendEndOfDayMessage();
          // Run auction for the bids received the day before.
          
        }
      }
    } else {
      Logging.log("[x] There are no players, stopping the server at " + Instant.now());
      this.gameServer.stop();
    }
  }

  /**
   * Main server method.
   * 
   * @param args
   */
  public static void main(String[] args) {
    try {
      // Try to initialize the server.
      new GameServer(9898).runAdXGame();
    } catch (IOException e) {
      Logging.log("Error initializing the server --> ");
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
