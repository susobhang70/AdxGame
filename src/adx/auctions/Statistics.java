package adx.auctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.structures.Campaign;
import adx.structures.Query;
import adx.util.InputValidators;
import adx.util.Pair;
import adx.util.Parameters;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Maintains statistics of ad auction.
 * 
 * @author Enrique Areyan Viqueira
 */
public class Statistics {

  /**
   * Contains the permissible agents.
   */
  private final Set<String> agentsNames;

  /**
   * A map from Day -> Agent -> Quality Score.
   */
  private Table<Integer, String, Double> qualityScores;

  /**
   * A map from Day -> Agent -> Profit
   */
  private Table<Integer, String, Double> profit;

  /**
   * A map from Campaign -> Agent.
   */
  private Map<Integer, String> campaignsOwnership;

  /**
   * A map from Day -> Agent -> List of Campaigns.
   */
  private Table<Integer, String, List<Campaign>> agentsCampaings;

  /**
   * A table that maps: Days -> Agent -> Campaign -> Query -> (Win Count, Win Cost)
   */
  private final Table<Integer, String, Table<Integer, Query, Pair<Integer, Double>>> statistics;

  /**
   * A table that maps: Days -> Agent -> Campaign > (Total Win Count, Total Win Cost)
   */
  private final Table<Integer, String, Map<Integer, Pair<Integer, Double>>> summary;

  /**
   * Constructor.
   * 
   * @throws AdXException
   */
  public Statistics(Set<String> agentsNames) throws AdXException {
    this.agentsNames = agentsNames;
    this.qualityScores = HashBasedTable.create();
    this.profit = HashBasedTable.create();
    for (String agent : this.agentsNames) {
      this.qualityScores.put(0, agent, 1.0);
      this.profit.put(0, agent, 0.0);
    }
    this.campaignsOwnership = new HashMap<Integer, String>();
    this.agentsCampaings = HashBasedTable.create();
    this.statistics = HashBasedTable.create();
    this.summary = HashBasedTable.create();
    InputValidators.validateNotNull(agentsNames);
  }

  /**
   * Checks that the agent name is registered. Throws an exception if the agent name is not registered.
   * 
   * @param agentName
   * @throws AdXException
   */
  private void checkAgentName(String agentName) throws AdXException {
    if (!this.agentsNames.contains(agentName)) {
      throw new AdXException("Unknown agent " + agentName);
    }
  }

  /**
   * This function checks whether the given campaign exists AND if the campaign belongs to the agent.
   * 
   * @param agentName
   * @param campaignId
   * @throws AdXException
   */
  private void checkCampaign(String agentName, int campaignId) throws AdXException {
    if (!this.campaignExists(campaignId)) {
      throw new AdXException("The campaign " + campaignId + ", does not exists");
    } else if (!this.isOwner(campaignId, agentName)) {
      throw new AdXException("The campaign " + campaignId + " is not owned by " + agentName);
    }
  }

  /**
   * Updates daily statistics, in this case quality score and profit.
   * 
   * @param day
   * @throws AdXException
   */
  public void updateDailyStatistics(int day) throws AdXException {
    for (String agent : this.agentsNames) {
      this.qualityScores.put(day, agent, this.computeQualityScore(day, agent));
      this.profit.put(day, agent, this.computeCumulativeProfit(day, agent));
    }
  }

  /**
   * Given a day and an agent, computes the agent's quality score.
   * 
   * @param day
   * @param agent
   * @return the agent's quality score for the day.
   */
  public Double computeQualityScore(int day, String agent) {
    // We will compute the quality score using yesterday's campaigns and quality score
    List<Campaign> campaigns = this.agentsCampaings.get(day - 1, agent);
    double qualityScore = this.getQualityScore(day - 1, agent);
    // We need today's statistics.
    Map<Integer, Pair<Integer, Double>> statistics = this.summary.get(day, agent);
    double averageEffectiveReachRatio = 0.0;
    // Logging.log("\t\t\tCompute Quality Score for day: " + day);
    // Logging.log("\t\t\tFor these campaigns: " + campaigns);
    // Logging.log("\t\t\tUsing these statistics: " + statistics);
    if (campaigns != null && campaigns.size() > 0) {
      if (statistics != null) {
        for (Campaign c : campaigns) {
          Pair<Integer, Double> stat = statistics.get(c.getId());
          if (stat != null) {
            averageEffectiveReachRatio += this.getEffectiveReach(stat.getElement1(), c.getReach());
          }
        }
        averageEffectiveReachRatio /= campaigns.size();
      }
      qualityScore = (1 - Parameters.QUALITY_SCORE_LEARNING_RATE) * qualityScore + Parameters.QUALITY_SCORE_LEARNING_RATE * averageEffectiveReachRatio;
    }
    return qualityScore;
  }

