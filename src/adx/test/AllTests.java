package adx.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MarketSegmentTest.class, QueryTest.class, AdAuctionsTest.class, ServerStateTest.class })
public class AllTests {

}
