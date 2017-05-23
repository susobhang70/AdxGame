package adx.sim.agents;

import java.util.HashSet;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.SimpleBidEntry;
import adx.util.Logging;
import adx.variants.onedaygame.OneDayBidBundle;

/**
 * Simple simAgent.
 * 
 * @author Enrique Areyan Viqueira
 */
public class SimpleSimAgent extends SimAgent {

  public SimpleSimAgent(String simAgentName) {
    super(simAgentName);
  }

  @Override
  public BidBundle getBidBundle() {
    try {
      // Bidding only on the exact market segment of the campaign.
      Set<SimpleBidEntry> bidEntries = new HashSet<SimpleBidEntry>();
      bidEntries.add(new SimpleBidEntry(this.myCampaign.getMarketSegment(), this.myCampaign.getBudget() / (double) this.myCampaign.getReach(), this.myCampaign.getBudget()));
      // Logging.log("[-] bidEntries = " + bidEntries);
      // The bid bundle indicates the campaign id, the limit across all auctions, and the bid entries.
      return new OneDayBidBundle(this.myCampaign.getId(), this.myCampaign.getBudget(), bidEntries);
    } catch (AdXException e) {
      Logging.log("[x] Something went wrong getting the bid bundle: " + e.getMessage());
      return null;
    }
  }

}
