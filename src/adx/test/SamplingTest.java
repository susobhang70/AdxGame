package adx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import adx.exceptions.AdXException;
import adx.structures.Campaign;
import adx.structures.Query;
import adx.util.Sampling;

public class SamplingTest {

  @Test
  public void testSamplePopulation() {
    for (int i = 10; i < 1000; i++) {
      HashMap<Query, Integer> population = Sampling.samplePopulation(i);
      int total = 0;
      for(int n : population.values()) {
        total += n;
      }
      assertEquals(i, total);
    }
  }

  @Test
  public void testSampleInitialCampaign() throws AdXException {
    assertTrue(Sampling.sampleInitialCampaign() instanceof Campaign);
  }

  @Test
  public void testSampleCampaign() throws AdXException {
    assertTrue(Sampling.sampleCampaign() instanceof Campaign);
  }

}
