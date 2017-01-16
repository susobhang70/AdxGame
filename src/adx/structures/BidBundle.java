package adx.structures;

import java.util.Map;
import java.util.Set;

/**
 * This is the main structure by which bids on queries are communicated to the
 * server.
 * 
 * @author Enrique Areyan Viqueira
 */
public class BidBundle {

  /**
   * Day the bid bundle was sent for.
   */
  private final int day;

  /**
   * A bid bundle is composed of bid entries.
   */
  private final Set<BidEntry> bidEntries;

  /**
   * This map stores global limits on expenditure
   */
  private final Map<Integer, Double> campaignsLimits;

  /**
   * Constructor.
   */
  public BidBundle() {
    super();
    this.day = -1;
    this.bidEntries = null;
    this.campaignsLimits = null;
  }

  /**
   * Constructor.
   * 
   * @param day
   * @param bidEntries
   * @param campaignLimits
   */
  public BidBundle(int day, Set<BidEntry> bidEntries, Map<Integer, Double> campaignLimits) {
    super();
    this.day = day;
    this.bidEntries = bidEntries;
    this.campaignsLimits = campaignLimits;
  }

  /**
   * Getter.
   * 
   * @return the simulated day for which this bid bundle was constructed.
   */
  public int getDay() {
    return this.day;
  }

  /**
   * Getter.
   * 
   * @return the set of bid entries of this bid bundle.
   */
  public Set<BidEntry> getBidEntries() {
    return this.bidEntries;
  }

  /**
   * Getter.
   * 
   * @param campaignId
   * @return the limit expenditure for the day for the given campaign.
   */
  public double getCampaignLimit(int campaignId) {
    return this.campaignsLimits.get(campaignId);
  }

  @Override
  public String toString() {
    return "[Day " + this.day + ", entries = " + this.bidEntries + ", limits = " + this.campaignsLimits + "]";
  }
}
