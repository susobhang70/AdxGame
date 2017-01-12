package adx.messages;

import adx.structures.Campaign;

/**
 * This class represents a message sent once by the server to each agent at the
 * beginning of the game.
 * 
 * @author Enrique Areyan Viqueira
 */
public class InitialMessage {

  /**
   * Stores an agent's initial campaign.
   */
  private final Campaign initialCampaign;
  
  /**
   * Stores the game number provided by the server.
   */
  private final int gameNumber;

  /**
   * Constructor.
   */
  public InitialMessage() {
    super();
    this.initialCampaign = null;
    this.gameNumber = -1;
  }

  /**
   * Constructor. 
   * 
   * @param initialCampaign
   */
  public InitialMessage(Campaign initialCampaign, int gameNumber) {
    super();
    this.initialCampaign = initialCampaign;
    this.gameNumber = gameNumber;
  }

  /**
   * Getter.
   * 
   * @return the initial campaign store by this message.
   */
  public Campaign getInitialCampaign() {
    return this.initialCampaign;
  }
  
  /**
   * Getter.
   * 
   * @return the response numerical code from the server.
   */
  public int getGameNumber() {
    return this.gameNumber;
  }

  @Override
  public String toString() {
    return "[Initial Message, game is "+this.gameNumber+", initial campaign is: " + this.initialCampaign + "]";
  }
}
