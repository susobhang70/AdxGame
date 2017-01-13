package adx.test;

import java.util.HashMap;
import java.util.HashSet;

import adx.auctions.AdAuctions;
import adx.auctions.AdStatistics;
import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.BidEntry;
import adx.structures.MarketSegment;
import adx.structures.Query;
import adx.util.Logging;

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
            new HashSet<BidEntry>() {{add(new BidEntry(2, new Query(MarketSegment.FEMALE_HIGH_INCOME), 600, 1220.0));}}, 
            new HashMap<Integer, Double>() {{put(2, 300.0);}
        });

    BidBundle bidBundle2 = 
        new BidBundle(0, 
            new HashSet<BidEntry>() {{add(new BidEntry(3, new Query(MarketSegment.YOUNG), 600, 31220.0));}}, 
            new HashMap<Integer, Double>() {{put(2, 400.0);}
        });

    BidBundle bidBundle3 = 
        new BidBundle(0, 
            new HashSet<BidEntry>() {{add(new BidEntry(4, new Query(MarketSegment.YOUNG), 600, 31220.0));
                                      add(new BidEntry(4, new Query(MarketSegment.MALE_YOUNG), 312.2, 31220.0));
                                      add(new BidEntry(4, new Query(MarketSegment.FEMALE_LOW_INCOME), 312.2, 31220.0));}}, 
            new HashMap<Integer, Double>() {{put(2, 400.0);}
        });

    Table<Integer, String, BidBundle> bidBundles = HashBasedTable.create();
    bidBundles.put(0, "enrique0", bidBundle0);
    bidBundles.put(0, "enrique1", bidBundle1);
    bidBundles.put(0, "enrique2", bidBundle2);
    bidBundles.put(0, "enrique3", bidBundle3);

    Logging.log("BidBundles = " + bidBundles);
    
    HashMap<Integer, String> campaignOwnership = new HashMap<Integer, String>();
    campaignOwnership.put(1, "enrique0");
    campaignOwnership.put(2, "enrique1");
    campaignOwnership.put(3, "enrique2");
    campaignOwnership.put(4, "enrique3");
    
    Logging.log(AdAuctions.filterBids(new Query(MarketSegment.FEMALE_YOUNG_HIGH_INCOME), bidBundles.row(0)));
    Logging.log(AdAuctions.filterBids(new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME), bidBundles.row(0)));
    Logging.log(AdAuctions.filterBids(new Query(MarketSegment.MALE), bidBundles.row(0)));
    Logging.log(AdAuctions.filterBids(new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME), bidBundles.row(0)));
    Logging.log(AdAuctions.filterBids(new Query(MarketSegment.OLD_HIGH_INCOME), bidBundles.row(0)));
    
    
    
    AdStatistics adStatistics = new AdStatistics();
    adStatistics.addStatistic(0, "enrique0", 1, new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME), 10, 100.0);
    adStatistics.addStatistic(0, "enrique0", 1, new Query(MarketSegment.FEMALE_OLD_LOW_INCOME), 20, 200.0);
    adStatistics.addStatistic(0, "enrique0", 1, new Query(MarketSegment.FEMALE_YOUNG_HIGH_INCOME), 30, 300.0);
    adStatistics.addStatistic(0, "enrique0", 1, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME), 40, 400.0);
    adStatistics.addStatistic(0, "enrique0", 1, new Query(MarketSegment.MALE_YOUNG_LOW_INCOME), 50, 500.0);
    adStatistics.addStatistic(0, "enrique0", 1, new Query(MarketSegment.MALE_OLD_LOW_INCOME), 60, 600.0);
    adStatistics.addStatistic(0, "enrique0", 1, new Query(MarketSegment.MALE_YOUNG_HIGH_INCOME), 70, 700.0);
    adStatistics.addStatistic(0, "enrique0", 1, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 80, 800.0);
    adStatistics.addStatistic(0, "enrique1", 1, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 90, 900.0);
    adStatistics.addStatistic(1, "enrique1", 1, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 100, 1000.0);
    adStatistics.addStatistic(1, "enrique1", 1, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME), 1000, 3000.0);
    Logging.log(adStatistics);
    
    
    AdAuctions.runSecondPriceAuction(null, 1000, AdAuctions.filterBids(new Query(MarketSegment.FEMALE_YOUNG_HIGH_INCOME), bidBundles.row(0)), null, adStatistics);
    
  }
}
