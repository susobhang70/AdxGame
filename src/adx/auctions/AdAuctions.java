package adx.auctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.Query;
import adx.util.InputValidators;
import adx.util.Pair;
import adx.util.Parameters;
import adx.util.Sampling;

/**
 * Methods to run second price auctions on bid bundles.
 * 
 * @author Enrique Areyan Viqueira
 */
public class AdAuctions {

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
   * Runs a classic second auction for supply many users for the given list of bids and limits. Ties are broken at random.
   * 
   * @param supply
   * @param bids
   * @param limits
   * @throws AdXException
   */
  public static void runSecondPriceAuction(int day, Query query, int supply, List<Pair<String, BidEntry>> bids, Map<Integer, Double> limits, Statistics adStatistics) throws AdXException {
    InputValidators.validateDay(day);
    InputValidators.validateSupply(supply);
    InputValidators.validateNotNull(query);
    InputValidators.validateNotNull(bids);
    InputValidators.validateNotNull(limits);
    InputValidators.validateNotNull(adStatistics);
    Collections.sort(bids, AdAuctions.bidComparator);
    // Keep the auction running while there is supply and at least one bidder.
    while (supply > 0 && bids.size() > 0) {
      // Determine winner
      Pair<Double, List<Pair<String, BidEntry>>> winnerPairList = AdAuctions.winnerDetermination(bids);
      int winCount;
      // There was only one winner.
      if (winnerPairList.getElement2().size() == 1) {
        winCount = AdAuctions.uniqueWinner(day, supply, query, winnerPairList.getElement1(), winnerPairList.getElement2().get(0), bids, limits, adStatistics);
      } else {
        winCount = AdAuctions.multipleWinners(day, query, winnerPairList.getElement1(), winnerPairList, bids, limits, adStatistics);
      }
      // Decrement supply according to how much was allocated.
      supply -= winCount;
    }
    // Print for debugging purposes
    //Logging.log(adStatistics);
  }

  /**
   * Handles the case where there are multiple winners in the auction. Here we give one impression at the time.
   * 
   * @param day
   * @param query
   * @param winCost
   * @param winnerPairList
   * @param bids
   * @param limits
   * @param adStatistics
   * @return
   * @throws AdXException
   */
  private static int multipleWinners(int day, Query query, Double winCost, Pair<Double, List<Pair<String, BidEntry>>> winnerPairList,
      List<Pair<String, BidEntry>> bids, Map<Integer, Double> limits, Statistics adStatistics) throws AdXException {
    // There are at least two winners. We will allocate only one chosen at random.
    List<Pair<String, BidEntry>> winnerList = winnerPairList.getElement2();
    Collections.shuffle(winnerList);
    //Logging.log("\tChoosen, random, winner = " + winnerList.get(0));
    BidEntry winnerBidEntry = winnerList.get(0).getElement2();
    String winnerName = winnerList.get(0).getElement1();
    Double dailyLimit = (limits.containsKey(winnerBidEntry.getCampaignId())) ? limits.get(winnerBidEntry.getCampaignId()) : Double.MAX_VALUE;
    Double totalSpendSoFar = 0.0;
    if(adStatistics.getSummaryStatistic(day, winnerName, winnerBidEntry.getCampaignId()) != null) {
      totalSpendSoFar = adStatistics.getSummaryStatistic(day, winnerName, winnerBidEntry.getCampaignId()).getElement2();
    }
    Double querySpendSoFar = adStatistics.getStatistic(day, winnerName, winnerBidEntry.getCampaignId(), query).getElement2();
    if (totalSpendSoFar + winCost <= dailyLimit && querySpendSoFar + winCost <= winnerBidEntry.getLimit()) {
      // This guy is allowed to take one more, allocate one more to him.
      adStatistics.addStatistic(day, winnerName, winnerBidEntry.getCampaignId(), query, 1, winCost);
      // Can this guy still be on the auction? We know that winCost will be the winCost for this guy in the future
      // So we can just check if he can buy one more
      if (totalSpendSoFar + 2 * winCost > dailyLimit || querySpendSoFar + 2 * winCost > winnerBidEntry.getLimit()) {
        bids.remove(winnerList.get(0));
      }
      return 1;
    } else {
      // Limits have been reach, remove this bidder.
      bids.remove(winnerList.get(0));
      return 0;
    }
  }

