package adx.test;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

import adx.exceptions.AdXException;
import adx.server.ServerState;
import adx.structures.BidBundle;
import adx.structures.BidEntry;

public class ServerStateTest {

  @SuppressWarnings("serial")
  @Test (expected=AdXException.class) 
  public void testValidateBidBundle0() throws AdXException {
    // Test the validation of the bid bundle, case where an agent reports a campaign it does not owns.
    ServerState serverState = new ServerState(0);
    serverState.registerCampaign(1, "agent0");
    BidBundle bidBundle0 = new BidBundle(0, new HashSet<BidEntry>() {{add(new BidEntry(1, null, 0, 0));}}, null);
    serverState.validateBidBundle(bidBundle0, "wrong agent");
  }
  
  @SuppressWarnings("serial")
  @Test (expected=AdXException.class)   
  public void testValidateBidBundle1() throws AdXException {
    // Test the validation of the bid bundle, case where an agent reports a campaign that does not exists.
    ServerState serverState = new ServerState(0);
    serverState.registerCampaign(1, "agent0");
    BidBundle bidBundle0 = new BidBundle(0, new HashSet<BidEntry>() {{add(new BidEntry(10, null, 0, 0));}}, null);
    serverState.validateBidBundle(bidBundle0, "agent0");
  }
  
  @SuppressWarnings("serial")
  @Test 
  public void testValidateBidBundle2() throws AdXException {
    // Test the validation of the bid bundle, case when the agent report a campaign it owns.
    ServerState serverState = new ServerState(0);
    serverState.registerCampaign(1, "agent0");
    BidBundle bidBundle0 = new BidBundle(0, new HashSet<BidEntry>() {{add(new BidEntry(1, null, 0, 0));}}, null);
    assertTrue(serverState.validateBidBundle(bidBundle0, "agent0"));
  }
  
  @SuppressWarnings("serial")
  @Test 
  public void testValidateBidBundle3() throws AdXException {
    // Test the validation of the bid bundle, case when the agent report various campaigns it owns.
    ServerState serverState = new ServerState(0);
    serverState.registerCampaign(208, "agent1");
    serverState.registerCampaign(350, "agent1");
    BidBundle bidBundle0 = new BidBundle(0, new HashSet<BidEntry>() {{
      add(new BidEntry(208, null, 0, 0));
      add(new BidEntry(208, null, 0, 0));
      add(new BidEntry(350, null, 0, 0));}}, null);
    assertTrue(serverState.validateBidBundle(bidBundle0, "agent1"));
  }

  @Test (expected=AdXException.class) 
  public void testOverlappingRegistration() throws AdXException {
    ServerState serverState = new ServerState(0);
    serverState.registerCampaign(0, "agent0");
    serverState.registerCampaign(0, "agent0");
  }
  
}
