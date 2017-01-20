package adx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;

import org.junit.Test;

import adx.auctions.Statistics;
import adx.exceptions.AdXException;
import adx.structures.Campaign;
import adx.structures.MarketSegment;
import adx.structures.Query;
import adx.util.Pair;

public class StatisticsTest {

  public static Statistics getStatistics() throws AdXException {
    HashSet<String> agents = new HashSet<String>();
    agents.add("agent0");
    agents.add("agent1");
    agents.add("agent2");
    agents.add("agent3");
    return new Statistics(agents);
  }

  @Test(expected = AdXException.class)
  public void testRegisterCampaign0() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    statistics.registerCampaign(1, new Campaign(), "agent0");
  }

  @Test(expected = AdXException.class)
  public void testRegisterCampaign1() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    statistics.registerCampaign(1, new Campaign(1, 1, 1, MarketSegment.HIGH_INCOME, 1), "WRONG AGENT");
  }

  @Test(expected = AdXException.class)
  public void testRegisterCampaign2() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    statistics.registerCampaign(0, new Campaign(1, 1, 1, MarketSegment.FEMALE, 1), "agent0");
    statistics.registerCampaign(0, new Campaign(1, 1, 1, MarketSegment.FEMALE, 1), "agent1");
  }

  @Test
  public void testRegisterCampaign3() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    statistics.registerCampaign(1, new Campaign(1, 1, 1, MarketSegment.YOUNG_HIGH_INCOME, 1), "agent0");
    assertTrue(statistics.isOwner(1, "agent0"));
    assertTrue(statistics.campaignExists(1));
    assertFalse(statistics.campaignExists(999));
  }

  @Test
  public void testAddStatistic0() throws AdXException {
    HashSet<String> agents = new HashSet<String>();
    agents.add("agent0");
    agents.add("agent1");
    Statistics adStatistics = new Statistics(agents);
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

    try {
      adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME), 10, 100.0);
    } catch (AdXException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Got wrong kind of exception");
    }

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

    adStatistics.registerCampaign(0, new Campaign(1, 1, 1, MarketSegment.FEMALE, 1), "agent0");
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME), 10, 100.0);

    adStatistics.registerCampaign(0, new Campaign(2, 1, 1, MarketSegment.FEMALE, 1), "agent1");
    adStatistics.addStatistic(0, "agent1", 2, new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME), 10, 100.0);

    assertEquals(adStatistics.getStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME)).getElement1(), new Integer(10));
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_LOW_INCOME)).getElement2(), new Double(100.0));
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_OLD_LOW_INCOME), 20, 200.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_HIGH_INCOME), 30, 300.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME), 40, 400.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.MALE_YOUNG_LOW_INCOME), 50, 500.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.MALE_OLD_LOW_INCOME), 60, 600.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.MALE_YOUNG_HIGH_INCOME), 70, 700.0);
    adStatistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 80, 800.0);
    adStatistics.addStatistic(0, "agent1", 2, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 90, 900.0);
    adStatistics.addStatistic(1, "agent1", 2, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 100, 1000.0);
    adStatistics.addStatistic(1, "agent1", 2, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME), 1000, 3000.0);
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME)).getElement1(), new Integer(40));
    assertEquals(adStatistics.getStatistic(0, "agent0", 1, new Query(MarketSegment.FEMALE_OLD_HIGH_INCOME)).getElement2(), new Double(400.0));
    assertEquals(adStatistics.getSummaryStatistic(0, "agent0", 1).getElement1(), new Integer(360));
    assertEquals(adStatistics.getSummaryStatistic(0, "agent0", 1).getElement2(), new Double(3600.0));
    assertEquals(adStatistics.getSummaryStatistic(1, "agent1", 2).getElement1(), new Integer(1100));
    assertEquals(adStatistics.getSummaryStatistic(1, "agent1", 2).getElement2(), new Double(4000.0));

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
  }

  @Test(expected = AdXException.class)
  public void testAddStatistic1() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    statistics.addStatistic(0, "agent0", 1, new Query(MarketSegment.MALE_OLD_LOW_INCOME), 3, 3.28);
  }

  @Test
  public void testComputeQualityScore0() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement1(), new Double(1.0));

    Campaign c = new Campaign(1, 1, 1, MarketSegment.FEMALE, 100);
    statistics.registerCampaign(0, c, "agent0");
    c.setBudget(100.0);
    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE), 90, 0.0);
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement1(), new Double(0.9261666020749232));
  }

  @Test
  public void testComputeQualityScore1() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    Campaign c0 = new Campaign(1, 1, 1, MarketSegment.FEMALE, 100);
    statistics.registerCampaign(0, c0, "agent0");

    c0.setBudget(100.0);

    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE), 90, 0.0);
    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE), 90, 0.0);
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement1(), new Double(1.1630636924254625));

  }

  @Test
  public void testComputeQualityScore2() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    Campaign c0 = new Campaign(1, 1, 1, MarketSegment.FEMALE, 100);
    Campaign c1 = new Campaign(2, 1, 1, MarketSegment.FEMALE, 200);
    statistics.registerCampaign(0, c0, "agent0");
    statistics.registerCampaign(0, c1, "agent0");

    c0.setBudget(100.0);
    c1.setBudget(200.0);

    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE), 100, 0.0);
    statistics.addStatistic(1, "agent0", 2, new Query(MarketSegment.FEMALE), 200, 0.0);
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement1(), new Double(0.9999766748613623));

  }

  @Test
  public void testComputeQualityScore3() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    Campaign c0 = new Campaign(1, 1, 1, MarketSegment.FEMALE, 100);
    Campaign c1 = new Campaign(2, 1, 1, MarketSegment.FEMALE, 200);
    statistics.registerCampaign(0, c0, "agent0");
    statistics.registerCampaign(0, c1, "agent0");

    c0.setBudget(100.0);
    c1.setBudget(200.0);

    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE), 0, 0.0);
    statistics.addStatistic(1, "agent0", 2, new Query(MarketSegment.FEMALE), 200, 0.0);
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement1(), new Double(0.6999883374306812));

  }

  @Test
  public void testComputeQualityScore4() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    Campaign c0 = new Campaign(1, 1, 1, MarketSegment.FEMALE, 100);
    Campaign c1 = new Campaign(2, 1, 1, MarketSegment.FEMALE, 200);
    statistics.registerCampaign(0, c0, "agent0");
    statistics.registerCampaign(0, c1, "agent0");

    c0.setBudget(100.0);
    c1.setBudget(200.0);

    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE), 100, 0.0);
    statistics.addStatistic(1, "agent0", 2, new Query(MarketSegment.FEMALE), 0, 0.0);
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement1(), new Double(0.6999883374306812));

  }

  @Test
  public void testComputeProfit0() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement2(), new Double(0.0));

    Campaign c0 = new Campaign(1, 1, 1, MarketSegment.FEMALE, 100);
    Campaign c1 = new Campaign(2, 1, 1, MarketSegment.FEMALE, 200);
    statistics.registerCampaign(0, c0, "agent0");
    statistics.registerCampaign(0, c1, "agent0");

    c0.setBudget(100.0);
    c1.setBudget(200.0);

    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE), 100, 0.0);
    statistics.addStatistic(1, "agent0", 2, new Query(MarketSegment.FEMALE), 100, 0.0);
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement2(), new Double(144.1513113566575));

    statistics.addStatistic(1, "agent0", 2, new Query(MarketSegment.FEMALE), 100, 0.0);
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement2(), new Double(299.9883374306812));
  }

  @Test
  public void testComputeProfit1() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    Campaign c1 = new Campaign(1, 1, 1, MarketSegment.FEMALE, 100);
    statistics.registerCampaign(0, c1, "agent0");
    c1.setBudget(100.0);

    Campaign c2 = new Campaign(2, 1, 1, MarketSegment.MALE, 100);
    statistics.registerCampaign(0, c2, "agent0");
    c2.setBudget(100.0);

    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE), 80, 85.25);
    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.MALE), 80, 85.25);
    statistics.addStatistic(1, "agent0", 2, new Query(MarketSegment.MALE_HIGH_INCOME), 80, 85.25);
    assertEquals(statistics.computeQualityScoreAndCumulativeProfit(1, "agent0").getElement2(), new Double(-114.94367301687879));
  }

  @Test
  public void computeEffectiveReach() throws AdXException {
    Statistics statistics = StatisticsTest.getStatistics();
    Campaign c1 = new Campaign(1, 1, 1, MarketSegment.FEMALE, 100);
    statistics.registerCampaign(0, c1, "agent0");
    c1.setBudget(100.0);

    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE), 80, 85.25);
    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.MALE), 80, 85.25);
    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE_HIGH_INCOME), 80, 85.25);
    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.FEMALE_YOUNG_HIGH_INCOME), 80, 85.25);
    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.OLD), 80, 85.25);
    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.YOUNG_LOW_INCOME), 80, 85.25);
    statistics.addStatistic(1, "agent0", 1, new Query(MarketSegment.MALE_OLD_HIGH_INCOME), 80, 85.25);
    Pair<Integer, Double> y = statistics.computeEffectiveReachAndCost(1, "agent0", c1);
    assertEquals(y.getElement1(), new Integer(240));
    assertEquals(y.getElement2(), new Double(596.75));
  }

}
