package adx.sim.agents;

import java.util.List;

import structures.Bidder;
import structures.Market;
import adx.structures.BidBundle;
import adx.structures.Campaign;
import adx.util.Logging;

/**
 * Abstract class to be extended by a simulated agent.
 * 
 * @author Enrique Areyan Viqueira
 */
public abstract class SimAgent {

  /**
   * A name for the simAgent
   */
  protected String simAgentName;

  /**
   * The agent's campaign.
   */
  protected Campaign myCampaign;

  /**
   * The other agent's campaigns.
   */
  protected List<Campaign> othersCampaigns;

  /**
   * Constructor.
   * 
   * @param simAgentName
   */
  public SimAgent(String simAgentName) {
    this.simAgentName = simAgentName;
  }

  /**
   * Getter.
   * 
   * @return the simAgent name.
   */
  public String getName() {
    return this.simAgentName;
  }

  /**
   * Setter.
   * 
   * @param campaign
   */
  public void setCampaigns(Campaign campaign, List<Campaign> othersCampaigns) {
    this.myCampaign = campaign;
    this.othersCampaigns = othersCampaigns;
    // Logging.log(this.simAgentName);
    // Logging.log(this.myCampaign);
    // Logging.log(this.othersCampaigns);
  }

  /**
   * The main method to be implemented by a SimAgent.
   * 
   * @return a BidBundle
   */
  public abstract BidBundle getBidBundle();

  /**
   * Helper method to print information, mainly about the model.
   */
  protected void printInfo(String name, Market<GameGoods, Bidder<GameGoods>> market) {
    Logging.log("***********" + name + "************");
    Logging.log("My Campaign: \n\t " + this.myCampaign);
    Logging.log("Others Campaigns: ");
    for (Campaign c : this.othersCampaigns) {
      Logging.log("\t " + c);
    }
    Logging.log("List of Goods = ");
    for (GameGoods gameGood : SimAgentModel.listOfGameGoods) {
      Logging.log("\t " + gameGood);
    }
    Logging.log("\n ------ Model: \n" + market + "\n ---------");
  }

}
