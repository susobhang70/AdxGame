package adx.onedaygame;

import adx.exceptions.AdXException;
import adx.structures.BidEntry;
import adx.structures.Query;

/**
 * This class represents an entry of a one day bid bundle.
 * 
 * @author Enrique Areyan Viqueira
 */
public class OneDayBidEntry extends BidEntry {

  /**
   * Constructor.
   */
  public OneDayBidEntry() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param query
   * @param bid
   * @param limit
   * @throws AdXException
   */
  public OneDayBidEntry(Query query, double bid, double limit) throws AdXException {
    // The Integer.MAX_VALUE is just a place holder, this value will get replaced later.
    super(Integer.MAX_VALUE, query, bid, limit);
  }
  
  @Override
  public String toString() {
    return "OneDayBidEntry = [query = " + this.query + ", bid = " + bid + ", limit = " + this.limit + "]";
  }

}
