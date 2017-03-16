package adx.onedaygame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.Query;
import adx.util.Logging;

/**
 * An example of a simple agent playing the OneDay game.
 * 
 * @author Enrique Areyan Viqueira
 */
public class SimpleOneDayAgent extends OneDayAgent {

  /**
   * Constructor.
   * 
   * @param host
   * @param port
   */
  public SimpleOneDayAgent(String host, int port) {
    super(host, port);
  }

  @Override
  protected BidBundle getBidBundle() {
    Set<BidEntry> bidEntries = new HashSet<BidEntry>();
    // Limits are just given by the campaign.
    Map<Integer, Double> limits = new HashMap<Integer, Double>();
    limits.put(this.myCampaign.getId(), this.myCampaign.getBudget());
    try {
      // Bidding only on the exact market segment of the campaign.
      bidEntries.add(new BidEntry(this.myCampaign.getId(), new Query(this.myCampaign.getMarketSegment()), 0.0, this.myCampaign.getBudget()));
      Logging.log("[-] bidEntries = " + bidEntries);
      return new BidBundle(bidEntries, limits);
    } catch (AdXException e) {
      Logging.log("[x] Something went wrong getting the bid bundle " + e.getMessage());
      return null;
    }
  }

  /**
   * Agent's main method.
   * 
   * @param args
   */
  public static void main(String[] args) {
    SimpleOneDayAgent agent = new SimpleOneDayAgent("localhost", 9898);
    agent.connect("agent0", "123456");
  }
}
