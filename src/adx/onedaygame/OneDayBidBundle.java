package adx.onedaygame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.BidEntry;

/**
 * A specialized object for the OneDay game.
 * 
 * @author Enrique Areyan Viqueira
 */
public class OneDayBidBundle extends BidBundle {

  /**
   * A bid bundle is composed of bid entries.
   */
  protected Set<OneDayBidEntry> bidEntries;

  /**
   * Constructor.
   */
  public OneDayBidBundle() {
    super();
  }

  /**
   * Constructor for a bidbundle to play only one day games.
   * 
   * @param bidEntries
   * @param campaignsLimits
   * @throws AdXException
   */
  public OneDayBidBundle(int campaignId, double limit, Set<OneDayBidEntry> oneDayBidEntries) throws AdXException {
    super(1, OneDayBidBundle.createBidEntries(campaignId, oneDayBidEntries), OneDayBidBundle.createLimits(campaignId, limit), null);
  }

  /**
   * Wrapper function - Given a campaignId and a limit, create a map.
   * 
   * @param campaignId
   * @param limit
   * @return
   */
  private static Map<Integer, Double> createLimits(int campaignId, double limit) {
    Map<Integer, Double> limits = new HashMap<Integer, Double>();
    limits.put(campaignId, limit);
    return limits;
  }

  /**
   * Wrapper function - Given a set of OneDayBidEntry, creates a set of BidEntry.
   * 
   * @param campaignId
   * @param oneDayBidEntries
   * @return
   * @throws AdXException
   */
  private static Set<BidEntry> createBidEntries(int campaignId, Set<OneDayBidEntry> oneDayBidEntries) throws AdXException {
    Set<BidEntry> bidEntries = new HashSet<BidEntry>();
    for (OneDayBidEntry oneDayBidEntry : oneDayBidEntries) {
      bidEntries.add(new BidEntry(campaignId, oneDayBidEntry.getQuery(), oneDayBidEntry.getBid(), oneDayBidEntry.getLimit()));
    }
    return bidEntries;
  }

}
