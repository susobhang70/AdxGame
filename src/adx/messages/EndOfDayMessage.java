package adx.messages;

/**
 * This message is used to inform the agents about the exact server time when
 * bids for the day are no longer accepted.
 * 
 * @author Enrique Areyan Viqueira
 */
public class EndOfDayMessage {

  /**
   * Simulated day
   */
  private final int day;

  /**
   * Instant when bids are no longer accepted, server time.
   */
  private final String endOfDayTime;

  /**
   * Constructor.
   */
  public EndOfDayMessage() {
    super();
    this.day = -1;
    this.endOfDayTime = null;
  }

  /**
   * Constructor.
   * 
   * @param day
   * @param endOfDayTime
   */
  public EndOfDayMessage(int day, String endOfDayTime) {
    this.day = day;
    this.endOfDayTime = endOfDayTime;
  }

  /**
   * Getter.
   * 
   * @return the day.
   */
  public int getDay() {
    return this.day;
  }

  /**
   * Getter.
   * 
   * @return the instant of time when bids are no longer accepted, server time.
   */
  public String getEndOfDayTime() {
    return this.endOfDayTime;
  }

  @Override
  public String toString() {
    return "[EndOfDayMessage: day = " + this.day + ", time =" + this.endOfDayTime + "]";
  }
}
