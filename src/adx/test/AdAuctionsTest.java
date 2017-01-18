package adx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import adx.auctions.AdAuctions;
import adx.auctions.AdStatistics;
import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.MarketSegment;
import adx.structures.Query;
import adx.util.Pair;

import com.google.common.collect.Table;

public class AdAuctionsTest {

  @Test
  public void testFilterBids() throws AdXException {
    Table<Integer, String, BidBundle> bidBundles = BidBundleTest.getTableBidBundles();
    // Day 0 bid bundles.
    assertEquals(AdAuctions.filterBids(new Query(MarketSegment.FEMALE_YOUNG_HIGH_INCOME), bidBundles.row(0)).size(), 4);
    assertEquals(AdAuctions.filterBids(new Query(MarketSegment.MALE_OLD_HIGH_INCOME), bidBundles.row(0)).size(), 1);
    assertEquals(AdAuctions.filterBids(new Query(MarketSegment.OLD_LOW_INCOME), bidBundles.row(0)).size(), 0);
    assertEquals(AdAuctions.filterBids(new Query(MarketSegment.YOUNG), bidBundles.row(0)).size(), 2);
    assertEquals(AdAuctions.filterBids(new Query(MarketSegment.OLD), bidBundles.row(0)).size(), 0);
    assertEquals(AdAuctions.filterBids(new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME), bidBundles.row(0)).size(), 4);
    assertEquals(AdAuctions.filterBids(new Query(MarketSegment.MALE_YOUNG_HIGH_INCOME), bidBundles.row(0)).size(), 4);
  }

  @Test
  public void testRunSecondPriceAuction() throws AdXException {

    Table<Integer, String, BidBundle> bidBundles = BidBundleTest.getTableBidBundles();
    List<Pair<String, BidEntry>> filteredBids = AdAuctions.filterBids(new Query(MarketSegment.MALE_YOUNG_HIGH_INCOME), bidBundles.row(0));
    AdStatistics adStatistics = AdStatisticsTest.getAdStatistics();
    Map<Integer, Double> limits = new HashMap<Integer, Double>();
    limits.put(3, 3250.0);
    Query query = new Query(MarketSegment.MALE_YOUNG_HIGH_INCOME);
    AdAuctions.runSecondPriceAuction(0, query, 10, filteredBids, limits, adStatistics);
  }

  @Test
  public void testRunSecondPriceAuction1() throws AdXException {
    try {
      AdAuctions.runSecondPriceAuction(-1, null, -1, null, null, null);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    }
    try {
      AdAuctions.runSecondPriceAuction(0, null, -1, null, null, null);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    }
    try {
      AdAuctions.runSecondPriceAuction(0, new Query(MarketSegment.FEMALE), -1, null, null, null);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    }
    try {
      AdAuctions.runSecondPriceAuction(0, new Query(MarketSegment.FEMALE), 100, null, null, null);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    }
    // Query to auction
    Query query = new Query(MarketSegment.MALE_OLD_LOW_INCOME);
    HashMap<Integer, Double> limits = new HashMap<Integer, Double>();
    List<Pair<String, BidEntry>> bids = new ArrayList<Pair<String, BidEntry>>();

    bids.add(new Pair<String, BidEntry>("agent0", new BidEntry(1, query, 1, 1)));

    AdStatistics adStatistics = AdStatisticsTest.getAdStatistics();
    AdAuctions.runSecondPriceAuction(0, query, 10, bids, limits, adStatistics);
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, query).getElement1(), new Integer(10));

    bids.add(new Pair<String, BidEntry>("agent0", new BidEntry(1, query, 1, 1)));
    bids.add(new Pair<String, BidEntry>("agent1", new BidEntry(2, query, 1, 1)));

    AdStatistics adStatistics1 = AdStatisticsTest.getAdStatistics();
    AdAuctions.runSecondPriceAuction(0, query, 10, bids, limits, adStatistics1);
    assertTrue(adStatistics1.getStatistic(0, "agent0", 1, query).getElement1() == 1 || adStatistics1.getStatistic(0, "agent0", 1, query).getElement1() == 9);

    bids.add(new Pair<String, BidEntry>("agent0", new BidEntry(0, query, 100, 10)));
    bids.add(new Pair<String, BidEntry>("agent1", new BidEntry(1, query, 200, 10)));
    bids.add(new Pair<String, BidEntry>("agent2", new BidEntry(2, query, 300, 10)));
    bids.add(new Pair<String, BidEntry>("agent3", new BidEntry(3, query, 400, 3000)));

    AdStatistics adStatistics2 = AdStatisticsTest.getAdStatistics();
    AdAuctions.runSecondPriceAuction(0, query, 10, bids, limits, adStatistics2);
    assertEquals(adStatistics2.getStatistic(0, "agent3", 3, query).getElement1(), new Integer(10));
    assertEquals(adStatistics2.getStatistic(0, "agent3", 3, query).getElement2(), new Double(3000.0));

    // Testing query limits, as well as daily limits.
    bids = new ArrayList<Pair<String, BidEntry>>();
    bids.add(new Pair<String, BidEntry>("agent0", new BidEntry(0, query, 100, 10)));
    bids.add(new Pair<String, BidEntry>("agent1", new BidEntry(1, query, 200, 100)));
    bids.add(new Pair<String, BidEntry>("agent2", new BidEntry(2, query, 300, 2000)));
    bids.add(new Pair<String, BidEntry>("agent3", new BidEntry(3, query, 400, 3000)));

    AdStatistics adStatistics3 = AdStatisticsTest.getAdStatistics();
    limits.put(3, 300.5);
    AdAuctions.runSecondPriceAuction(0, query, 10, bids, limits, adStatistics3);
    assertEquals(adStatistics3.getStatistic(0, "agent3", 3, query).getElement1(), new Integer(1));
    assertEquals(adStatistics3.getStatistic(0, "agent3", 3, query).getElement2(), new Double(300.0));
    assertEquals(adStatistics3.getStatistic(0, "agent2", 2, query).getElement1(), new Integer(9));
    assertEquals(adStatistics3.getStatistic(0, "agent2", 2, query).getElement2(), new Double(1800.0));
  }

  @Test
  public void testWinnerDetermination() throws AdXException {
    // Test null pointer
    try {
      AdAuctions.winnerDetermination(null);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }
    // Test No bids
    List<Pair<String, BidEntry>> bids = new ArrayList<Pair<String, BidEntry>>();
    try {
      AdAuctions.winnerDetermination(bids);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }
    // Test only one bid.
    bids.add(new Pair<String, BidEntry>("agent0", new BidEntry(0, new Query(MarketSegment.FEMALE), 100, 1000)));
    Pair<Double, List<Pair<String, BidEntry>>> auctionWinners0 = AdAuctions.winnerDetermination(bids);
    assertEquals(auctionWinners0.getElement1(), (Double) 0.0);
    assertEquals(auctionWinners0.getElement2().get(0), bids.get(0));
    // Test two equal bids.
    bids.add(new Pair<String, BidEntry>("agent1", new BidEntry(1, new Query(MarketSegment.FEMALE), 100, 1000)));
    Pair<Double, List<Pair<String, BidEntry>>> auctionWinners1 = AdAuctions.winnerDetermination(bids);
    assertEquals(auctionWinners1.getElement1(), (Double) 100.0);
    assertTrue(auctionWinners1.getElement2().get(0) == bids.get(0) && auctionWinners1.getElement2().get(1) == bids.get(1));
    // Test three bids: two equal winners, one other lower.
    bids.add(new Pair<String, BidEntry>("agent2", new BidEntry(2, new Query(MarketSegment.FEMALE), 90, 1000)));
    Pair<Double, List<Pair<String, BidEntry>>> auctionWinners2 = AdAuctions.winnerDetermination(bids);
    assertEquals(auctionWinners2.getElement1(), (Double) 100.0);
    assertTrue(auctionWinners2.getElement2().get(0) == bids.get(0) && auctionWinners2.getElement2().get(1) == bids.get(1));
    assertFalse(auctionWinners2.getElement2().get(0) == bids.get(2));

    // Test all different bids
    List<Pair<String, BidEntry>> allDifferentBids = new ArrayList<Pair<String, BidEntry>>();
    allDifferentBids.add(new Pair<String, BidEntry>("agent100", new BidEntry(3, new Query(MarketSegment.FEMALE), 50.0, 1000)));
    allDifferentBids.add(new Pair<String, BidEntry>("agent200", new BidEntry(4, new Query(MarketSegment.FEMALE), 40.0, 1000)));
    allDifferentBids.add(new Pair<String, BidEntry>("agent300", new BidEntry(5, new Query(MarketSegment.FEMALE), 30.0, 1000)));
    allDifferentBids.add(new Pair<String, BidEntry>("agent400", new BidEntry(6, new Query(MarketSegment.FEMALE), 20.0, 1000)));
    allDifferentBids.add(new Pair<String, BidEntry>("agent500", new BidEntry(7, new Query(MarketSegment.FEMALE), 10.0, 1000)));
    Pair<Double, List<Pair<String, BidEntry>>> auctionWinners3 = AdAuctions.winnerDetermination(allDifferentBids);
    assertEquals(auctionWinners3.getElement1(), (Double) 40.0);
    assertEquals(auctionWinners3.getElement2().get(0), allDifferentBids.get(0));
  }

  @Test
  public void testSimpleTieAuction() throws AdXException {
    Query query = new Query(MarketSegment.FEMALE);
    int supply = 2;
    List<Pair<String, BidEntry>> bids = new ArrayList<Pair<String, BidEntry>>();
    bids.add(new Pair<String, BidEntry>("agent0", new BidEntry(1, query, 50, 50)));
    bids.add(new Pair<String, BidEntry>("agent1", new BidEntry(2, query, 50, Double.MAX_VALUE)));

    AdStatistics adStatistics = AdStatisticsTest.getAdStatistics();
    AdAuctions.runSecondPriceAuction(0, query, supply, bids, new HashMap<Integer, Double>(), adStatistics);

    assertTrue(adStatistics.getSummaryStatistic(0, "agent1", 2).getElement1() > 0);
    assertTrue(adStatistics.getSummaryStatistic(0, "agent0", 1).getElement1() == 0 || adStatistics.getSummaryStatistic(0, "agent0", 1).getElement1() == 1);
    // Logging.log(adStatistics);
  }

  @Test
  public void testSimpleAuction() throws AdXException {
    Query query = new Query(MarketSegment.FEMALE);
    int supply = 100;
    List<Pair<String, BidEntry>> bids = new ArrayList<Pair<String, BidEntry>>();
    bids.add(new Pair<String, BidEntry>("agent0", new BidEntry(1, query, 50, Double.MAX_VALUE)));
    bids.add(new Pair<String, BidEntry>("agent1", new BidEntry(2, query, 40, Double.MAX_VALUE)));

    AdStatistics adStatistics = AdStatisticsTest.getAdStatistics();
    AdAuctions.runSecondPriceAuction(0, query, supply, bids, new HashMap<Integer, Double>(), adStatistics);
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, query).getElement1(), new Integer(100));
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, query).getElement2(), new Double(4000.0));

    Query query2 = new Query(MarketSegment.FEMALE);
    int supply2 = 213;
    List<Pair<String, BidEntry>> bids2 = new ArrayList<Pair<String, BidEntry>>();
    bids2.add(new Pair<String, BidEntry>("agent0", new BidEntry(1, query, 50, Double.MAX_VALUE)));
    bids2.add(new Pair<String, BidEntry>("agent1", new BidEntry(2, query, 40, Double.MAX_VALUE)));
    AdAuctions.runSecondPriceAuction(0, query2, supply2, bids2, new HashMap<Integer, Double>(), adStatistics);

    assertEquals(adStatistics.getStatistic(0, "agent0", 1, query).getElement1(), new Integer(313));
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, query).getElement2(), new Double(12520.0));

  }
  
  @Test 
  public void testSimpleAuction2() throws AdXException {
    Query query = new Query(MarketSegment.MALE_YOUNG_HIGH_INCOME);
    int supply = 493;
    List<Pair<String, BidEntry>> bids = new ArrayList<Pair<String, BidEntry>>();
    bids.add(new Pair<String, BidEntry>("agent0", new BidEntry(1, query, 1.0, 994.0)));
    bids.add(new Pair<String, BidEntry>("agent1", new BidEntry(2, query, 1.0, 1882.0)));

    HashMap<Integer, Double> limits = new HashMap<Integer, Double>();
    limits.put(1, 994.0);
    limits.put(2, 1882.0);
    
    AdStatistics adStatistics = AdStatisticsTest.getAdStatistics();
    AdAuctions.runSecondPriceAuction(0, query, supply, bids, limits, adStatistics);
    assertTrue((adStatistics.getStatistic(0, "agent0", 1, query).getElement2() / adStatistics.getStatistic(0, "agent0", 1, query).getElement1()) == 1.0);
    assertTrue((adStatistics.getStatistic(0, "agent1", 2, query).getElement2() / adStatistics.getStatistic(0, "agent1", 2, query).getElement1()) == 1.0);
  }

  @Test
  public void testRunAllAuctions() throws AdXException {
    HashMap<String, BidBundle> bidBundles = new HashMap<String, BidBundle>();
    bidBundles.put("agent0", BidBundleTest.getBidBundle0());
    bidBundles.put("agent1", BidBundleTest.getBidBundle1());
    bidBundles.put("agent2", BidBundleTest.getBidBundle2());
    bidBundles.put("agent3", BidBundleTest.getBidBundle3());
    // Logging.log(bidBundles);
    AdStatistics adStatistics = AdStatisticsTest.getAdStatistics();
    AdAuctions.runAllAuctions(0, bidBundles, adStatistics);
    // Logging.log(adStatistics);
  }

}
