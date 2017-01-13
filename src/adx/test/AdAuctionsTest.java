package adx.test;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import adx.auctions.AdAuctions;
import adx.exceptions.AdXException;
import adx.structures.BidEntry;
import adx.structures.MarketSegment;
import adx.structures.Query;

public class AdAuctionsTest {

  @Test
  public void testFilterBids() {
    // fail("Not yet implemented");
  }

  @Test
  public void testRunSecondPriceAuction() {
    // fail("Not yet implemented");
  }

  @Test
  public void testWinnerDetermination() throws AdXException {
    List<Entry<String, BidEntry>> bids = new ArrayList<Entry<String, BidEntry>>();
    // Only one bid.
    bids.add(new AbstractMap.SimpleEntry<String, BidEntry>("agent0",
        new BidEntry(0, new Query(MarketSegment.FEMALE), 100, 1000)));
    Entry<Double, Entry<String, BidEntry>> auctionOutcome0 = AdAuctions.winnerDetermination(bids);
    //Logging.log(auctionOutcome0);
    assertEquals(auctionOutcome0.getKey(), (Double) 0.0);
    assertEquals(auctionOutcome0.getValue().getKey(), "agent0");
    // Two equal bids.
    bids.add(new AbstractMap.SimpleEntry<String, BidEntry>("agent1",
        new BidEntry(1, new Query(MarketSegment.FEMALE), 100, 1000)));
    Entry<Double, Entry<String, BidEntry>> auctionOutcome1 = AdAuctions.winnerDetermination(bids);
    //Logging.log(auctionOutcome1);
    assertEquals(auctionOutcome1.getKey(), (Double) 100.0);
    // Three bids, two equal winners, one other lower.
    bids.add(new AbstractMap.SimpleEntry<String, BidEntry>("agent2",
        new BidEntry(2, new Query(MarketSegment.FEMALE), 90, 1000)));
    Entry<Double, Entry<String, BidEntry>> auctionOutcome2 = AdAuctions.winnerDetermination(bids);
    //Logging.log(auctionOutcome2);
    assertEquals(auctionOutcome2.getKey(), (Double) 100.0);

    //All different bids
    List<Entry<String, BidEntry>> allDifferentBids = new ArrayList<Entry<String, BidEntry>>();
    allDifferentBids.add(new AbstractMap.SimpleEntry<String, BidEntry>("agent100",
        new BidEntry(3, new Query(MarketSegment.FEMALE), 50.0, 1000)));
    allDifferentBids.add(new AbstractMap.SimpleEntry<String, BidEntry>("agent200",
        new BidEntry(4, new Query(MarketSegment.FEMALE), 40.0, 1000)));
    allDifferentBids.add(new AbstractMap.SimpleEntry<String, BidEntry>("agent300",
        new BidEntry(5, new Query(MarketSegment.FEMALE), 30.0, 1000)));
    allDifferentBids.add(new AbstractMap.SimpleEntry<String, BidEntry>("agent400",
        new BidEntry(6, new Query(MarketSegment.FEMALE), 20.0, 1000)));
    allDifferentBids.add(new AbstractMap.SimpleEntry<String, BidEntry>("agent500",
        new BidEntry(7, new Query(MarketSegment.FEMALE), 10.0, 1000)));
    Entry<Double, Entry<String, BidEntry>> auctionOutcome3 = AdAuctions.winnerDetermination(allDifferentBids);
    //Logging.log(auctionOutcome3);
    assertEquals(auctionOutcome3.getKey(), (Double) 40.0);
    assertEquals(auctionOutcome3.getValue().getKey(), "agent100");

  }

}