  /**
   * Computes profit for the given day and agent.
   * 
   * @param day
   * @param agent
   * @return the agent's profit for the day. Might be negative.
   */
  public Double computeCumulativeProfit(int day, String agent) {
    List<Campaign> campaigns = this.agentsCampaings.get(day - 1, agent);
    Map<Integer, Pair<Integer, Double>> statistics = this.summary.get(day, agent);
    Double profit = this.getProfit(day - 1, agent);
    if (statistics != null) {
      for (Campaign c : campaigns) {
        if (statistics.containsKey(c.getId())) {
          Pair<Integer, Double> stat = statistics.get(c.getId());
          profit += this.getEffectiveReach(stat.getElement1(), c.getReach()) * c.getBudget() - stat.getElement2();
        }
      }
    }
    return profit;
  }

  /**
   * Given a number of impressions x, a total reach and total budget, computes the Effective Reach Ratio.
   * 
   * @param x
   * @param reach
   * @param budget
   * @return the effective reach ratio for obtaining x impressions on a campaign with given reach and budget.
   */
  private double getEffectiveReach(double x, int reach) {
    return (2 / 4.08577) * (Math.atan(4.08577 * (x / reach) - 3.08577) - Math.atan(-3.08577));
  }

  /**
   * Getter.
   * 
   * @return the map of quality scores.
   */
  public Double getQualityScore(int day, String agent) {
    return this.qualityScores.get(day, agent);
  }
  
  /**
   * Getter.
   * 
   * @return the map of quality scores.
   */
  public Double getProfit(int day, String agent) {
    return this.profit.get(day, agent);
  }

  /**
   * Getter.
   * 
   * @param day
   * @return the map of quality scores for the given day.
   */
  public Map<String, Double> getQualityScores(int day) {
    return this.qualityScores.row(day);
  }

  /**
   * Register a campaign to an agent.
   * 
   * @param campaignId
   * @param agentName
   * @throws AdXException
   */
  public void registerCampaign(int day, Campaign campaign, String agentName) throws AdXException {
    this.checkAgentName(agentName);
    int campaignId = campaign.getId();
    InputValidators.validateCampaignId(campaignId);
    if (this.campaignsOwnership.containsKey(campaignId)) {
      throw new AdXException("Campaign: " + campaignId + " has already been registered to: " + agentName);
    }
    this.campaignsOwnership.put(campaignId, agentName);
    if (!this.agentsCampaings.contains(day, agentName)) {
      this.agentsCampaings.put(day, agentName, new ArrayList<Campaign>());
    }
    this.agentsCampaings.get(day, agentName).add(campaign);
  }

  /**
   * Given a campaign id, return true if that campaign has been registered and false otherwise.
   * 
   * @param campaignId
   * @return true if the campaign has been registered, false otherwise.
   */
  public boolean campaignExists(int campaignId) {
    return this.campaignsOwnership.containsKey(campaignId);
  }

  /**
   * Given a campaign Id and an agent name, return true if the agent owns the campaign.
   * 
   * @param campaignId
   * @param agentName
   * @return true if the agent owns the campaign.
   */
  public boolean isOwner(int campaignId, String agentName) {
    return this.campaignExists(campaignId) && this.campaignsOwnership.get(campaignId).equals(agentName);
  }

