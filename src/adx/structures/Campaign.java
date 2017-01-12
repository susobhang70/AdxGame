package adx.structures;

import adx.exceptions.AdXException;

/**
 * Represents a campaign of the game.
 * 
 * @author Enrique Areyan Viqueira
 */
public class Campaign {

  /**
   * A unique identifier of the campaign.
   */
  protected final int id;

  /**
   * Campaign's market segment.
   */
  protected final MarketSegment marketSegment;

  /**
   * A campaign must reach a positive number of people.
   */
  protected final int reach;

  /**
   * A campaign has a budget represented by a double.
   */
  protected double budget = -1.0;

  /**
   * Constructor.
   */
  public Campaign() {
    super();
    this.id = -1;
    this.marketSegment = null;
    this.reach = -1;
    this.budget = -1;
  }

  /**
   * Constructor.
   * 
   * @param budget - this campaign's budget.
   * @param reach - this campaign's reach.
   * @throws AdXException in case either budget or reach are non-positive.
   */
  public Campaign(int id, MarketSegment marketSegment, int reach) throws AdXException {
    if (id <= 0) {
      throw new AdXException("The id of a campaign must be a positive integer");
    }
    if (marketSegment == null) {
      throw new AdXException("The market segment of a campaign cannot be null");
    }
    if (reach <= 0) {
      throw new AdXException("The reach of a campaign must be a positive integer");
    }
    this.id = id;
    this.marketSegment = marketSegment;
    this.reach = reach;
  }

  /**
   * Sets budget. This method can only be called once.
   * 
   * @param budget
   * @throws AdXException in case the method is called more than once.
   */
  public void setBudget(double budget) throws AdXException {
    if (this.budget == -1.0) {
      if (budget <= 0.0) {
        throw new AdXException("The budget of a campaign must be a positive double");
      } else {
        this.budget = budget;
      }
    } else {
      throw new AdXException("A campaign budget can only be set once");
    }
  }
  
  /**
   * Getter.
   * 
   * @return the campaign's unique identifier.
   */
  public int getId() {
    return this.id;
  }
  
  /**
   * Getter.
   * 
   * @return the campaign's market segment.
   */
  public MarketSegment getMarketSegment() {
    return this.marketSegment;
  }
  /**
   * Getter.
   * 
   * @return the campaign's reach.
   */
  public int getReach() {
    return this.reach;
  }

  /**
   * Getter.
   * 
   * @return the campaign's budget.
   */
  public double getBudget() {
    return this.budget;
  }
  @Override
  public String toString() {
    return "Campaign " + this.id + " = [Segment = " + this.marketSegment
        + ", Reach = " + this.reach + ", Budget = " + this.budget + "]";
  }

}