  /**
   * Handles the case where there is a unique winner in the auction. In this case we can allocate a chunk of impressions at a time.
   * 
   * @param day
   * @param supply
   * @param query
   * @param winnerCost
   * @param winner
   * @param bids
   * @param limits
   * @param adStatistics
   * @return
   * @throws AdXException
   */
  private static int uniqueWinner(int day, int supply, Query query, Double winnerCost, Pair<String, BidEntry> winner, List<Pair<String, BidEntry>> bids,
      Map<Integer, Double> limits, Statistics adStatistics) throws AdXException {
    // Remove winner from further consideration.
    //Logging.log("Unique winner = " + winner);
    bids.remove(winner);
    String winnerName = winner.getElement1();
    BidEntry winnerBidEntry = winner.getElement2();
    if (!winnerBidEntry.getQuery().matchesQuery(query))
      throw new AdXException("Error while running auction: given bid's query does not match the query being auctioned.");
    Integer winCount;
    Double winCost;
    if (winnerCost > 0.0) {
      double dailyLimit = (limits.containsKey(winnerBidEntry.getCampaignId())) ? limits.get(winnerBidEntry.getCampaignId()) : Double.MAX_VALUE;
      // We can either take the whole supply or as much as allowed by our limits (up to flooring numbers)
      double limitSpend = Math.min(supply * winnerCost,
          Math.min(winnerBidEntry.getLimit(), dailyLimit - adStatistics.getSummaryStatistic(0, winnerName, winnerBidEntry.getCampaignId()).getElement2()));
      winCount = (int) Math.floor(limitSpend / winnerCost);
      winCost = winCount * winnerCost;
    } else {
      winCount = supply;
      winCost = 0.0;
    }
    //Logging.log("supply = " + supply);
    //Logging.log("day = " + day + ", winnerName = " + winnerName + ", id = " + winnerBidEntry.getCampaignId() + ", query = " + query + ", winCount = " +
    // winCount + ", winCost = " + winCost);
    // Add the statistics about what this campaign won.
    adStatistics.addStatistic(day, winnerName, winnerBidEntry.getCampaignId(), query, winCount, winCost);
    return winCount;
  }

  /**
   * Given a list of ORDERED bids, returns a pair (price,winner). If more that one winner exists, one is returned at random. This function assumes there is at
   * least one bidder and that the list of bids is already given in descending order of bid.
   * 
   * @param bids
   * @return the winner Entry<String, BidEntry>
   * @throws AdXException
   */
  public static Pair<Double, List<Pair<String, BidEntry>>> winnerDetermination(List<Pair<String, BidEntry>> bids) throws AdXException {
    // If bids is null OR no bids, throw exception.
    if (bids == null || bids.size() == 0) {
      throw new AdXException("To run a winner determination routine we need at least one bid.");
    }
    ArrayList<Pair<String, BidEntry>> winnerList = new ArrayList<Pair<String, BidEntry>>();
    // If there is only one bidder, then that is the winner at a winning cost of 0.0.
    if (bids.size() == 1) {
      winnerList.add(bids.get(0));
      return new Pair<Double, List<Pair<String, BidEntry>>>(0.0, winnerList);
    }
    double winningBid = bids.get(0).getElement2().getBid();
    Iterator<Pair<String, BidEntry>> bidsListIterator = bids.iterator();
    Pair<String, BidEntry> currentBidder = null;
    while ((bidsListIterator.hasNext()) && ((currentBidder = bidsListIterator.next()) != null) && currentBidder.getElement2().getBid() == winningBid) {
      winnerList.add(currentBidder);
    }
    if (winnerList.size() == 0) {
      throw new AdXException("There has to be at least one winner.");
    }
    Double winningCost = (winnerList.size() == 1) ? currentBidder.getElement2().getBid() : winningBid;
    return new Pair<Double, List<Pair<String, BidEntry>>>(winningCost, winnerList);
  }

  /**
   * A wrapper function that runs auction on all queries.
   * 
   * @param day
   * @param bidBundles
   * @param adStatistics
   * @throws AdXException
   */
  public static void runAllAuctions(int day, Map<String, BidBundle> bidBundles, Statistics adStatistics) throws AdXException {
    HashMap<Query, Integer> samplePopulation = Sampling.samplePopulation(Parameters.POPULATION_SIZE);
    //Logging.log(samplePopulation);
    for (Entry<Query, Integer> sample : samplePopulation.entrySet()) {
      Query query = sample.getKey();
      int supply = sample.getValue();
      List<Pair<String, BidEntry>> bids = AdAuctions.filterBids(query, bidBundles);
      //Logging.log("\t filteredBids = " + bids);
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
