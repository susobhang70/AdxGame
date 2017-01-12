package adx.agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import adx.messages.ConnectServerMessage;
import adx.messages.EndOfDayMessage;
import adx.messages.InitialMessage;
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
  
  protected Map<Integer, Campaign> myCampaigns;

  public GameAgent(String host, int port) {
    super(host, port);
    this.myCampaigns = new HashMap<Integer, Campaign>();
  }

  @Override
  protected void handleInitialMessage(InitialMessage initialMessage) {
    Logging.log("[-] handleInitialMessage");
    Campaign initialCampaign = initialMessage.getInitialCampaign();
    Logging.log(initialCampaign);
    this.myCampaigns.put(0, initialCampaign);
  }

  @Override
  protected void handleEndOfDayMessage(EndOfDayMessage endOfDayMessage) {
    Logging.log("[-] handleEndOfDayMessage " + endOfDayMessage);
    this.getAdBid(endOfDayMessage.getDay());
  }
  
  /**
   * Produces and sends a bidbundle for the given day.
   * 
   * @param day
   */
  protected void getAdBid(int day) {
    //Get my active campaign for this day.
    Campaign c = this.myCampaigns.get(day);
    //If I have a campaign, prepare and send bid bundle.
    if(this.myCampaigns.containsKey(day)) {
      Logging.log("Preparing and sending Ad Bid for day " + day + ", and campaign " + c);
      BidEntry bidEntry = new BidEntry(c.getId(), new Query(c.getMarketSegment()), c.getBudget() / c.getReach(), c.getBudget());
      Set<BidEntry> bidEntries = new HashSet<BidEntry>();
      bidEntries.add(bidEntry);
      Map<Integer, Double> limits = new HashMap<Integer, Double>();
      limits.put(c.getId(), 200.0);
      BidBundle bidBundle = new BidBundle(day, bidEntries, limits);
      this.getClient().sendTCP(bidBundle);
    } else {
      Logging.log("No campaings present on day " + day);
    }
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
      request.setAgentName("enrique2");
      request.setAgentPassword("123456");
      agent.getClient().sendTCP(request);
      while (true);
    } catch (Exception e) {
      Logging.log("[x] Error trying to connect to the server!");
      e.printStackTrace();
    }
  }

}
