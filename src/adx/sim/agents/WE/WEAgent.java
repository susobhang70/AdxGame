package adx.sim.agents.WE;

import java.util.HashSet;
import java.util.Set;

import adx.exceptions.AdXException;
import adx.sim.agents.SimAgent;
import adx.sim.agents.SimAgentModel;
import adx.sim.agents.SimAgentModel.MarketModel;
import adx.structures.BidBundle;
import adx.structures.SimpleBidEntry;
import adx.util.Logging;
import adx.variants.onedaygame.OneDayBidBundle;
import algorithms.pricing.RestrictedEnvyFreePricesLPSolution;
import algorithms.pricing.RestrictedEnvyFreePricesLPWithReserve;
import algorithms.pricing.error.PrincingAlgoException;
import allocations.greedy.GreedyAllocation;
import ilog.concert.IloException;
import structures.Bidder;
import structures.Market;
import structures.MarketAllocation;
import structures.exceptions.AllocationException;
import structures.exceptions.BidderCreationException;
import structures.exceptions.GoodsException;
import structures.exceptions.MarketAllocationException;
import structures.exceptions.MarketCreationException;
import structures.exceptions.MarketOutcomeException;

/**
 * Implements the Walrasian Equilibrium (WE) agent.
 * 
 * @author Enrique Areyan Viqueira
 */
public class WEAgent extends SimAgent {

  /**
   * Additive factor for the bids.
   */
  private static final double epsilon = 0.001;
  
  /**
   * The reserve price.
   */
  private final double reserve;

  /**
   * Constructor.
   * 
   * @param simAgentName
   */
  public WEAgent(String simAgentName, double reserve) {
    super(simAgentName);
    this.reserve = reserve;
  }

  @Override
  public BidBundle getBidBundle() {

    try {
      // Get the model.
      MarketModel marketModel = SimAgentModel.constructModel(this.myCampaign, this.othersCampaigns);
      Market<GameGoods, Bidder<GameGoods>> market = marketModel.market;
      // Print some useful info
      //this.printInfo("WE AGENT", market);
      Bidder<GameGoods> myCampaignBidder = marketModel.mybidder;
      Set<GameGoods> myDemandSet = marketModel.mybidder.getDemandSet();

      // Run allocation algorithm.
      MarketAllocation<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>> greedyAllocation = new GreedyAllocation<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>>().Solve(market);
      //greedyAllocation.printAllocation();

      // Run pricing algorithm
      RestrictedEnvyFreePricesLPWithReserve<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>> restrictedEnvyFreePricesLP = new RestrictedEnvyFreePricesLPWithReserve<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>>(greedyAllocation, this.reserve);
      // restrictedEnvyFreePricesLP.setMarketClearanceConditions(true);
      restrictedEnvyFreePricesLP.createLP();
      RestrictedEnvyFreePricesLPSolution<Market<GameGoods, Bidder<GameGoods>>, GameGoods, Bidder<GameGoods>> prices = restrictedEnvyFreePricesLP.Solve();
      //prices.printPrices();

      // Back-up bid from WE allocation and prices
      Set<SimpleBidEntry> bidEntries = new HashSet<SimpleBidEntry>();
      for (GameGoods demandedGood : myDemandSet) {
        //Logging.log("Price for: " + demandedGood + " is " + prices.getPrice(demandedGood));
        if (greedyAllocation.getAllocation(demandedGood, myCampaignBidder) > 0) {
          bidEntries.add(new SimpleBidEntry(demandedGood.getMarketSegment(), prices.getPrice(demandedGood) + WEAgent.epsilon, greedyAllocation.getAllocation(demandedGood, myCampaignBidder) * (prices.getPrice(demandedGood) + WEAgent.epsilon)));
        }
      }
      // The bid bundle indicates the campaign id, the limit across all auctions, and the bid entries.
      OneDayBidBundle WEBidBundle = new OneDayBidBundle(this.myCampaign.getId(), this.myCampaign.getBudget(), bidEntries);
      //Logging.log("\n:::::::WEBidBundle = " + WEBidBundle);
      return WEBidBundle;

    } catch (AdXException | MarketCreationException | BidderCreationException | MarketAllocationException | AllocationException | GoodsException | PrincingAlgoException | IloException | MarketOutcomeException e) {
      Logging.log("Failed to create market model --> ");
      e.printStackTrace();
    }
    return null;
  }

}
