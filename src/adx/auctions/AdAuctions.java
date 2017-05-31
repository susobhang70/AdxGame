package adx.auctions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import adx.exceptions.AdXException;
import adx.statistics.Statistics;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.MarketSegment;
import adx.structures.Query;
import adx.util.Pair;
import adx.util.Parameters;
import adx.util.Sampling;

public class AdAuctions {

  protected static CompareBidEntries bidComparator = new CompareBidEntries();

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
        // Check if the query matches.
        if (bidEntry.getQuery().matchesQuery(query)) {
          bids.add(new Pair<String, BidEntry>(agentBid.getKey(), bidEntry));
        }
      }
    }
    return bids;
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
   * Comparator. Compares two bid entries by the bids. This is used to order a list of bid entries in descending order of bid.
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

  /**
   * Runs all the auctions for a given day.
   * 
   * @param day
   * @param bidBundles
   * @param adStatistics
   * @throws AdXException
   */
  public static void runAllAuctions(int day, Map<String, BidBundle> bidBundles, Statistics adStatistics) throws AdXException {
    // Get the daily limits
    Map<Integer, Double> dailyLimits = AdAuctions.getCampaingsDailyLimit(day, bidBundles);
    // Collect bids.
    Map<Query, StandingBids> allQueriesStandingBids = new HashMap<Query, StandingBids>();
    for (MarketSegment marketSegment : Sampling.segmentsToSample.keySet()) {
      Query query = new Query(marketSegment);
      allQueriesStandingBids.put(query, new StandingBids(AdAuctions.filterBids(query, bidBundles)));
    }
    // Sample user population.
    List<Query> samplePopulation = Sampling.samplePopulation(Parameters.POPULATION_SIZE);
    // Debug print.
    // Logging.log(allQueriesStandingBids);
    // Logging.log(dailyLimits);
    // Logging.log(samplePopulation);
    // For each user sampled, run the auction.
    for (Query query : samplePopulation) {
      StandingBids bidsForCurrentQuery = allQueriesStandingBids.get(query);
      // Logging.log("Auction: \t " + query);
      // Logging.log("\t -> " + bidsForCurrentQuery);
      // Attempt to allocate the user.
      while (true) {
        Pair<String, BidEntry> winner = bidsForCurrentQuery.getWinner();
        if (winner == null) {
          // There is no bidder for this query, thus nothing to allocate.
          break;
        }
        String winnerName = winner.getElement1();
        BidEntry winnerBidEntry = winner.getElement2();
        double winCost = bidsForCurrentQuery.getWinnerCost();
        // Logging.log("Winner is: " + winner + ", pays " + winCost);
        double totalSpendSoFar = adStatistics.getStatisticsAds().getDailySummaryStatistic(day, winnerName, winnerBidEntry.getCampaignId()).getElement2();
        double querySpendSoFar = adStatistics.getStatisticsAds().getDailyStatistic(day, winnerName, winnerBidEntry.getCampaignId(), query).getElement2();
        double dailyLimit = (dailyLimits.containsKey(winnerBidEntry.getCampaignId())) ? dailyLimits.get(winnerBidEntry.getCampaignId()) : Double.MAX_VALUE;

        if (totalSpendSoFar + winCost > dailyLimit) {
          // In case the campaign already hit its daily limit, delete all bid entries matching this campaigns from ALL standing bids in ALL queries.
          for (StandingBids queryStandingBids : allQueriesStandingBids.values()) {
            queryStandingBids.deleteBidFromCampaign(winnerBidEntry.getCampaignId());
          }
          // Logging.log("DELETED BID FROM ALL QUERIES");
        } else if (querySpendSoFar + winCost > winnerBidEntry.getLimit()) {
          // In case the campaign hit its query limit but not its daily limit, deleted only from the current query's standing bids.
          bidsForCurrentQuery.deleteBid(winner);
          // Logging.log("DELETED BID FROM QUERY: " + query);
        } else {
          // In case the campaign still has budget (both query and daily), allocate the user to this campaign.
          adStatistics.getStatisticsAds().addStatistic(day, winnerName, winnerBidEntry.getCampaignId(), query, 1, winCost);
          break;
        }
      }
    }
    //Logging.log(adStatistics.getStatisticsAds().printNiceAdStatisticsTable());
  }
}
