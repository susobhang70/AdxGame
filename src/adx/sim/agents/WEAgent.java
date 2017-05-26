package adx.sim.agents;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structures.Bidder;
import structures.Market;
import structures.MarketAllocation;
import structures.exceptions.AllocationException;
import structures.exceptions.BidderCreationException;
import structures.exceptions.GoodsCreationException;
import structures.exceptions.GoodsException;
import structures.exceptions.MarketAllocationException;
import structures.exceptions.MarketCreationException;
import structures.exceptions.MarketOutcomeException;
import adx.exceptions.AdXException;
import adx.structures.BidBundle;
import adx.structures.Campaign;
import adx.structures.MarketSegment;
import adx.structures.SimpleBidEntry;
import adx.util.Logging;
import adx.variants.onedaygame.OneDayBidBundle;
import algorithms.pricing.RestrictedEnvyFreePricesLP;
import algorithms.pricing.RestrictedEnvyFreePricesLPSolution;
import algorithms.pricing.error.PrincingAlgoException;
import allocations.greedy.GreedyAllocation;

import com.google.common.collect.ImmutableList;

/**
 * Implements the Walrasian Equilibrium (WE) agent.
 * 
 * @author Enrique Areyan Viqueira
 */
public class WEAgent extends SimAgent {

  /**
   * Immutable list of goods.
   */
  private static ImmutableList<GameGoods> listOfGameGoods;

  /**
   * Additive factor for the bids.
   */
  private static final double epsilon = 0.1;

  /**
   * Static structures.
   */
  static {
    // Create the market segments just once.
    List<MarketSegment> marketSegments = new ArrayList<MarketSegment>();
    marketSegments.add(MarketSegment.MALE_YOUNG_LOW_INCOME);
    marketSegments.add(MarketSegment.MALE_YOUNG_HIGH_INCOME);
    marketSegments.add(MarketSegment.MALE_OLD_LOW_INCOME);
    marketSegments.add(MarketSegment.MALE_OLD_HIGH_INCOME);
    marketSegments.add(MarketSegment.FEMALE_YOUNG_LOW_INCOME);
    marketSegments.add(MarketSegment.FEMALE_YOUNG_HIGH_INCOME);
    marketSegments.add(MarketSegment.FEMALE_OLD_LOW_INCOME);
    marketSegments.add(MarketSegment.FEMALE_OLD_HIGH_INCOME);
    List<GameGoods> gameGoods = new ArrayList<GameGoods>();
    for (MarketSegment marketSegment : marketSegments) {
      try {
        gameGoods.add(new GameGoods(marketSegment, MarketSegment.proportionsMap.get(marketSegment)));
      } catch (GoodsCreationException e) {
        Logging.log("Error creating goods in the market model");
        e.printStackTrace();
      }
    }
    ImmutableList.Builder<GameGoods> gameMarketSegmentBuilder = ImmutableList.builder();
    gameMarketSegmentBuilder.addAll(gameGoods);
    WEAgent.listOfGameGoods = gameMarketSegmentBuilder.build();
  }

  /**
   * Constructor.
   * 
   * @param simAgentName
   */
  public WEAgent(String simAgentName) {
    super(simAgentName);
  }

  @Override
  public BidBundle getBidBundle() {
    // Print some useful info
    Logging.log(this.myCampaign);
    Logging.log(this.othersCampaigns);
    Logging.log(WEAgent.listOfGameGoods);

    try {
      // Construct the model
      ArrayList<Bidder<GameGoods>> listOfBidders = new ArrayList<Bidder<GameGoods>>();
      Set<GameGoods> myDemandSet = this.generateDemandSet(this.myCampaign);
      Bidder<GameGoods> myCampaignBidder = new Bidder<GameGoods>(this.myCampaign.getReach(), this.myCampaign.getBudget(), myDemandSet);
      listOfBidders.add(myCampaignBidder);
      for (Campaign othersCampaigns : this.othersCampaigns) {
        listOfBidders.add(new Bidder<GameGoods>(othersCampaigns.getReach(), othersCampaigns.getBudget(), this.generateDemandSet(othersCampaigns)));
      }
      Market<GameGoods, Bidder<GameGoods>> market = new Market<GameGoods, Bidder<GameGoods>>(WEAgent.listOfGameGoods, listOfBidders);
      Logging.log(market);

      // Run allocation algorithm
      MarketAllocation<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>> greedyAllocation = new GreedyAllocation<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>>().Solve(market);
      greedyAllocation.printAllocation();

      // Run pricing algorithm
      RestrictedEnvyFreePricesLP<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>> restrictedEnvyFreePricesLP = new RestrictedEnvyFreePricesLP<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>>(greedyAllocation);
      // restrictedEnvyFreePricesLP.setMarketClearanceConditions(true);
      restrictedEnvyFreePricesLP.createLP();
      RestrictedEnvyFreePricesLPSolution<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>> prices = restrictedEnvyFreePricesLP.Solve();
      prices.printPrices();

      // Back-up bid from prices
      Set<SimpleBidEntry> bidEntries = new HashSet<SimpleBidEntry>();
      for (GameGoods demandedGood : myDemandSet) {
        Logging.log("Price for: " + demandedGood + " is " + prices.getPrice(demandedGood));
        if (greedyAllocation.getAllocation(demandedGood, myCampaignBidder) > 0) {
          bidEntries.add(new SimpleBidEntry(demandedGood.getMarketSegment(), prices.getPrice(demandedGood) + WEAgent.epsilon, greedyAllocation.getAllocation(demandedGood, myCampaignBidder) * (prices.getPrice(demandedGood) + WEAgent.epsilon)));
        }
      }
      // The bid bundle indicates the campaign id, the limit across all auctions, and the bid entries.
      OneDayBidBundle WEBidBundle = new OneDayBidBundle(this.myCampaign.getId(), this.myCampaign.getBudget(), bidEntries);
      Logging.log("\n:::::::WEBidBundle = " + WEBidBundle);
      return WEBidBundle;

    } catch (AdXException | MarketCreationException | BidderCreationException | MarketAllocationException | AllocationException | GoodsException
        | PrincingAlgoException | IloException | MarketOutcomeException e) {
      Logging.log("Failed to create market model --> ");
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Given a campaign, generates its demand set.
   * 
   * @param campaign
   * @return
   * @throws AdXException
   */
  private Set<GameGoods> generateDemandSet(Campaign campaign) throws AdXException {
    Set<GameGoods> demandSet = new HashSet<GameGoods>();
    for (GameGoods gameGood : WEAgent.listOfGameGoods) {
      if (MarketSegment.marketSegmentSubset(campaign.getMarketSegment(), gameGood.getMarketSegment())) {
        demandSet.add(gameGood);
      }
    }
    return demandSet;
  }

}
