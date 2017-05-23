package adx.sim;

import java.util.ArrayList;
import java.util.List;

import adx.exceptions.AdXException;
import adx.server.ServerState;
import adx.sim.agents.SimAgent;
import adx.sim.agents.SimpleSimAgent;
import adx.sim.agents.WEAgent;
import adx.structures.BidBundle;
import adx.structures.Campaign;
import adx.util.Pair;
import adx.util.Sampling;

/**
 * This class simulates the game. This is used for experimental purposes.
 * 
 * @author Enrique Areyan Viqueira
 */
public class Simulator {

  /**
   * A simulation has a fixed list of SimAgents.
   */
  private final List<SimAgent> agents;

  /**
   * Keep a server state
   */
  private final ServerState serverState;

  /**
   * Constructor.
   * 
   * @param agents
   * @throws AdXException
   */
  public Simulator(List<SimAgent> agents) throws AdXException {
    this.agents = agents;
    this.serverState = new ServerState(0);
    for (SimAgent simAgent : agents) {
      this.serverState.registerAgent(simAgent.getName());
    }
  }

  /**
   * Run the simulation.
   * 
   * @throws AdXException
   */
  public void run() throws AdXException {

    this.serverState.initStatistics();

    // Sample and distribute campaigns
    List<Campaign> allCampaigns = new ArrayList<Campaign>();
    for (int j = 0; j < this.agents.size(); j++) {
      allCampaigns.add(Sampling.sampleInitialCampaign());
    }
    int i = 0;
    for (SimAgent agent : this.agents) {
      List<Campaign> otherCampaigns = new ArrayList<Campaign>(allCampaigns);
      Campaign agentCampaign = otherCampaigns.get(i);
      this.serverState.registerCampaign(agentCampaign, agent.getName());
      otherCampaigns.remove(agentCampaign);
      i++;
      agent.setCampaigns(agentCampaign, otherCampaigns);
    }
    // this.serverState.printServerState();

    // Ask for bids
    for (SimAgent agent : this.agents) {
      BidBundle bidBundle = agent.getBidBundle();
      Pair<Boolean, String> bidBundleAccept = this.serverState.addBidBundle(bidBundle.getDay(), agent.getName(), bidBundle);
      if (!bidBundleAccept.getElement1()) {
        throw new AdXException("Bid bundle not accepted. Server replied: " + bidBundleAccept.getElement2());
      }
    }
    this.serverState.advanceDay();
    // Run auctions
    this.serverState.runAdAuctions();
    this.serverState.updateDailyStatistics();
    // Report results
    this.serverState.printServerState();
  }

  public static void main(String[] args) throws AdXException {
    List<SimAgent> simAgents = new ArrayList<SimAgent>();
    for (int i = 0; i < 5; i++) {
      simAgents.add(new SimpleSimAgent("OneDayAgent" + i));
    }
    simAgents.add(new WEAgent("WEAgent"));
    Simulator simulator = new Simulator(simAgents);
    simulator.run();
  }

}
