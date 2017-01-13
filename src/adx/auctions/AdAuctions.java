package adx.auctions;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.Query;
import adx.util.Logging;

/**
 * Methods to run second price auctions on bid bundles.
 * 
 * @author Enrique Areyan Viqueira
 */
public class AdAuctions {
  
  private static Random randomGenerator = new Random();
  
  private static CompareBidEntries bidComparator = new CompareBidEntries();

  /**
   * Given a Query and a map of BidBundles filter and return those bids that
   * match the query. This method assumes the bid bundle reports campaigns
   * owned by agents, no impostors.
   * 
   * @param query
   * @param bidBundles
   * @return
   * @throws AdXException
   */
  public static List<Entry<String, BidEntry>> filterBids(Query query, Map<String, BidBundle> bidBundles) throws AdXException {

    Logging.log("\nFiltered bids for query: " + query);
    List<Entry<String, BidEntry>> bids = new ArrayList<Entry<String, BidEntry>>();
    for (Entry<String, BidBundle> agentBid : bidBundles.entrySet()) {
      Logging.log(agentBid.getKey() + " , " + agentBid.getValue());
      for (BidEntry bidEntry : agentBid.getValue().getBidEntries()) {
        // First check if the query matches.
        if (bidEntry.getQuery().matchesQuery(query)) {
            bids.add(new AbstractMap.SimpleEntry<String, BidEntry>(agentBid.getKey(), bidEntry));
        } else {
          Logging.log("[x] Do NOT select this entry: " + bidEntry);
        }
      }
    }
    return bids;
  }

  /**
   * Runs a classic second auction for supply many users for the given list of
   * bids and limits. Ties are broken at random.
   * 
   * @param supply
   * @param bids
   * @param limits
   * @throws AdXException 
   */
  public static void runSecondPriceAuction(Query query, int supply,
      List<Entry<String, BidEntry>> bids, Map<Integer, Double> limits,
      AdStatistics adStatistics) throws AdXException {
    Logging.log("Running second price auctions ");
    Collections.sort(bids, AdAuctions.bidComparator);
    Logging.log(bids);
    
    // Keep the auction running while there is supply and at least one bidder.
    while(supply > 0 && bids.size() > 0) {
      Entry<Double, Entry<String, BidEntry>> winner = AdAuctions.winnerDetermination(bids);
      Logging.log("winner is = " + winner);
      // Remove winner from further consideration.
      bids.remove(winner.getValue());
      // Check two limits: campaigns limits and query limits.
      
      // Add the statistics about what this campaign won.
    }
  }
  
  /**
   * Given a list of ORDERED bids, returns a pair (price,winner). If more that
   * one winner exists, one is returned at random. This function assumes there
   * is at least one bidder and that the list of bids is already given in
   * descending order of bid.
   * 
   * @param bids
   * @return the winner Entry<String, BidEntry>
   * @throws AdXException
   */ 
  public static Entry<Double, Entry<String, BidEntry>> winnerDetermination(List<Entry<String, BidEntry>> bids) throws AdXException {
    // If there is only one bidder, then that is the winner.
    if(bids.size() == 1) {
      return new AbstractMap.SimpleEntry<Double, Entry<String, BidEntry>>(0.0, bids.get(0));
    }
    double winningBid = bids.get(0).getValue().getBid();
    Iterator<Entry<String, BidEntry>> bidsListIterator = bids.iterator();
    List<Entry<String, BidEntry>> winners = new ArrayList<>();
    Entry<String, BidEntry> currentBidder = null;
    while ((bidsListIterator.hasNext())
        && ((currentBidder = bidsListIterator.next()) != null)
        && currentBidder.getValue().getBid() == winningBid) {
      winners.add(currentBidder);
    }
    //Logging.log("\t\t List of all winners = " + winners);
    if (winners.size() == 0) {
      throw new AdXException("There has to be at least one winner.");
    } else if (winners.size() == 1) {
      return new AbstractMap.SimpleEntry<Double, Entry<String, BidEntry>>(currentBidder.getValue().getBid(), winners.get(0));
    } else {
      return new AbstractMap.SimpleEntry<Double, Entry<String, BidEntry>>(winners.get(0).getValue().getBid(), winners.get(randomGenerator.nextInt(winners.size())));
    }
  }
  
  /**
   * Comparator.
   * 
   * @author Enrique Areyan Viqueira
   */
  public static class CompareBidEntries implements Comparator<Entry<String, BidEntry>> {
    @Override
    public int compare(Entry<String, BidEntry> o1, Entry<String, BidEntry> o2) {
      if (o1.getValue().getBid() < o2.getValue().getBid()) {
        return 1;
      } else if (o1.getValue().getBid() > o2.getValue().getBid()) {
        return -1;
      } else {
        return 0;
      }
    }
  }

}
