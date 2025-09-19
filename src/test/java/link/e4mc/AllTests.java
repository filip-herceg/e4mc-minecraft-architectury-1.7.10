package link.e4mc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ConfigTest.class,
    E4mcCommandTest.class,
    TextHelperTest.class,
    RelaySessionTest.class,
    IntegrationTest.class
})
public class AllTests {
    // Test suite to run all tests together
}
