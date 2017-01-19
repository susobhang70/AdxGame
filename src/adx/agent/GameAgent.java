package adx.agent;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.messages.ConnectServerMessage;
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
   * A map that keeps track of the campaigns owned by the agent.
   */
  protected Map<Integer, List<Campaign>> myCampaigns;

  /**
   * A map that keeps track of campaigns owned by other agents.
   */
  protected Map<Integer, Campaign> theirCampaigns;

  /**
   * A map that keeps track of campaign opportunities.
   */
  protected List<Campaign> campaignOpportunity;

  /**
   * Keeps the current quality score received by the server.
   */
  protected Double currentQualityScore;

  /**
   * Constructor.
   * 
   * @param host
   *          - on which the agent will try to connect.
   * @param port
   *          - the agent will use for the connection.
   */
  public GameAgent(String host, int port) {
    super(host, port);
    this.myCampaigns = new HashMap<Integer, List<Campaign>>();
  }

  @Override
  protected void handleEndOfDayMessage(EndOfDayMessage endOfDayMessage) {
    Logging.log("[-] handleEndOfDayMessage " + endOfDayMessage);
    Logging.log("[-] Current time = " + Instant.now());
    try {
      this.campaignOpportunity = endOfDayMessage.getCampaignsForAuction();
      this.myCampaigns.put(endOfDayMessage.getDay(), endOfDayMessage.getCampaignsWon());
      this.currentQualityScore = endOfDayMessage.getQualityScore();
      BidBundle bidBundle = this.getAdBid(endOfDayMessage.getDay());
      if (bidBundle != null) {
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
    for (Campaign camp : this.campaignOpportunity) {
      campaignBids.put(camp.getId(), new Double((camp.getReach() * 0.1) / this.currentQualityScore));
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
  protected BidBundle getAdBid(int day) throws AdXException {
    // Get the list of active campaigns for this day.
    List<Campaign> myList = this.myCampaigns.get(day);
    Set<BidEntry> bidEntries = new HashSet<BidEntry>();
    Map<Integer, Double> limits = new HashMap<Integer, Double>();
    // If I have a campaign, prepare and send bid bundle.
    if (myList != null) {
      Logging.log("\t[-] Preparing and sending Ad Bid for day " + day);
      for (Campaign c : myList) {
        BidEntry bidEntry = new BidEntry(c.getId(), new Query(c.getMarketSegment()), c.getBudget() / c.getReach(), c.getBudget());
        bidEntries.add(bidEntry);
        limits.put(c.getId(), c.getBudget());
      }
    } else {
      Logging.log("\t[-] No campaings present on day " + day);
    }
    return new BidBundle(day, bidEntries, limits, this.getCampaignBids());
  }

  /**
   * Agent's main method.
   * 
   * @param args
   */
  public static void main(String[] args) {
    GameAgent agent = new GameAgent("localhost", 9898);
    try {
      ConnectServerMessage request = new ConnectServerMessage();
      request.setAgentName("agent1");
      request.setAgentPassword("123456");
      agent.getClient().sendTCP(request);
      while (true)
        ;
    } catch (Exception e) {
      Logging.log("[x] Error trying to connect to the server!");
      e.printStackTrace();
    }
  }

}
