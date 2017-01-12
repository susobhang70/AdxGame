package adx.structures;

/**
 * A query represents a market segment together with all possible attributes
 * such as: publisher device and adtype.
 * 
 * @author Enrique Areyan Viqueira
 */
public class Query {

  /**
   * This query's market segment.
   */
  private final MarketSegment marketSegment;
  
  public Query() {
    super();
    this.marketSegment = null;
  }

  /**
   * Constructor.
   * 
   * @param marketSegment
   */
  public Query(MarketSegment marketSegment) {
    this.marketSegment = marketSegment;
  }

  /**
   * Getter.
   * 
   * @return this query's market segment.
   */
  public MarketSegment getMarketSegment() {
    return this.marketSegment;
  }
  
  @Override
  public String toString() {
    return this.marketSegment.name();
  }

}
