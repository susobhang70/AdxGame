package adx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;

import org.junit.Test;

import adx.auctions.AdStatistics;
import adx.exceptions.AdXException;
import adx.structures.MarketSegment;
import adx.structures.Query;

public class AdStatisticsTest {

  public static AdStatistics getAdStatistics() {
    HashSet<String> agents = new HashSet<String>();
    agents.add("agent0");
    agents.add("agent1");
    agents.add("agent2");
    agents.add("agent3");
    return new AdStatistics(agents);
  }

  @Test
  public void testAddStatistic() throws AdXException {
    HashSet<String> agents = new HashSet<String>();
    agents.add("agent0");
    agents.add("agent1");
    AdStatistics adStatistics = new AdStatistics(agents);
    try {
      adStatistics.getStatistic(-1, null, 0, null);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }
    try {
      adStatistics.getStatistic(1, null, 0, null);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME), 10, 100.0);

    try {
      adStatistics.getStatistic(0, "WRONG AGENT", 0, null);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }

    try {
      adStatistics.getStatistic(0, "agent0", 0, null);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }

    assertEquals(adStatistics.getStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME)).getElement1(), new Integer(10));
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME)).getElement2(), new Double(100.0));
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_OLD_LOW_INCOME), 20, 200.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_HIGH_INCOME), 30, 300.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME), 40, 400.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.MALE_YOUNG_LOW_INCOME), 50, 500.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.MALE_OLD_LOW_INCOME), 60, 600.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.MALE_YOUNG_HIGH_INCOME), 70, 700.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 80, 800.0);
    adStatistics.addStatistic(0, "agent1", 1, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 90, 900.0);
    adStatistics.addStatistic(1, "agent1", 1, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 100, 1000.0);
    adStatistics.addStatistic(1, "agent1", 1, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME), 1000, 3000.0);
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME)).getElement1(), new Integer(40));
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME)).getElement2(), new Double(400.0));
    assertEquals(adStatistics.getSummaryStatistic(0, "agent0", 1).getElement1(), new Integer(360));
    assertEquals(adStatistics.getSummaryStatistic(0, "agent0", 1).getElement2(), new Double(3600.0));
    assertEquals(adStatistics.getSummaryStatistic(1, "agent1", 1).getElement1(), new Integer(1100));
    assertEquals(adStatistics.getSummaryStatistic(1, "agent1", 1).getElement2(), new Double(4000.0));

    try {
      adStatistics.getSummaryStatistic(-1, null, 0);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }
    try {
      adStatistics.getSummaryStatistic(1, null, 0);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }
    try {
      adStatistics.getSummaryStatistic(0, "WRONG AGENT", 0);
      fail("Suppose to get AdXException");
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }
    // Logging.log(adStatistics);
  }

}
