package adx.onedaygame;

import adx.agent.Agent;
import adx.messages.EndOfDayMessage;
import adx.structures.Campaign;
import adx.util.Logging;
import adx.util.Printer;

/**
 * An abstract class to be implemented by an agent playing the OneDay game.
 * 
 * @author Enrique Areyan Viqueira
 */
abstract public class OneDayAgent extends Agent {

  /**
   * In this game agents have only one campaign.z
   */
  protected Campaign myCampaign;

  /**
   * Constructor.
   * 
   * @param host
   * @param port
   */
  public OneDayAgent(String host, int port) {
    super(host, port);
  }

  @Override
  protected void handleEndOfDayMessage(EndOfDayMessage endOfDayMessage) {
    int curr_day = endOfDayMessage.getDay();
    if (curr_day == 1) {
      this.myCampaign = endOfDayMessage.getCampaignsWon().get(0);
      Logging.log("[-] My campaign: " + this.myCampaign);
      this.getClient().sendTCP(this.getBidBundle());
    } else {
      Logging.log("[-] Statistics: " + Printer.getNiceStatsTable(endOfDayMessage.getStatistics()));
      Logging.log("[-] Final Profit: " + endOfDayMessage.getCumulativeProfit());
    }
  }

  /**
   * This is the only function that needs to be implemented by an agent playing the OneDay Game.
   * 
   * @return the agent's bid bundle.
   */
  abstract protected OneDayBidBundle getBidBundle();

}
