package adx.auctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.structures.Query;
import adx.util.Pair;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Maintains statistics of ad auction.
 * 
 * @author Enrique Areyan Viqueira
 */
public class AdStatistics {

  /**
   * A table that maps: Days -> Agent -> Campaign -> Query -> (Win Count, Win Cost)
   */
  private final Table<Integer, String, Table<Integer, Query, Pair<Integer, Double>>> statistics;

  /**
   * A table that maps: Days -> Agent -> Campaign > (Total Win Count, Total Win Cost)
   */
  private final Table<Integer, String, Map<Integer, Pair<Integer, Double>>> summary;

  /**
   * Contains the permissible agents.
   */
  private final Set<String> agentsNames;

  /**
   * Constructor.
   */
  public AdStatistics(Set<String> agentsNames) {
    this.statistics = HashBasedTable.create();
    this.summary = HashBasedTable.create();
    this.agentsNames = agentsNames;
  }

  /**
   * Checks if the given input (day, agent) is valid.
   * 
   * @param day
   * @param agent
   * @throws AdXException
   */
  private void checkValidInput(int day, String agent) throws AdXException {
    if (day < 0) {
      throw new AdXException("There are no statistics for negative days.");
    } else if (agent == null) {
      throw new AdXException("There are no statistics for null agent.");
    } else if (!this.agentsNames.contains(agent)) {
      throw new AdXException("Unknonw agent " + agent);
    }
  }

  /**
   * Check if the given input (day, agent, query) is valid.
   * 
   * @param day
   * @param agent
   * @param query
   * @throws AdXException
   */
  private void checkValidInput(int day, String agent, Query query) throws AdXException {
    if (query == null) {
      throw new AdXException("There are no statistics for null query.");
    } else {
      this.checkValidInput(day, agent);
    }
  }

  /**
   * Adds an statistic.
   * 
   * @param day
   * @param agent
   * @param campaignId
   * @param query
   * @param winCount
   * @param winCost
   * @throws AdXException
   */
  public void addStatistic(int day, String agent, int campaignId, Query query, int winCount, double winCost) throws AdXException {
    this.checkValidInput(day, agent, query);
    if (!this.statistics.contains(day, agent)) {
      this.statistics.put(day, agent, HashBasedTable.create());
      this.summary.put(day, agent, new HashMap<Integer, Pair<Integer, Double>>());
    }
    if (!this.statistics.get(day, agent).contains(campaignId, query)) {
      this.statistics.get(day, agent).put(campaignId, query, new Pair<Integer, Double>(winCount, winCost));
    } else {
      Integer currentCount = this.statistics.get(day, agent).get(campaignId, query).getElement1();
      Double currentCost = this.statistics.get(day, agent).get(campaignId, query).getElement2();
      this.statistics.get(day, agent).put(campaignId, query, new Pair<Integer, Double>(currentCount + winCount, currentCost + winCost));
    }
    if (this.summary.get(day, agent).get(campaignId) == null) {
      this.summary.get(day, agent).put(campaignId, new Pair<Integer, Double>(winCount, winCost));
    } else {
      Pair<Integer, Double> total = this.summary.get(day, agent).get(campaignId);
      this.summary.get(day, agent).put(campaignId, new Pair<Integer, Double>(total.getElement1() + winCount, total.getElement2() + winCost));
    }
  }

  /**
   * Returns a statistics.
   * 
   * @param day
   * @param agent
   * @param campaignId
   * @param query
   * @return a Tuple containing the winCount and the winCost.
   * @throws AdXException
   */
  public Pair<Integer, Double> getStatistic(int day, String agent, int campaignId, Query query) throws AdXException {
    this.checkValidInput(day, agent, query);
    if (this.statistics.get(day, agent) == null || this.statistics.get(day, agent).get(campaignId, query) == null) {
      this.addStatistic(day, agent, campaignId, query, 0, 0.0);
    }
    return this.statistics.get(day, agent).get(campaignId, query);
  }

  /**
   * Returns summary statistics.
   * 
   * @param day
   * @param agent
   * @param campaignId
   * @param query
   * @return
   * @throws AdXException
   */
  public Pair<Integer, Double> getSummaryStatistic(int day, String agent, int campaignId) throws AdXException {
    this.checkValidInput(day, agent);
    Map<Integer, Pair<Integer, Double>> dayAgentSummaryStatistics = this.summary.get(day, agent);
    if (dayAgentSummaryStatistics == null) {
      HashMap<Integer, Pair<Integer, Double>> newStat = new HashMap<Integer, Pair<Integer, Double>>();
      newStat.put(campaignId, new Pair<Integer, Double>(0, 0.0));
      this.summary.put(day, agent, newStat);
      dayAgentSummaryStatistics = newStat;
    }
    return dayAgentSummaryStatistics.get(campaignId);
  }

  /**
   * Returns the summary statistics for the given day and agent.
   * 
   * @param day
   * @param agent
   * @return
   */
  public Map<Integer, Pair<Integer, Double>> getDailySummary(int day, String agent) {
    return this.summary.get(day, agent);
  }

  @Override
  public String toString() {
    String ret = "";
    if (this.statistics.rowMap().entrySet().size() > 0) {
      ret += "\n\t\t Statistics Table:";
      for (Entry<Integer, Map<String, Table<Integer, Query, Pair<Integer, Double>>>> x : this.statistics.rowMap().entrySet()) {
        ret += "\n\t\t\t Day: " + x.getKey();
        for (Entry<String, Table<Integer, Query, Pair<Integer, Double>>> y : x.getValue().entrySet()) {
          ret += "\n\t\t\t\t Agent: " + y.getKey();
          for (Entry<Integer, Map<Query, Pair<Integer, Double>>> z : y.getValue().rowMap().entrySet()) {
            ret += "\n\t\t\t\t\t Campaign: " + z.getKey() + ", total " + this.summary.get(x.getKey(), y.getKey()).get(z.getKey());
            for (Entry<Query, Pair<Integer, Double>> w : z.getValue().entrySet()) {
              ret += "\n\t\t\t\t\t\t Query: " + w.getKey() + ", (" + w.getValue().getElement1() + ", " + w.getValue().getElement2() + ")";
            }
          }
        }
      }
    } else {
      ret += "Currently, there are no statistics";
    }
    return ret;
  }
}
