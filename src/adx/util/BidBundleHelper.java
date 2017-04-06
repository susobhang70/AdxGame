package adx.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.structures.BidEntry;
import adx.structures.SimpleBidEntry;

/**
 * A library with static methods to perform operations on bidbundles.
 * 
 * @author Enrique Areyan Viqueira
 */
public class BidBundleHelper {

  /**
   * Wrapper function - Given a campaignId and a limit, create a map.
   * 
   * @param campaignId
   * @param limit
   * @return
   */
  public static Map<Integer, Double> createLimits(int campaignId, double limit) {
    Map<Integer, Double> limits = new HashMap<Integer, Double>();
    limits.put(campaignId, limit);
    return limits;
  }
  
  /**
   * Wrapper function - Given a set of SimpleBidEntry, creates a set of BidEntry.
   * 
   * @param campaignId
   * @param simpleBidEntries
   * @return
   * @throws AdXException
   */
  public static Set<BidEntry> createBidEntries(int campaignId, Set<SimpleBidEntry> simpleBidEntries) throws AdXException {
    Set<BidEntry> bidEntries = new HashSet<BidEntry>();
    for (SimpleBidEntry oneDayBidEntry : simpleBidEntries) {
      bidEntries.add(new BidEntry(campaignId, oneDayBidEntry.getQuery(), oneDayBidEntry.getBid(), oneDayBidEntry.getLimit()));
    }
    return bidEntries;
  }

}
