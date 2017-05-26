package adx.auctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import adx.exceptions.AdXException;
import adx.statistics.Statistics;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.Query;
import adx.util.InputValidators;
import adx.util.Logging;
import adx.util.Pair;
import adx.util.Parameters;
import adx.util.Sampling;

/**
 * Methods to run second price auctions on bid bundles.
 * 
 * @author Enrique Areyan Viqueira
 */
public class OLD_AdAuctions {

  /**
   * Runs a classic second auction for supply many users for the given list of bids and limits. Ties are broken at random.
   * 
   * @param supply
   * @param bids
   * @param limits
   * @throws AdXException
   */
  public static void runSecondPriceAuction(int day, Query query, int supply, List<Pair<String, BidEntry>> bids, Map<Integer, Double> limits,
      Statistics adStatistics) throws AdXException {
    InputValidators.validateDay(day);
    InputValidators.validateSupply(supply);
    InputValidators.validateNotNull(query);
    InputValidators.validateNotNull(bids);
    InputValidators.validateNotNull(limits);
    InputValidators.validateNotNull(adStatistics);
    Collections.sort(bids, AdAuctions.bidComparator);
    int winCount;
    // Keep the auction running while there is supply and at least one bidder.
    while (supply > 0 && bids.size() > 0) {
      // Determine winner
      Pair<Double, List<Pair<String, BidEntry>>> winnerPairList = OLD_AdAuctions.winnerDetermination(bids);
      winCount = OLD_AdAuctions.auctionAllocation(day, query, winnerPairList.getElement1(), winnerPairList, bids, limits, adStatistics);
      supply -= winCount;
    }
    // Print for debugging purposes
    // Logging.log(adStatistics);
  }

  /**
   * Allocation function.
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
  private static int auctionAllocation(int day, Query query, Double winCost, Pair<Double, List<Pair<String, BidEntry>>> winnerPairList,
      List<Pair<String, BidEntry>> bids, Map<Integer, Double> limits, Statistics adStatistics) throws AdXException {
    // Shuffle the list of winners.
    List<Pair<String, BidEntry>> winnerList = winnerPairList.getElement2();
    Collections.shuffle(winnerList);

    BidEntry winnerBidEntry = winnerList.get(0).getElement2();
    String winnerName = winnerList.get(0).getElement1();
    Double dailyLimit = (limits.containsKey(winnerBidEntry.getCampaignId())) ? limits.get(winnerBidEntry.getCampaignId()) : Double.MAX_VALUE;
    Double totalSpendSoFar = 0.0;
    if (adStatistics.getStatisticsAds().getDailySummaryStatistic(day, winnerName, winnerBidEntry.getCampaignId()) != null) {
      totalSpendSoFar = adStatistics.getStatisticsAds().getDailySummaryStatistic(day, winnerName, winnerBidEntry.getCampaignId()).getElement2();
    }
    Double querySpendSoFar = adStatistics.getStatisticsAds().getDailyStatistic(day, winnerName, winnerBidEntry.getCampaignId(), query).getElement2();
    if (totalSpendSoFar + winCost <= dailyLimit && querySpendSoFar + winCost <= winnerBidEntry.getLimit()) {
      // This bidder is allowed to take one more, allocate one more to him.
      adStatistics.getStatisticsAds().addStatistic(day, winnerName, winnerBidEntry.getCampaignId(), query, 1, winCost);
      return 1;
    } else {
      // Limits have been reach, remove this bidder.
      bids.remove(winnerList.get(0));
      return 0;
    }
  }

  /**
   * Given a list of ORDERED bids, returns a pair (price, list of winners). This function assumes there is at least one bidder and that the list of bids is
   * already given in descending order of bid.
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
    // Keep adding bidders to the winnerList as long as their bids match the winning bid.
    while ((bidsListIterator.hasNext()) && ((currentBidder = bidsListIterator.next()) != null) && currentBidder.getElement2().getBid() == winningBid) {
      winnerList.add(currentBidder);
    }
    if (winnerList.size() == 0) {
      throw new AdXException("There has to be at least one winner.");
    }
    // If there is only one winner, then the payment is that of the second bidder (in currentBidder).
    // Otherwise, there is a tie for first place which means the payment is the winningBid.
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
    HashMap<Query, Integer> samplePopulation = Sampling.sampleAndBucketPopulation(Parameters.POPULATION_SIZE);
    Logging.log("[-] Population sampled = " + samplePopulation);
    for (Entry<Query, Integer> sample : samplePopulation.entrySet()) {
      Query query = sample.getKey();
      int supply = sample.getValue();
      List<Pair<String, BidEntry>> bids = AdAuctions.filterBids(query, bidBundles);
      // Logging.log("\t filteredBids = " + bids);
      Map<Integer, Double> limits = AdAuctions.getCampaingsDailyLimit(day, bidBundles);
      OLD_AdAuctions.runSecondPriceAuction(day, query, supply, bids, limits, adStatistics);
    }
  }

}
