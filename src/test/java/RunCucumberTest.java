import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.runner.RunWith;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@RunWith(Cucumber.class)
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "fr.etu.steats")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@CucumberOptions(features = "src/test/ressources/features", glue = "fr.etu.steats")
public class RunCucumberTest {
}