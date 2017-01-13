package adx.auctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import adx.exceptions.AdXException;
import adx.structures.Query;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Maintains statistics of ad auction.
 * 
 * @author Enrique Areyan Viqueira
 */
public class AdStatistics {

  /**
   * A table that maps: Days -> Agent -> Campaign -> Query -> <Win Count, Price>
   */
  private final Table<Integer, String, Table<Integer, Query, Tuple>> statistics;
  
  private final Table<Integer, String, Map<Integer, Tuple>> summary;

  /**
   * Constructor.
   */
  public AdStatistics() {
    this.statistics = HashBasedTable.create();
    this.summary = HashBasedTable.create();
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
    if(!this.statistics.contains(day, agent)) {
      this.statistics.put(day, agent, HashBasedTable.create());
      this.summary.put(day, agent, new HashMap<Integer, Tuple>());
    }
    if(!this.statistics.get(day, agent).contains(campaignId, query)) {
      this.statistics.get(day, agent).put(campaignId, query, new Tuple(winCount, winCost));
      if(this.summary.get(day, agent).get(campaignId) == null) {
        this.summary.get(day, agent).put(campaignId, new Tuple(winCount, winCost));
      } else {
        Tuple total = this.summary.get(day, agent).get(campaignId);
        total.winCost += winCost;
        total.winCount += winCount;
      }
    } else {
      throw new AdXException("The statistics for: (day = "+ day + ", agent = " + agent + ", campaign = " + campaignId +", query = " + query + "); have already been recorded.");
    }
  }
  
  @Override
  public String toString() {
    String ret = "\n Statistics Table:";
    for(Entry<Integer, Map<String, Table<Integer, Query, Tuple>>> x : this.statistics.rowMap().entrySet()) {
      ret += "\n\t Day: " + x.getKey();
      for(Entry<String, Table<Integer, Query, Tuple>> y : x.getValue().entrySet()) {
        ret += "\n\t\t Agent: " + y.getKey();
        for(Entry<Integer, Map<Query, Tuple>> z : y.getValue().rowMap().entrySet()) {
          ret += "\n\t\t\t Campaign: " + z.getKey() + ", total " + this.summary.get(x.getKey(), y.getKey()).get(z.getKey());
          for(Entry<Query, Tuple> w : z.getValue().entrySet()) {
            ret += "\n\t\t\t\t Query: " + w.getKey() + ", (" + w.getValue().winCount + "," + w.getValue().winCost + ")";
          }
        }
      }
    }
    return ret;
  }
  
  /**
   * A simple internal tuple class to maintain
   * (winCount, winCost)
   * 
   * @author Enrique Areyan Viqueira
   */
  private final class Tuple {
    int winCount;
    double winCost;
    
    /**
     * Constructor.
     * 
     * @param winCount
     * @param winCost
     */
    public Tuple(int winCount, double winCost) {
      this.winCount = winCount;
      this.winCost = winCost;
    }
    
    public String toString() {
      return "(" + this.winCount + "," + this.winCost + ")";
    }
  }
}
