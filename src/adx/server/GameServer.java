package adx.server;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;

import adx.exceptions.AdXException;
import adx.messages.EndOfDayMessage;
import adx.messages.InitialMessage;
import adx.structures.Campaign;
import adx.util.Logging;
import adx.util.Parameters;
import adx.util.Sampling;

import com.esotericsoftware.kryonet.Connection;

/**
 * A concrete implementation of a game server.
 * 
 * @author Enrique Areyan Viqueira
 */
public class GameServer extends GameServerAbstract {

  /**
   * Constructor.
   * 
   * @param port
   *          - on which the server will run.
   * @throws IOException
   *           in case the server could not be started.
   */
  public GameServer(int port) throws IOException {
    super(port);
  }

  /**
   * Runs the game.
   */
  protected void runAdXGame() {
    // First order of business is to accept connections for a fixed amount of time
    Instant deadlineForNewPlayers = Instant.now().plusSeconds(Parameters.SECONDS_DURATION_DAY);
    Logging.log("[-] Accepting connections until " + deadlineForNewPlayers);
    while (Instant.now().isBefore(deadlineForNewPlayers))
      ;
    // Do not accept any new agents beyond deadline. Play with present agents.
    this.acceptingNewPlayers = false;
    // Check if there is at least one agent to play the game.
    if (this.namesToConnections.size() > 0) {
      Instant endTime = Instant.now().plusSeconds(Parameters.SECONDS_DURATION_DAY);
      this.sendInitialMessage();
      this.sendEndOfDayMessage();
      // Play game
      while (true) {
        if (Instant.now().isAfter(endTime)) {
          // Time is up for the present day, stop accepting bids for this day
          // and run corresponding auctions.
          this.serverState.printServerState();
          this.serverState.advanceDay();
          // Run auction for the bids received the day before.
          synchronized (this.serverState) {
            synchronized (this) {
              try {
                this.serverState.runAdAuctions();
                this.serverState.runCampaignAuctions();
              } catch (AdXException e) {
                Logging.log("[x] Error running ad auction -> " + e.getMessage());
              }
              endTime = Instant.now().plusSeconds(Parameters.SECONDS_DURATION_DAY);
              this.sendEndOfDayMessage();
            }
          }
        }
      }
    } else {
      Logging.log("[x] There are no players, stopping the server at " + Instant.now());
      this.gameServer.stop();
    }
  }

  /**
   * Send the initial message to all registered agents.
   */
  private synchronized void sendInitialMessage() {
    Logging.log("[-] Sending initial message to:");
    for (Entry<String, Connection> agent : this.namesToConnections.entrySet()) {
      Logging.log("\t[-] " + agent.getKey() + ", " + agent.getValue());
      Connection agentConnection = agent.getValue();
      try {
        Campaign c = Sampling.sampleInitialCampaign();
        this.serverState.registerCampaign(c, agent.getKey());
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
    Logging.log("[-] Sending end of day message. ");
    Instant timeEndOfDay = Instant.now().plusSeconds(Parameters.SECONDS_DURATION_DAY);
    this.serverState.currentDayEnd = timeEndOfDay;
    List<Campaign> listOfCampaigns = null;
    try {
      listOfCampaigns = this.serverState.generateCampaignsOpportunities();
    } catch (AdXException e) {
      Logging.log("[x] Error sampling list of campaigns for auction --> " + e.getMessage());
    }
    for (Entry<String, Connection> agent : this.namesToConnections.entrySet()) {
      agent.getValue().sendTCP(
          new EndOfDayMessage(this.serverState.getCurrentDay() + 1, timeEndOfDay.toString(), this.serverState.getSummaryStatistic(agent.getKey()),
              listOfCampaigns, this.serverState.getWonCampaigns(agent.getKey())));
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
