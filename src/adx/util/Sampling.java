package adx.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import adx.exceptions.AdXException;
import adx.structures.Campaign;
import adx.structures.MarketSegment;
import adx.structures.Query;

/**
 * This class implements logic to sample a population of users given their
 * proportions.
 * 
 * @author Enrique Areyan Viqueira
 */
public class Sampling {

  /**
   * Contains a map pointing to the market segments to be sample.
   */
  public static final Map<MarketSegment, Integer> segmentsToSample;
  
  /**
   * Contains a list of market segment and its cumulative probability.
   */
  public static ArrayList<Entry<MarketSegment, Integer>> cumulativeMarketSegments;
  
  /**
   * Total proportion of all users. Usually this number is 10000.
   */
  public static Integer totalProportion = 0;
  
  /**
   * Different campaign reach factors.
   */
  private static final double[] campaignReachFactor = { 0.2, 0.5, 0.8 };
  
  /**
   * A unique identifier for campaigns ids.
   */
  public static int campaignId = 0;
  
  static {
    // Initialize static structures only once.
    HashSet<MarketSegment> setOfSegments = new HashSet<MarketSegment>();
    setOfSegments.add(MarketSegment.MALE_YOUNG_LOW_INCOME);
    setOfSegments.add(MarketSegment.MALE_YOUNG_HIGH_INCOME);
    setOfSegments.add(MarketSegment.MALE_OLD_LOW_INCOME);
    setOfSegments.add(MarketSegment.MALE_OLD_HIGH_INCOME);
    setOfSegments.add(MarketSegment.FEMALE_YOUNG_LOW_INCOME);
    setOfSegments.add(MarketSegment.FEMALE_YOUNG_HIGH_INCOME);
    setOfSegments.add(MarketSegment.FEMALE_OLD_LOW_INCOME);
    setOfSegments.add(MarketSegment.FEMALE_OLD_HIGH_INCOME);
    
    HashMap<MarketSegment, Integer> initSegmentsToSample = new HashMap<MarketSegment, Integer>();
    for(MarketSegment m : setOfSegments) {
      initSegmentsToSample.put(m, MarketSegment.proportionsMap.get(m));
    }
    segmentsToSample = Collections.unmodifiableMap(initSegmentsToSample);
    Sampling.cumulativeMarketSegments = new ArrayList<Entry<MarketSegment, Integer>>();
    // Construct a list with the cumulative proportions.
    for (MarketSegment m : Sampling.segmentsToSample.keySet()) {
      Sampling.totalProportion += Sampling.segmentsToSample.get(m);
      Sampling.cumulativeMarketSegments.add(new AbstractMap.SimpleEntry<MarketSegment, Integer>(m, totalProportion));
    }
  }

  /**
   * Given a size of a population n, produces a random population of n users
   * according to the parameters of the game.
   * 
   * @param n
   * @throws AdXException 
   */
  // TODO: this method could be optimized. Since the cumulative distribution
  // is fixed, create an array (or map) of size 10000 that maps directly to the market segment.
  public static final HashMap<Query, Integer> samplePopulation(int n) throws AdXException {
    // Construct the sample. Initially there are zero users in each market segment.
    HashMap<Query, Integer> population = new HashMap<Query, Integer>();
    for (MarketSegment m : Sampling.segmentsToSample.keySet()) {
      population.put(new Query(m), 0);
    }
    Random random = new Random();
    // Sample one user at a time.
    for (int i = 0; i < n; i++) {
      int r = random.nextInt(Sampling.totalProportion) + 1;
      for (Entry<MarketSegment, Integer> x : Sampling.cumulativeMarketSegments) {
        if (r <= x.getValue()) {
          Query query = new Query(x.getKey());
          population.put(query, population.get(query) + 1);
          break;
        }
      }
    }
    return population;
  }
  
  /**
   * Samples a campaign to be sent to agents at the start of the game.
   * 
   * @return an initial day campaign where reach equals budget.
   * @throws AdXException
   */
  public static Campaign sampleInitialCampaign() throws AdXException {
    // The only difference is that the budget equals 1$ per impression.
    Campaign initialCampaign = Sampling.sampleCampaign();
    initialCampaign.setBudget(initialCampaign.getReach());
    return initialCampaign;
  }
  
  /**
   * Samples a campaign among all possible market segments (1, 2 or 3 letter segments)
   * 
   * @param day
   * @return a campaign with a random reach and market segment.
   * @throws AdXException
   */
  public static Campaign sampleCampaign() throws AdXException {
    return Sampling.sampleCampaignOpportunityMessage(MarketSegment.proportionsList);
  }
  
  /**
   * Samples a list of n campaign. 
   * 
   * @param n
   * @return a list of n random campaigns.
   * @throws AdXException
   */
  public static List<Campaign> sampleCampaingList(int n) throws AdXException {
    ArrayList<Campaign> campaignsList = new ArrayList<Campaign>();
    for(int i = 0 ; i < n; i++) {
      campaignsList.add(Sampling.sampleCampaign());
    }
    return campaignsList;
  }
  
  /**
   * This method draws a campaign as the game would.
   * 
   * @param currentDay
   * @return a CampaignOpportunityMessage with a sample campaign.
   * @throws AdXException
   */
  private static Campaign sampleCampaignOpportunityMessage(List<Entry<MarketSegment, Integer>> candidateSegments) throws AdXException {
    Random randomGenerator = new Random();
    // Get a random Market Segment and the number of users |C_S| in that segment.
    Entry<MarketSegment, Integer> randomEntry = candidateSegments.get(randomGenerator.nextInt(candidateSegments.size()));
    MarketSegment randomSegment = randomEntry.getKey();
    int sizeOfRandomSegment = randomEntry.getValue();
    // Determine the random campaign reach level factor C_RL.
    double randomReachFactor = Sampling.campaignReachFactor[randomGenerator.nextInt(Sampling.campaignReachFactor.length)];
    // Compute the actual reach (see game specs, C_R = C_RL * |C_S|.
    int reach = (int) Math.floor(randomReachFactor * sizeOfRandomSegment);
    return new Campaign(++Sampling.campaignId, randomSegment, reach);
  }

}
