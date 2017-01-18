package adx.structures;

import java.util.Map;
import java.util.Set;

import adx.exceptions.AdXException;

/**
 * This is the main structure by which bids on queries are communicated to the server.
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
   * This map stores bids of campaign as (CampaignId, Bid).
   */
  private final Map<Integer, Double> campaignsBid;

  /**
   * Constructor.
   */
  public BidBundle() {
    super();
    this.day = -1;
    this.bidEntries = null;
    this.campaignsLimits = null;
    this.campaignsBid = null;
  }

  /**
   * Constructor.
   * 
   * @param day
   * @param bidEntries
   * @param campaignLimits
   * @throws AdXException
   */
  public BidBundle(int day, Set<BidEntry> bidEntries, Map<Integer, Double> campaignsLimits, Map<Integer, Double> campaignsBid) throws AdXException {
    super();
    if (day < 0) {
      throw new AdXException("The day must be a non-negative integer.");
    }
    this.day = day;
    if (bidEntries == null) {
      throw new AdXException("The bidEntries must be non-null");
    }
    this.bidEntries = bidEntries;
    this.campaignsLimits = campaignsLimits;
    this.campaignsBid = campaignsBid;
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
  public Double getCampaignLimit(int campaignId) {
    if (!(this.campaignsLimits == null)) {
      return this.campaignsLimits.get(campaignId);
    } else {
      return null;
    }
  }

  /**
   * Returns the bid for the given campaign id.
   * 
   * @param campaignId
   * @return the bid for the campaign
   */
  public Double getCampaignBid(int campaignId) {
    if (this.campaignsBid == null) {
      return null;
    }
    return this.campaignsBid.get(campaignId);
  }

  @Override
  public String toString() {
    String ret = "[Day " + this.day;
    if (this.bidEntries != null)
      ret += ", entries = " + this.bidEntries;
    if (this.campaignsLimits != null)
      ret += ", campaigns limits = " + this.campaignsLimits;
    if (this.campaignsBid != null)
      ret += ", campaigns bid = " + this.campaignsBid;
    return ret + "]";
  }
}
