package fish.payara.cargotracker.integration;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import net.java.cargotracker.application.*;
import net.java.cargotracker.application.internal.DefaultBookingService;
import net.java.cargotracker.application.internal.DefaultCargoInspectionService;
import net.java.cargotracker.application.internal.DefaultHandlingEventService;
import net.java.cargotracker.application.util.DateUtil;
import net.java.cargotracker.application.util.JsonMoxyConfigurationContextResolver;
import net.java.cargotracker.domain.model.cargo.*;
import net.java.cargotracker.domain.model.handling.*;
import net.java.cargotracker.domain.model.location.Location;
import net.java.cargotracker.domain.model.location.LocationRepository;
import net.java.cargotracker.domain.model.location.SampleLocations;
import net.java.cargotracker.domain.model.location.UnLocode;
import net.java.cargotracker.domain.model.voyage.*;
import net.java.cargotracker.domain.service.RoutingService;
import net.java.cargotracker.domain.shared.*;
import net.java.cargotracker.infrastructure.persistence.jpa.JpaCargoRepository;
import net.java.cargotracker.infrastructure.persistence.jpa.JpaHandlingEventRepository;
import net.java.cargotracker.infrastructure.persistence.jpa.JpaLocationRepository;
import net.java.cargotracker.infrastructure.persistence.jpa.JpaVoyageRepository;
import net.java.cargotracker.infrastructure.routing.ExternalRoutingService;
import net.java.pathfinder.api.GraphTraversalService;
import net.java.pathfinder.api.TransitEdge;
import net.java.pathfinder.api.TransitPath;
import net.java.pathfinder.internal.GraphDao;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author Fraser Savage
 * Tests that the pre-existing cargo can have their tracking info viewed through both the public interface and the admin interface.
 */
@RunWith(Arquillian.class)
public class TrackExistingTest {
    private static final Logger log = Logger.getLogger(TrackExistingTest.class.getCanonicalName());
    private static final String webapp_src = "src/main/webapp";
    private static final String trackingId1 = "ABC123";
    private static final String trackingId2 = "JKL567";

