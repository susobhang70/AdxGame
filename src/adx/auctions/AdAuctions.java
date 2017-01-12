package adx.auctions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import adx.structures.BidBundle;
import adx.structures.Query;

/**
 * Methods to run second price auctions on bid bundles.
 * 
 * @author Enrique Areyan Viqueira
 */
public class AdAuctions {

  /**
   * Given a query and a map of bidbundles, select and return those bids that
   * match the query.
   * 
   * @param day
   * @param query
   * @param bidBundles
   */
  public static void filterBids(Query query, Map<String, BidBundle> bidBundles) {

    System.out.println("Filtered bids = " + bidBundles);
    for (BidBundle x : bidBundles.values()) {
      System.out.println(x);
    }

  }

  public static void runSecondPriceAuction(int supply, List<Entry<String, BidBundle>> bids, Map<Integer, Double> limits) {

  }

}
