package adx.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;

import org.junit.Test;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.MarketSegment;
import adx.structures.Query;

public class BidBundleTest {

  @SuppressWarnings("serial")
  public static BidBundle getBidBundle0() {
    return new BidBundle(0, new HashSet<BidEntry>() {
      {
        add(new BidEntry(1, new Query(MarketSegment.FEMALE), 12.0, 120.0));
        add(new BidEntry(1, new Query(MarketSegment.MALE), 21.0, 210.0));
      }
    }, new HashMap<Integer, Double>() {
      {
        put(1, 200.0);
      }
    });

  }

  @SuppressWarnings("serial")
  public static BidBundle getBidBundle1() {
    return new BidBundle(0, new HashSet<BidEntry>() {
      {
        add(new BidEntry(2, new Query(MarketSegment.FEMALE_HIGH_INCOME), 600, 1220.0));
      }
    }, new HashMap<Integer, Double>() {
      {
        put(2, 300.0);
      }
    });
  }

  @SuppressWarnings("serial")
  public static BidBundle getBidBundle2() {
    return new BidBundle(0, new HashSet<BidEntry>() {
      {
        add(new BidEntry(3, new Query(MarketSegment.YOUNG), 600, 31220.0));
      }
    }, new HashMap<Integer, Double>() {
      {
        put(3, 400.0);
      }
    });
  }

  @SuppressWarnings("serial")
  public static BidBundle getBidBundle3() {
    return new BidBundle(0, new HashSet<BidEntry>() {
      {
        add(new BidEntry(4, new Query(MarketSegment.YOUNG), 600, 31220.0));
        add(new BidEntry(4, new Query(MarketSegment.MALE_YOUNG), 312.2, 31220.0));
        add(new BidEntry(4, new Query(MarketSegment.FEMALE_LOW_INCOME), 312.2, 31220.0));
      }
    }, null);
  }

  public static Table<Integer, String, BidBundle> getTableBidBundles() {
    Table<Integer, String, BidBundle> bidBundles = HashBasedTable.create();
    bidBundles.put(0, "agent0", BidBundleTest.getBidBundle0());
    bidBundles.put(0, "agent1", BidBundleTest.getBidBundle1());
    bidBundles.put(0, "agent2", BidBundleTest.getBidBundle2());
    bidBundles.put(0, "agent3", BidBundleTest.getBidBundle3());
    return bidBundles;
  }

  @Test
  public void testBidBundles() {
    BidBundle bidBundle0 = BidBundleTest.getBidBundle0();
    assertEquals(bidBundle0.getBidEntries().size(), 2);
    assertEquals(bidBundle0.getCampaignLimit(1), new Double(200.0));

    BidBundle bidBundle1 = BidBundleTest.getBidBundle1();
    assertEquals(bidBundle1.getBidEntries().size(), 1);
    assertEquals(bidBundle1.getCampaignLimit(2), new Double(300.0));

    BidBundle bidBundle2 = BidBundleTest.getBidBundle2();
    assertEquals(bidBundle2.getBidEntries().size(), 1);
    assertEquals(bidBundle2.getCampaignLimit(3), new Double(400.0));

    BidBundle bidBundle3 = BidBundleTest.getBidBundle3();
    assertEquals(bidBundle3.getBidEntries().size(), 3);
    assertEquals(bidBundle3.getCampaignLimit(4), null);

    assertEquals(BidBundleTest.getTableBidBundles().size(), 4);
  }

}