    /**
     * Deploys the war to the application server.
     * @return
     */
    @Deployment
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(MavenImporter.class)
                .loadPomFromFile("pom.xml").importBuildOutput().as(WebArchive.class);
        System.out.println(war.toString(true));
        return war;
    }

    @ArquillianResource
    private URI deploymentUrl;

    @Rule
    public TestName testName = new TestName();

    private WebClient browser;

    private HtmlPage landingPageResponse;

    /**
     * Set up method run before each test to load up a web client and load the application landing page.
     */
    @Before
    @RunAsClient
    public void setUp() {
        try {
            browser = new WebClient();
            browser.getOptions().setThrowExceptionOnScriptError(false);
            landingPageResponse = browser.getPage(deploymentUrl.toString() + "index.xhtml");
            Assert.assertEquals("Could not load the application landing page.", "Cargo Tracker", landingPageResponse.getTitleText());
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test setup for class \"" + TrackExistingTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    /**
     * Tests that tracking of the Id ABC123 works through the public interface.
     */
    @Test
    @RunAsClient
    @InSequence(1)
    public void testPublicTrackingId1() {
        log.log(Level.INFO, "Starting automated test to track Id \"" + trackingId1 + "\" through public interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage enterCargoIdPage = landingPageResponse.getElementById("publicLandingLink").click();
            enterCargoIdPage.getElementById("trackingForm:trackingIdInput").setAttribute("value", trackingId1);
            System.out.println(enterCargoIdPage.getElementById("trackingForm:trackingIdInput").getAttribute("value"));
            HtmlPage trackingPage = enterCargoIdPage.getElementById("trackingForm:submitTrack").click();
            Assert.assertTrue("Handling history did not show expected first event.", trackingPage.asText().contains("Received in Hong Kong, at 03/01/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected second event.", trackingPage.asText().contains("Loaded onto voyage 0100S in Hong Kong, at 03/02/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected third event.", trackingPage.asText().contains("Unloaded off voyage 0100S in New York, at 03/05/2014 12:00 AM GMT"));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + TrackExistingTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    /**
     * Tests that the tracking of the Id JKL567 works through the public interface.
     */
    @Test
    @RunAsClient
    @InSequence(2)
    public void testPublicTrackingId2() {
        log.log(Level.INFO, "Starting automated test to track Id \"" + trackingId2 + "\" through public interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage enterCargoIdPage = landingPageResponse.getElementById("publicLandingLink").click();

            enterCargoIdPage.getElementById("trackingForm:trackingIdInput").setAttribute("value", trackingId2);
            System.out.println(enterCargoIdPage.getElementById("trackingForm:trackingIdInput").getAttribute("value"));
            HtmlPage trackingPage = enterCargoIdPage.getElementById("trackingForm:submitTrack").click();
            Assert.assertTrue("Tracker did not contain expected misdirection notice.", trackingPage.asText().contains("Cargo is misdirected"));
            Assert.assertTrue("Handling history did not show expected first event.", trackingPage.asText().contains("Received in Hangzhou, at 03/01/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected second event.", trackingPage.asText().contains("Loaded onto voyage 0100S in Hangzhou, at 03/03/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected third event.", trackingPage.asText().contains("Unloaded off voyage 0100S in New York, at 03/05/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected fourth event.", trackingPage.asText().contains("Loaded onto voyage 0100S in New York, at 03/06/2014 12:00 AM GMT"));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + TrackExistingTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    /**
     * Tests that tracking Id ABC123 can be tracked through the Admin interface.
     */
    @Test
    @RunAsClient
    @InSequence(3)
    public void testAdminTrackingId1(){
        log.log(Level.INFO, "Starting automated test to track Id \"" + trackingId1 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\"." , adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage enterCargoIdPage = adminDashboard.getElementById("adminTracking").click();
            enterCargoIdPage.getElementById("trackingForm:trackingIdInput").setAttribute("value", trackingId1);
            System.out.println(enterCargoIdPage.getElementById("trackingForm:trackingIdInput").getAttribute("value"));
            HtmlPage trackingPage = enterCargoIdPage.getElementById("trackingForm:submitTrack").click();
            Assert.assertTrue("Tracker did not contain expected next activity.", trackingPage.asText().contains("Next expected activity is to load cargo onto voyage 0200T in New York"));
            Assert.assertTrue("Handling history did not show expected first event.", trackingPage.asText().contains("Received in Hong Kong, at 03/01/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected second event.", trackingPage.asText().contains("Loaded onto voyage 0100S in Hong Kong, at 03/02/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected third event.", trackingPage.asText().contains("Unloaded off voyage 0100S in New York, at 03/05/2014 12:00 AM GMT"));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + TrackExistingTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());        }
    }

    /**
     * Tests that the tracking Id JKL567 can be tracked through the admin interface.
     */
    @Test
    @RunAsClient
    @InSequence(4)
    public void testAdminTrackingId2() {
        log.log(Level.INFO, "Starting automated test to track Id \"" + trackingId2 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\"." , adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage enterCargoIdPage = adminDashboard.getElementById("adminTracking").click();
            enterCargoIdPage.getElementById("trackingForm:trackingIdInput").setAttribute("value", trackingId2);
            System.out.println(enterCargoIdPage.getElementById("trackingForm:trackingIdInput").getAttribute("value"));
            HtmlPage trackingPage = enterCargoIdPage.getElementById("trackingForm:submitTrack").click();
            Assert.assertTrue("Tracker did not contain expected misdirection notice.", trackingPage.asText().contains("Cargo is misdirected"));
            Assert.assertTrue("Handling history did not show expected first event.", trackingPage.asText().contains("Received in Hangzhou, at 03/01/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected second event.", trackingPage.asText().contains("Loaded onto voyage 0100S in Hangzhou, at 03/03/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected third event.", trackingPage.asText().contains("Unloaded off voyage 0100S in New York, at 03/05/2014 12:00 AM GMT"));
            Assert.assertTrue("Handling history did not show expected fourth event.", trackingPage.asText().contains("Loaded onto voyage 0100S in New York, at 03/06/2014 12:00 AM GMT"));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + TrackExistingTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());        }
    }
}
