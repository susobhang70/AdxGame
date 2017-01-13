package adx.server;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.util.Logging;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This class maintains the state of the server for a single game.
 * 
 * @author Enrique Areyan Viqueira
 */
public class ServerState {

  /**
   * A unique identifier of the game.
   */
  private final int gameId;

  /**
   * Starting time of the game.
   */
  private final long gameStartTime;

  /**
   * Current simulated day.
   */
  private int currentDay;

  /**
   * Instant object when the current simulated day ends.
   */
  protected Instant currentDayEnd;

  /**
   * A set of agents names
   */
  private final Set<String> agentsNames;

  /**
   * A map from campaign to agentsNames.
   */
  private Map<Integer, String> campaignsOwnership;

  /**
   * A map from days to a tuple (Agent, BidBundle)
   */  
  private Table<Integer, String, BidBundle> bidBundles;

  /**
   * Constructor.
   * 
   * @param gameId
   */
  public ServerState(int gameId) {
    this.gameId = gameId;
    this.gameStartTime = System.nanoTime();
    this.currentDay = 0;
    this.agentsNames = new HashSet<String>();
    this.campaignsOwnership = new HashMap<Integer, String>();
    this.bidBundles = HashBasedTable.create();

  }

  /**
   * Add a bid bundle.
   * 
   * @param day
   * @param agent
   * @param bidBundle
   */
  public boolean addBidBundle(int day, String agent, BidBundle bidBundle) {
    if (day != this.currentDay) {
      Logging.log("[x] Bid bundle on day " + day + " for agent " + agent + ", too late current day " + this.currentDay + ", not accepted.");
      return false;
    } else {
      Logging.log("[-] Bid bundle on day " + day + " for agent " + agent + ", accepted.");
      this.bidBundles.put(day, agent, bidBundle);
      return true;
    }
  }

  /**
   * Getter.
   * 
   * @return the time, in nanoseconds, the game started.
   */
  public long getGameStartTime() {
    return this.gameStartTime;
  }

  /**
   * Getter.
   * 
   * @return the current simulated day.
   */
  public int getCurrentDay() {
    return this.currentDay;
  }

  public void advanceDay() {
    this.currentDay++;
  }

  /**
   * Saves an agent.
   * 
   * @param agentName
   */
  public void saveAgentName(String agentName) {
    this.agentsNames.add(agentName);
  }

  /**
   * Register a campaign to an agent.
   * 
   * @param campaignId
   * @param agentName
   * @throws AdXException
   */
  public void registerCampaign(int campaignId, String agentName) throws AdXException {
    if (this.campaignsOwnership.containsKey(campaignId)) {
      throw new AdXException("Campaign " + campaignId + " has alread been registered to " + agentName);
    }
    this.campaignsOwnership.put(campaignId, agentName);
  }

  /**
   * Validates that the campaigns in a bid bundle actually belong to the given
   * agent.
   * 
   * @param bidBundle
   * @param agent
   * @param campaignsOwnership
   * @throws AdXException 
   */
  public boolean validateBidBundle(BidBundle bidBundle, String agent) throws AdXException {
    for(BidEntry bidEntry : bidBundle.getBidEntries()) {
      if(!this.campaignsOwnership.containsKey(bidEntry.getCampaignId())) {
        throw new AdXException("The bid bundle refers to a non-existing campaign.");
      } else if(!this.campaignsOwnership.get(bidEntry.getCampaignId()).equals(agent)) {
        throw new AdXException("The bid bundle refers to a campaign not owned by the agent.");
      }
    }
    return true;
  }
  
  /**
   * A light string representation for debugging purposes.
   */
  public void printServerState() {
    Logging.log("[-] Server State: \n\t Game Id = " + this.gameId
        + "\n\t Agents Names = " + this.agentsNames + "\n\t Campaings "
        + this.campaignsOwnership + "\n\t Day = " + this.currentDay
        + "\n\t EndOfDay = "
        + ((this.currentDayEnd != null) ? this.currentDayEnd.toString() : "")
        + "\n\t BidBundles = " + this.bidBundles);
  }

}
