package adx.server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import adx.auctions.AdAuctions;
import adx.auctions.AdStatistics;
import adx.auctions.CampaignAuctions;
import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.Campaign;
import adx.util.Logging;
import adx.util.Pair;
import adx.util.Parameters;
import adx.util.Sampling;

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
   * A map from Day -> Agent -> List of Campaigns.
   */
  private Table<Integer, String, List<Campaign>> agentsCampaings;

  /**
   * A map from days to a tuple (Agent, BidBundle)
   */
  private Table<Integer, String, BidBundle> bidBundles;

  /**
   * An object that keeps track of the AdAuctions statistics.
   */
  private AdStatistics adStatistics;

  /**
   * An object that stores the campaigns for auction each day.
   */
  private Map<Integer, List<Campaign>> campaignsForAuction;

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
    this.campaignsForAuction = new HashMap<Integer, List<Campaign>>();
    this.agentsCampaings = HashBasedTable.create();
  }

  /**
   * Add a bid bundle.
   * 
   * @param day
   * @param agent
   * @param bidBundle
   */
  public Pair<Boolean, String> addBidBundle(int day, String agent, BidBundle bidBundle) {
    try {
      this.validateBidBundle(day, bidBundle, agent);
      Logging.log("[-] Bid bundle on day " + day + " for agent " + agent + ", accepted.");
      this.bidBundles.put(day, agent, bidBundle);
    } catch (AdXException e) {
      return new Pair<Boolean, String>(false, e.getMessage());
    }
    return new Pair<Boolean, String>(true, "OK");
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

  /**
   * Advances the simulated day.
   */
  public void advanceDay() {
    this.currentDay++;
    Logging.log("[*] Day " + this.currentDay + " ended at " + Instant.now());
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
  public void registerCampaign(Campaign campaign, String agentName) throws AdXException {
    int campaignId = campaign.getId();
    if (this.campaignsOwnership.containsKey(campaignId)) {
      throw new AdXException("Campaign " + campaignId + " has alread been registered to " + agentName);
    }
    this.campaignsOwnership.put(campaignId, agentName);
    if (!this.agentsCampaings.contains(this.currentDay, agentName)) {
      this.agentsCampaings.put(this.currentDay, agentName, new ArrayList<Campaign>());
    }
    this.agentsCampaings.get(this.currentDay, agentName).add(campaign);
  }

  /**
   * Validates a bid bundle. This means check if: (1) the campaigns in a bid bundle actually belong to the given agent. (2) the limit on a query is at least the
   * bid.
   * 
   * @param bidBundle
   * @param agent
   * @param campaignsOwnership
   * @throws AdXException
   */
  public void validateBidBundle(int day, BidBundle bidBundle, String agent) throws AdXException {
    if (day != this.currentDay + 1) {
      throw new AdXException("Received Bid bundle for day " + day + " for agent " + agent + ", but currently accepting for day " + (this.currentDay + 1) + ". Bid Bundle not accepted.");
    }
    for (BidEntry bidEntry : bidBundle.getBidEntries()) {
      if (!this.campaignsOwnership.containsKey(bidEntry.getCampaignId())) {
        Logging.log(bidBundle);
        Logging.log(bidEntry);
        throw new AdXException("The entry " + bidEntry + " refers to a non-existing campaign.");
      } else if (!this.campaignsOwnership.get(bidEntry.getCampaignId()).equals(agent)) {
        throw new AdXException("The entry " + bidEntry + " refers to a campaign not owned by the agent.");
      } else if (bidEntry.getQuery() == null) {
        throw new AdXException("The entry " + bidEntry + " refers to a null query.");
      } else if (bidEntry.getLimit() < bidEntry.getBid()) {
        throw new AdXException("The entry " + bidEntry + " limit is less than the bid.");
      }
    }
  }

  /**
   * Runs all the ad auctions.
   * 
   * @throws AdXException
   */
  public void runAdAuctions() throws AdXException {
    // Initialize the AdStatistics object, only once.
    if (this.adStatistics == null) {
      this.adStatistics = new AdStatistics(this.agentsNames);
    }
    Logging.log("\t[-] Try to run ad auctions for day: " + this.currentDay);
    AdAuctions.runAllAuctions(this.currentDay, this.bidBundles.row(this.currentDay), this.adStatistics);
    Logging.log("\t[-] Done running ad auctions for day: " + this.currentDay);
  }

  /**
   * Generates and stores a list of campaigns opportunities for the current simulated day.
   * 
   * @throws AdXException
   */
  public List<Campaign> generateCampaignsOpportunities() throws AdXException {
    if (!this.campaignsForAuction.containsKey(this.currentDay + 1)) {
      List<Campaign> listOfCampaigns = Sampling.sampleCampaingList(Parameters.NUMBER_AUCTION_CAMPAINGS);
      this.campaignsForAuction.put(this.currentDay + 1, listOfCampaigns);
    } else {
      throw new AdXException("[x] Already sample campaign opportunities for day: " + (this.currentDay + 1));
    }
    return this.campaignsForAuction.get(this.currentDay + 1);
  }

  /**
   * Run all campaigns auctions.
   * 
   * @throws AdXException
   */
  public void runCampaignAuctions() throws AdXException {
    Logging.log("\t[-] Run campaign auction on day " + this.currentDay);
    List<Campaign> campaignsForAuction = this.campaignsForAuction.get(this.currentDay);
    for (Campaign campaign : campaignsForAuction) {
      Logging.log("\t\t [-] Campaign = " + campaign);
      List<Pair<String, Double>> filteredBids = CampaignAuctions.filterBids(campaign.getId(), this.bidBundles.row(this.currentDay));
      Logging.log("\t\t [-] Filtered bids = " + filteredBids);
      if (filteredBids.size() > 0) {
        Pair<String, Double> winner = CampaignAuctions.runCampaignAuction(filteredBids);
        Logging.log("\t\t\t [-] Winner!!! = " + winner.getElement1());
        this.registerCampaign(campaign, winner.getElement1());
        campaign.setBudget(winner.getElement2());
      }
    }
  }

  /**
   * Given an agent, returns a list of all campaigns won by the agent in the current day.
   * 
   * @param agent
   * @return
   */
  public List<Campaign> getWonCampaigns(String agent) {
    return this.agentsCampaings.get(this.currentDay, agent);
  }

  /**
   * Returns the daily summary statistics.
   * 
   * @param day
   * @param agent
   * @return
   */
  public Map<Integer, Pair<Integer, Double>> getSummaryStatistic(String agent) {
    // Initialize the AdStatistics object, only once.
    if (this.adStatistics == null) {
      this.adStatistics = new AdStatistics(this.agentsNames);
    }
    return this.adStatistics.getDailySummary(this.currentDay, agent);
  }

  /**
   * Printer.
   * 
   * @return
   */
  public String printNiceCampaignTable() {
    String ret = "";
    if (this.agentsCampaings.rowMap().entrySet().size() > 0) {
      for (Entry<Integer, Map<String, List<Campaign>>> x : this.agentsCampaings.rowMap().entrySet()) {
        ret += "\n\t\t Day: " + x.getKey();
        for (Entry<String, List<Campaign>> y : x.getValue().entrySet()) {
          ret += "\n\t\t\t Agent: " + y.getKey();
          for (Campaign z : y.getValue()) {
            ret += "\n\t\t\t\t " + z;
          }
        }
      }
    } else {
      ret += "\n\t\t Currently, there are no campaigns.";
    }
    return ret;
  }

  /**
   * Printer.
   * 
   * @return
   */
  public String printNiceCampaignForAuctionList() {
    String ret = "";
    if (this.campaignsForAuction.entrySet().size() > 0) {
      for (Entry<Integer, List<Campaign>> x : this.campaignsForAuction.entrySet()) {
        ret += "\n\t\t Day: " + x.getKey();
        for (Campaign c : x.getValue()) {
          ret += "\n\t\t\t " + c;
        }
      }
    } else {
      ret += "\n\t\t Currently, there are no campaigns for auction";
    }
    return ret;
  }

  /**
   * A light string representation for debugging purposes.
   */
  public void printServerState() {
    Logging.log("[-] Server State: \n\t Game Id = " + this.gameId + "\n\t Agents Names: " + this.agentsNames + "\n\t Campaings Ownership: "
        + this.campaignsOwnership + "\n\t Map of Campaigns: " + this.printNiceCampaignTable() + "\n\t Day: " + this.currentDay + "\n\t EndOfDay: "
        + ((this.currentDayEnd != null) ? this.currentDayEnd.toString() : "") +
        // "\n\t BidBundles = " + this.bidBundles +
        "\n\t AdStatistics: " + this.adStatistics + "\n\t Campaign For Auction: " + this.printNiceCampaignForAuctionList());
  }

}
