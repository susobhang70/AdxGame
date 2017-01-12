package adx.test;

import java.util.HashMap;
import java.util.HashSet;

import adx.auctions.AdAuctions;
import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.MarketSegment;
import adx.structures.Query;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Testing class
 * 
 * @author Enrique Areyan Viqueira
 */
public class Test {

  @SuppressWarnings("serial")
  public static void main(String[] args) throws AdXException {
    System.out.println("Testing AdX Game");

    /*
     * System.out.println("Probs = " + MarketSegment.proportionsMap);
     * System.out.println("Total = " + Sampling.totalProportion);
     * System.out.println
     * (MarketSegment.proportionsMap.get(MarketSegment.MALE_YOUNG_HIGH_INCOME));
     * for (int i = 0; i < 100; i++) System.out.println("Sample = " +
     * Sampling.samplePopulation(10000));
     * 
     * for (int i = 0; i < 100; i++) { Campaign c = Sampling.sampleCampaign();
     * System.out.println(c); }
     * 
     * Instant time2 = Instant.now().plusSeconds(3); while
     * (Instant.now().isBefore(time2)); System.out.println("Done");
     */
    BidBundle bidBundle0 = 
        new BidBundle(0, 
            new HashSet<BidEntry>() {{add(new BidEntry(1, new Query(MarketSegment.FEMALE), 12.0, 120.0));
                                      add(new BidEntry(1, new Query(MarketSegment.MALE), 21.0, 210.0));}}, 
            new HashMap<Integer, Double>() {{put(1, 200.0);}
        });
    BidBundle bidBundle1 = 
        new BidBundle(0, 
            new HashSet<BidEntry>() {{add(new BidEntry(2, new Query(MarketSegment.FEMALE_HIGH_INCOME), 122.0, 1220.0));}}, 
            new HashMap<Integer, Double>() {{put(2, 300.0);}
        });

    BidBundle bidBundle2 = 
        new BidBundle(0, 
            new HashSet<BidEntry>() {{add(new BidEntry(2, new Query(MarketSegment.YOUNG), 312.2, 31220.0));}}, 
            new HashMap<Integer, Double>() {{put(2, 400.0);}
        });

    Table<Integer, String, BidBundle> bidBundles = HashBasedTable.create();
    bidBundles.put(0, "enrique0", bidBundle0);
    bidBundles.put(0, "enrique1", bidBundle1);
    bidBundles.put(0, "enrique2", bidBundle2);

    System.out.println(bidBundles);
    
    AdAuctions.filterBids(new Query(MarketSegment.FEMALE), bidBundles.row(0));
  }
}
