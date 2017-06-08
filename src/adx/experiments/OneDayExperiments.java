package adx.experiments;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import adx.exceptions.AdXException;
import adx.sim.Simulator;
import adx.sim.agents.SimAgent;
import adx.sim.agents.WE.WEAgent;
import adx.statistics.Statistics;
import adx.util.Logging;

public class OneDayExperiments {

  public static void main(String[] args) throws AdXException {

    // Setup agents and statistics handler
    List<SimAgent> simAgents = new ArrayList<SimAgent>();
    //Map<String, DescriptiveStatistics> stats = new HashMap<String, DescriptiveStatistics>();
    DescriptiveStatistics stats = new DescriptiveStatistics();
    for (int i = 0; i < 8; i++) {
      // Walrasian Agents
      simAgents.add(new WEAgent("WEAgent" + i));
      // stats.put("WEAgent" + i, new DescriptiveStatistics());

      // Waterfall Agents
      //simAgents.add(new WFAgent("WFAgent" + i));
      //stats.put("WFAgent" + i, new DescriptiveStatistics());
    }
    for (int t = 0; t < 10000; t++) {
      // Run simulator.
      Simulator simulator = new Simulator(simAgents);
      // Get statistics.
      Statistics statistics = simulator.run();
      for (int i = 0; i < 8; i++) {
        Double profit = statistics.getProfit(1, "WEAgent" + i);
        //stats.get("WEAgent" + i).addValue(profit);
        stats.addValue(profit);
        Logging.log("WEAgent" + i + ", " + profit);

        //Double profit = statistics.getProfit(1, "WFAgent" + i);
        //stats.get("WFAgent" + i).addValue(profit);
        //stats.addValue(profit);
        //Logging.log("WFAgent" + i + ", " + profit);
      }
    }
    //for (int i = 0; i < 8; i++) {
      // Walrasian Agents
      //Logging.log("WEAgent" + i + " average profit = " + stats.get("WEAgent" + i).getMean() + ", stdev = " + stats.get("WEAgent" + i).getStandardDeviation());
      
      // Waterfall Agents
      //Logging.log("WFAgent" + i + " average profit = " + stats.get("WFAgent" + i).getMean() + ", stdev = " + stats.get("WFAgent" + i).getStandardDeviation());
    Logging.log("WEAgent average profit = " + stats.getMean() + ", stdev = " + stats.getStandardDeviation());
    //Logging.log("WFAgent average profit = " + stats.getMean() + ", stdev = " + stats.getStandardDeviation());
    //}

  }

}