  /**
   * Returns a list of all campaigns owned by the agent on the given day.
   * 
   * @param day
   * @param agent
   * @return a list of all campaigns owned by the agent on the given day.
   */
  public List<Campaign> getWonCampaigns(int day, String agent) {
    return this.agentsCampaings.get(day, agent);
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
    InputValidators.validateDay(day);
    InputValidators.validateNotNull(query);
    this.checkAgentName(agent);
    this.checkCampaign(agent, campaignId);
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
    InputValidators.validateDay(day);
    InputValidators.validateNotNull(query);
    this.checkAgentName(agent);
    this.checkCampaign(agent, campaignId);
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
    InputValidators.validateDay(day);
    this.checkAgentName(agent);
    this.checkCampaign(agent, campaignId);
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

  /**
   * Printer.
   * 
   * @return a human readable representation of the quality scores.
   */
  public String printNiceQualityScoresTable() {
    String ret = "";
    if (this.qualityScores.size() > 0) {
      for (Entry<Integer, Map<String, Double>> x : this.qualityScores.rowMap().entrySet()) {
        ret += "\n\t\t Day: " + x.getKey();
        for (Entry<String, Double> y : x.getValue().entrySet()) {
          ret += "\n\t\t\t " + y.getKey() + " -> " + y.getValue();
        }
      }
    } else {
      ret += "Currently, no quality scores are registered";
    }
    return ret;
  }
  
  /**
   * Printer.
   * 
   * @return a human readable representation of the quality scores.
   */
  public String printNiceProfitScoresTable() {
    String ret = "";
    if (this.profit.size() > 0) {
      for (Entry<Integer, Map<String, Double>> x : this.profit.rowMap().entrySet()) {
        ret += "\n\t\t Day: " + x.getKey();
        for (Entry<String, Double> y : x.getValue().entrySet()) {
          ret += "\n\t\t\t " + y.getKey() + " -> " + y.getValue();
        }
      }
    } else {
      ret += "Currently, no profits are registered";
    }
    return ret;
  }

  /**
   * Printer.
   * 
   * @return a human readable representation of the map of campaign ownership.
   */
  public String printNiceCampaignOwnership() {
    String ret = "";
    if (this.campaignsOwnership.entrySet().size() > 0) {
      for (Entry<Integer, String> x : this.campaignsOwnership.entrySet()) {
        ret += "\n\t\t" + x.getKey() + " -> " + x.getValue();
      }
    } else {
      ret += "Currently, no campaign ownership is registered";
    }
    return ret;
  }

  /**
   * Printer.
   * 
   * @return a human readable representation of the campaign table.
   */
  public String printNiceCampaignTable() {
    String ret = "";
    if (this.agentsCampaings.rowMap().entrySet().size() > 0) {
      for (Entry<Integer, Map<String, List<Campaign>>> x : this.agentsCampaings.rowMap().entrySet()) {
        ret += "\n\t\t Day: " + x.getKey();
        for (Entry<String, List<Campaign>> y : x.getValue().entrySet()) {
          ret += "\n\t\t\t Agent: " + y.getKey();
          for (Campaign z : y.getValue()) {
            ret += "\n\t\t\t\t " + z;
          }
        }
      }
    } else {
      ret += "\n\t\t Currently, there are no campaigns.";
    }
    return ret;
  }

  /**
   * Printer.
   * 
   * @return a human readable representation of the ad statistics table.
   */
  public String printNiceAdStatisticsTable() {
    String ret = "";
    if (this.statistics.rowMap().entrySet().size() > 0) {
      for (Entry<Integer, Map<String, Table<Integer, Query, Pair<Integer, Double>>>> x : this.statistics.rowMap().entrySet()) {
        ret += "\n\t\t Day: " + x.getKey();
        for (Entry<String, Table<Integer, Query, Pair<Integer, Double>>> y : x.getValue().entrySet()) {
          ret += "\n\t\t\t Agent: " + y.getKey();
          for (Entry<Integer, Map<Query, Pair<Integer, Double>>> z : y.getValue().rowMap().entrySet()) {
            ret += "\n\t\t\t\t Campaign: " + z.getKey() + ", total " + this.summary.get(x.getKey(), y.getKey()).get(z.getKey());
            for (Entry<Query, Pair<Integer, Double>> w : z.getValue().entrySet()) {
              ret += "\n\t\t\t\t\t Query: " + w.getKey() + ", (" + w.getValue().getElement1() + ", " + w.getValue().getElement2() + ")";
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
