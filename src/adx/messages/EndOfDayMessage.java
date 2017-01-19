package adx.messages;

import java.util.List;
import java.util.Map;

import adx.structures.Campaign;
import adx.util.Pair;

/**
 * This message is used to inform the agents about the exact server time when bids for the day are no longer accepted.
 * 
 * @author Enrique Areyan Viqueira
 */
public class EndOfDayMessage {

  /**
   * Simulated day
   */
  private final int day;

  /**
   * Instant when bids are no longer accepted, server time.
   */
  private final String endOfDayTime;

  /**
   * Statistics containing, for each campaign, the winCount and winCost
   */
  private final Map<Integer, Pair<Integer, Double>> statistics;

  /**
   * A list of campaigns being up for auction.
   */
  private final List<Campaign> auctionedCampaings;

  /**
   * A list of campaigns the agent won.
   */
  private final List<Campaign> wonCampaings;

  /**
   * The current quality score.
   */
  private final double qualityScore;

  /**
   * The cumulative profit so far.
   */
  private final double cumulativeProfit;

  /**
   * Constructor.
   */
  public EndOfDayMessage() {
    super();
    this.day = -1;
    this.endOfDayTime = null;
    this.statistics = null;
    this.auctionedCampaings = null;
    this.wonCampaings = null;
    this.qualityScore = -1;
    this.cumulativeProfit = -1;
  }

  /**
   * Constructor.
   * 
   * @param day
   * @param endOfDayTime
   */
  public EndOfDayMessage(int day, String endOfDayTime, Map<Integer, Pair<Integer, Double>> statistics, List<Campaign> auctionedCampaings,
      List<Campaign> wonCampaings, double qualityScore, double cumulativeProfit) {
    this.day = day;
    this.endOfDayTime = endOfDayTime;
    this.statistics = statistics;
    this.auctionedCampaings = auctionedCampaings;
    this.wonCampaings = wonCampaings;
    this.qualityScore = qualityScore;
    this.cumulativeProfit = cumulativeProfit;
  }

  /**
   * Getter.
   * 
   * @return the day.
   */
  public int getDay() {
    return this.day;
  }

  /**
   * Getter.
   * 
   * @return the instant of time when bids are no longer accepted, server time.
   */
  public String getEndOfDayTime() {
    return this.endOfDayTime;
  }

  /**
   * Getter.
   * 
   * @return the list of campaigns up for auction.
   */
  public List<Campaign> getCampaignsForAuction() {
    return this.auctionedCampaings;
  }

  /**
   * Getter.
   * 
   * @return the list of campaigns won by the agent.
   */
  public List<Campaign> getCampaignsWon() {
    return this.wonCampaings;
  }

  /**
   * Getter.
   * 
   * @return the statistics received from the server.
   */
  public Map<Integer, Pair<Integer, Double>> getStatistics() {
    return this.statistics;
  }

  /**
   * Getter.
   * 
   * @return the quality score in effect for the agent.
   */
  public double getQualityScore() {
    return this.qualityScore;
  }

  /**
   * Getter.
   * 
   * @return the cumulative profit for the agent.
   */
  public double getCumulativeProfit() {
    return this.cumulativeProfit;
  }

  @Override
  public String toString() {
    return "\n\t EndOfDayMessage: \n\t\t Day: " + this.day + ", \n\t\t Time: " + this.endOfDayTime + ",\n\t\t Statistics: " + this.statistics
        + ", \n\t\t Campaigns up for auction: " + this.auctionedCampaings + ", \n\t\t Won campaigns: " + this.wonCampaings + "\n\t\t Quality Score = "
        + this.qualityScore + "\n\t\t Cumulative Profit: " + this.cumulativeProfit;
  }
}
