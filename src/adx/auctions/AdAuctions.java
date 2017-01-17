package adx.auctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import adx.util.Pair;
import adx.util.Parameters;
import adx.util.Sampling;

/**
 * Methods to run second price auctions on bid bundles.
 * 
 * @author Enrique Areyan Viqueira
 */
public class AdAuctions {

  private static Random randomGenerator = new Random();

  private static CompareBidEntries bidComparator = new CompareBidEntries();

  /**
   * Given a Query and a map of BidBundles filter and return those bids that match the query. This method assumes the bid bundle reports campaigns owned by
   * agents, no impostors.
   * 
   * @param query
   * @param bidBundles
   * @return
   * @throws AdXException
   */
  public static List<Pair<String, BidEntry>> filterBids(Query query, Map<String, BidBundle> bidBundles) throws AdXException {
    List<Pair<String, BidEntry>> bids = new ArrayList<Pair<String, BidEntry>>();
    for (Entry<String, BidBundle> agentBid : bidBundles.entrySet()) {
      for (BidEntry bidEntry : agentBid.getValue().getBidEntries()) {
        // First check if the query matches.
        if (bidEntry.getQuery().matchesQuery(query)) {
          bids.add(new Pair<String, BidEntry>(agentBid.getKey(), bidEntry));
        }
      }
    }
    return bids;
  }

  /**
   * A helper function to check that the input of an auction is valid.
   * 
   * @param day
   * @param supply
   * @param query
   * @param bids
   * @param limits
   * @param adStatistics
   * @throws AdXException
   */
  private static void checkAuctionInput(int day, int supply, Query query, List<Pair<String, BidEntry>> bids, Map<Integer, Double> limits,
      AdStatistics adStatistics) throws AdXException {
    if (day < 0) {
      throw new AdXException("Cannot run auction in a negative day");
    }
    if (supply <= 0) {
      throw new AdXException("Cannot run auction withouth at least 1 supply");
    }
    if (query == null) {
      throw new AdXException("Cannot run auction with null query");
    }
    if (bids == null) {
      throw new AdXException("Cannot run auction with null bids");
    }
    if (limits == null) {
      throw new AdXException("Cannot run auction with null daily limits");
    }
    if (adStatistics == null) {
      throw new AdXException("Cannot run auction with null adStatistics");
    }
  }

  /**
   * Runs a classic second auction for supply many users for the given list of bids and limits. Ties are broken at random.
   * 
   * @param supply
   * @param bids
   * @param limits
   * @throws AdXException
   */
  public static void runSecondPriceAuction(int day, Query query, int supply, List<Pair<String, BidEntry>> bids, Map<Integer, Double> limits,
      AdStatistics adStatistics) throws AdXException {
    AdAuctions.checkAuctionInput(day, supply, query, bids, limits, adStatistics);
    Collections.sort(bids, AdAuctions.bidComparator);
    /*
     * Some debug prints Logging.log("Running second price auctions on supply = " + supply); for (Pair<String, BidEntry> x : bids) Logging.log(x.getElement1() +
     * ": " + x.getElement2()); for (Entry<Integer, Double> x : limits.entrySet()) Logging.log("[Campaign " + x.getKey() + ", daily limit = " + x.getValue() +
     * "]");
     */

    // Keep the auction running while there is supply and at least one bidder.
    while (supply > 0 && bids.size() > 0) {
      // Determine winner
      Pair<Double, Pair<String, BidEntry>> winner = AdAuctions.winnerDetermination(bids);
      Double winnerCost = winner.getElement1();
      String winnerName = winner.getElement2().getElement1();
      BidEntry winnerBidEntry = winner.getElement2().getElement2();

      if (!winnerBidEntry.getQuery().matchesQuery(query))
        throw new AdXException("Error while running auction: given bid's query does not match the query being auctioned.");

      // Remove winner from further consideration.
      bids.remove(winner.getElement2());
      Integer winCount;
      Double winCost;
      if (winnerCost > 0.0) {
        double dailyLimit = (limits.containsKey(winnerBidEntry.getCampaignId())) ? limits.get(winnerBidEntry.getCampaignId()) : Double.MAX_VALUE;
        // We can either take the whole supply or as much as allowed by our limits (up to flooring numbers)
        double limitSpend = Math.min(
            supply * winnerCost,
            Math.min(winnerBidEntry.getLimit(),
                dailyLimit - adStatistics.getSummaryStatistic(0, winner.getElement2().getElement1(), winnerBidEntry.getCampaignId()).getElement2()));
        winCount = (int) Math.floor(limitSpend / winnerCost);
        winCost = winCount * winnerCost;
      } else {
        winCount = supply;
        winCost = 0.0;
      }
      // Add the statistics about what this campaign won.
      adStatistics.addStatistic(day, winnerName, winnerBidEntry.getCampaignId(), query, winCount, winCost);
      // Decrement supply
      supply -= winCount;
    }
    // Print for debugging purposes
    // Logging.log(adStatistics);
  }

