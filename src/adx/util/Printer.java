package adx.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import adx.structures.Campaign;

/**
 * A class that implements static methods for string representation of complicated object.
 * 
 * @author Enrique Areyan Viqueira
 *
 */
public class Printer {

  /**
   * String representation of a list of campaigns.
   * 
   * @param listOfCampaigns
   * @return
   */
  public static String printNiceListMyCampaigns(List<Campaign> listOfCampaigns) {
    String ret = "";
    if (listOfCampaigns != null && listOfCampaigns.size() > 0) {
      for (Campaign c : listOfCampaigns) {
        ret += "\n\t\t" + c;
      }
    } else {
      ret += "No campaigns in the give list.";
    }
    return ret;
  }

  /**
   * String representation of statistics.
   * 
   * @param stats
   * @return
   */
  public static String getNiceStatsTable(Map<Integer, Pair<Integer, Double>> stats) {
    String ret = "";
    for (Entry<Integer, Pair<Integer, Double>> x : stats.entrySet()) {
      ret += "\n\tCampaign " + x.getKey() + ", Total Reach = " + x.getValue().getElement1() + ", Total Cost " + x.getValue().getElement2();
    }
    return ret;
  }

}
