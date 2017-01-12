package adx.structures;

/**
 * A bid entry is a constituent part of a BidBundle.
 * 
 * @author Enrique Areyan Viqueira
 */
public class BidEntry {

  /**
   * The campaign id to which this entry refers.
   */
  private final int campaignId;

  /**
   * The query of this entry.
   */
  private final Query query;

  /**
   * The bid placed on behalf of the campaign on this BidEntry's query.
   */
  private final double bid;

  /**
   * A limit on expenditure by this campaign on this query.
   */
  private final double limit;

  /**
   * Constructor.
   */
  public BidEntry() {
    super();
    this.campaignId = -1;
    this.query = null;
    this.bid = -1;
    this.limit = -1;
  }

  /**
   * Constructor.
   * 
   * @param campaignId
   * @param query
   * @param bid
   * @param limit
   */
  public BidEntry(int campaignId, Query query, double bid, double limit) {
    super();
    this.campaignId = campaignId;
    this.query = query;
    this.bid = bid;
    this.limit = limit;
  }

  /**
   * Getter.
   * 
   * @return the campaign id.
   */
  public int getCampaignId() {
    return this.campaignId;
  }

  /**
   * Getter.
   * 
   * @return the query.
   */
  public Query getQuery() {
    return this.query;
  }

  /**
   * Getter.
   * 
   * @return the bid.
   */
  public double getBid() {
    return this.bid;
  }

  /**
   * Getter.
   * 
   * @return the limit expenditure.
   */
  public double getLimit() {
    return this.limit;
  }

  @Override
  public String toString() {
    return "[Query: campaign id = " + this.campaignId + ", query = "
        + this.query + ", bid = " + bid + ", limit = " + this.limit + "]";
  }

}