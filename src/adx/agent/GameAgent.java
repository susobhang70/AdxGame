package adx.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.messages.EndOfDayMessage;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.Campaign;
import adx.structures.Query;
import adx.util.Logging;

/**
 * This class represents a simple agent for the game.
 * 
 * @author Enrique Areyan Viqueira
 */
public class GameAgent extends Agent {

  /**
   * Current simulated day.
   */
  protected int currentDay;
  /**
   * A map that keeps track of the campaigns owned by the agent.
   */
  protected List<Campaign> myCampaigns;

  /**
   * A map that keeps track of campaign opportunities.
   */
  protected List<Campaign> campaignOpportunity;

  /**
   * Keeps the current quality score received by the server.
   */
  protected Double currentQualityScore;

  /**
   * Empty constructor. For testing purposes only.
   */
  public GameAgent() {
    this.init();
  }

  /**
   * Constructor.
   * 
   * @param host - on which the agent will try to connect.
   * @param port - the agent will use for the connection.
   */
  public GameAgent(String host, int port) {
    super(host, port);
    this.init();
  }

  public void init() {
    this.myCampaigns = new ArrayList<Campaign>();
  }

  /**
   * Return the list of active campaigns.
   * 
   * @return the list of active campaigns.
   */
  public List<Campaign> getActiveCampaigns() {
    List<Campaign> activeCampaigns = new ArrayList<Campaign>();
    for (Campaign c : this.myCampaigns) {
      if (this.currentDay >= c.getStartDay() && this.currentDay <= c.getEndDay()) {
        activeCampaigns.add(c);
      }
    }
    return activeCampaigns;
  }

  /**
   * Parse the end of day message.
   */
  @Override
  public void handleEndOfDayMessage(EndOfDayMessage endOfDayMessage) {
    Logging.log("[-] handleEndOfDayMessage " + endOfDayMessage);
    Logging.log("[-] Current time = " + Instant.now());
    try {
      this.currentDay = endOfDayMessage.getDay();
      this.campaignOpportunity = endOfDayMessage.getCampaignsForAuction();
      if (endOfDayMessage.getCampaignsWon() != null) {
        this.myCampaigns.addAll(endOfDayMessage.getCampaignsWon());
      }
      this.currentQualityScore = endOfDayMessage.getQualityScore();
      BidBundle bidBundle = this.getAdBid();
      if (bidBundle != null && this.getClient() != null && this.getClient().isConnected()) {
        this.getClient().sendTCP(bidBundle);
      }
    } catch (AdXException e) {
      Logging.log("[x] Something went wrong getting the bid bundle for day " + endOfDayMessage.getDay() + " -> " + e.getMessage());
    }
  }

  /**
   * Computes the campaign bids.
   * 
   * @return a map with campaign bids.
   */
  protected Map<Integer, Double> getCampaignBids() {
    Map<Integer, Double> campaignBids = new HashMap<Integer, Double>();
    if (this.campaignOpportunity != null) {
      for (Campaign camp : this.campaignOpportunity) {
        campaignBids.put(camp.getId(), new Double((camp.getReach() * 0.1) / this.currentQualityScore));
      }
    } else {
      Logging.log("[-] No campaign opportunites present for day " + this.currentDay);
    }
    return campaignBids;
  }

  /**
   * Produces and sends a bidbundle for the given day.
   * 
   * @param day
   *          - the day for which we want a bid bundle
   * @throws AdXException
   *           in case something went wrong creating the bid bundle.
   */
  public BidBundle getAdBid() throws AdXException {
    // Get the list of active campaigns for this day.
    Set<BidEntry> bidEntries = new HashSet<BidEntry>();
    Map<Integer, Double> limits = new HashMap<Integer, Double>();
    // If I have a campaign, prepare and send bid bundle.
    List<Campaign> myActiveCampaigns = this.getActiveCampaigns();
    if (myActiveCampaigns != null && myActiveCampaigns.size() > 0) {
      Logging.log("[-] Preparing and sending Ad Bid for day " + this.currentDay);
      Logging.log("[-] Active campaigns are: " + myActiveCampaigns);
      for (Campaign c : myActiveCampaigns) {
        BidEntry bidEntry = new BidEntry(c.getId(), new Query(c.getMarketSegment()), c.getBudget() / c.getReach(), c.getBudget());
        bidEntries.add(bidEntry);
        limits.put(c.getId(), c.getBudget());
      }
    } else {
      Logging.log("[-] No campaings present on day " + this.currentDay);
    }
    return new BidBundle(this.currentDay, bidEntries, limits, this.getCampaignBids());
  }

  /**
   * Agent's main method.
   * 
   * @param args
   */
  public static void main(String[] args) {
    GameAgent agent = new GameAgent("localhost", 9898);
    agent.connect("agent0", "123456");
  }

  public String printNiceListMyCampaigns() {
    String ret = "";
    if (this.myCampaigns.size() > 0) {
      for (Campaign c : this.myCampaigns) {
        ret += "\n\t\t" + c;
      }
    } else {
      ret += "No campaigns registered for this agent.";
    }
    return ret;
  }

  public String toString() {
    return "Agent: " + this.agentName + "\n\tMy Campaigns: " + this.printNiceListMyCampaigns();
  }

}