  /**
   * Given a list of ORDERED bids, returns a pair (price,winner). If more that one winner exists, one is returned at random. This function assumes there is at
   * least one bidder and that the list of bids is already given in descending order of bid.
   * 
   * @param bids
   * @return the winner Entry<String, BidEntry>
   * @throws AdXException
   */
  public static Pair<Double, Pair<String, BidEntry>> winnerDetermination(List<Pair<String, BidEntry>> bids) throws AdXException {
    // If bids is null OR no bids, throw exception.
    if (bids == null || bids.size() == 0) {
      throw new AdXException("To run a winner determination routine we need at least one bid.");
    }
    // If there is only one bidder, then that is the winner.
    if (bids.size() == 1) {
      return new Pair<Double, Pair<String, BidEntry>>(0.0, bids.get(0));
    }
    double winningBid = bids.get(0).getElement2().getBid();
    Iterator<Pair<String, BidEntry>> bidsListIterator = bids.iterator();
    List<Pair<String, BidEntry>> winners = new ArrayList<>();
    Pair<String, BidEntry> currentBidder = null;
    while ((bidsListIterator.hasNext()) && ((currentBidder = bidsListIterator.next()) != null) && currentBidder.getElement2().getBid() == winningBid) {
      winners.add(currentBidder);
    }
    // Logging.log("\t\t List of all winners = " + winners);
    if (winners.size() == 0) {
      throw new AdXException("There has to be at least one winner.");
    } else if (winners.size() == 1) {
      return new Pair<Double, Pair<String, BidEntry>>(currentBidder.getElement2().getBid(), winners.get(0));
    } else {
      return new Pair<Double, Pair<String, BidEntry>>(winners.get(0).getElement2().getBid(), winners.get(randomGenerator.nextInt(winners.size())));
    }
  }

  /**
   * A wrapper function that runs auction on all queries.
   * 
   * @param day
   * @param bidBundles
   * @param adStatistics
   * @throws AdXException
   */
  public static void runAllAuctions(int day, Map<String, BidBundle> bidBundles, AdStatistics adStatistics) throws AdXException {
    HashMap<Query, Integer> samplePopulation = Sampling.samplePopulation(Parameters.POPULATION_SIZE);
    Logging.log(samplePopulation);
    for (Entry<Query, Integer> sample : samplePopulation.entrySet()) {
      Query query = sample.getKey();
      int supply = sample.getValue();
      List<Pair<String, BidEntry>> bids = AdAuctions.filterBids(query, bidBundles);
      Map<Integer, Double> limits = AdAuctions.getCampaingsDailyLimit(day, bidBundles);
      AdAuctions.runSecondPriceAuction(day, query, supply, bids, limits, adStatistics);
    }
  }

  /**
   * Given a map Agent -> BidBundle, returns a map Campaign Id -> Limit
   * 
   * @param day
   * @param bidBundles
   * @return the map containing the daily limit of all campaigns.
   * @throws AdXException
   */
  public static Map<Integer, Double> getCampaingsDailyLimit(int day, Map<String, BidBundle> bidBundles) throws AdXException {
    Map<Integer, Double> limits = new HashMap<Integer, Double>();
    for (BidBundle bidBundle : bidBundles.values()) {
      if (bidBundle.getDay() != day) {
        throw new AdXException("Processing a bid bundle for the WRONG day");
      }
      for (BidEntry bidEntry : bidBundle.getBidEntries()) {
        if (bidBundle.getCampaignLimit(bidEntry.getCampaignId()) != null) {
          limits.put(bidEntry.getCampaignId(), bidBundle.getCampaignLimit(bidEntry.getCampaignId()));
        }
      }
    }
    return limits;
  }

  /**
   * Comparator.
   * 
   * @author Enrique Areyan Viqueira
   */
  public static class CompareBidEntries implements Comparator<Pair<String, BidEntry>> {
    @Override
    public int compare(Pair<String, BidEntry> o1, Pair<String, BidEntry> o2) {
      if (o1.getElement2().getBid() < o2.getElement2().getBid()) {
        return 1;
      } else if (o1.getElement2().getBid() > o2.getElement2().getBid()) {
        return -1;
      } else {
        return 0;
      }
    }
  }

}
