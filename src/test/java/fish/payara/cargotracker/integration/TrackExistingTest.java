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

    @Deployment
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(MavenImporter.class)
                .loadPomFromFile("pom.xml").importBuildOutput().as(WebArchive.class);
                /*
                ShrinkWrap
                .create(WebArchive.class)
                        /*
                        // Application layer components.
                .addClasses(BookingService.class,
                        ApplicationEvents.class,
                        CargoInspectionService.class,
                        HandlingEventService.class,
                        DefaultBookingService.class,
                        DefaultCargoInspectionService.class,
                        DefaultHandlingEventService.class)
                        // Domain layer components.
                .addClass(TrackingId.class)
                .addClass(UnLocode.class)
                .addClass(Itinerary.class)
                .addClass(Leg.class)
                .addClass(Voyage.class)
                .addClass(VoyageNumber.class)
                .addClass(Schedule.class)
                .addClass(CarrierMovement.class)
                .addClass(Location.class)
                .addClass(HandlingEvent.class)
                .addClass(Cargo.class)
                .addClass(RouteSpecification.class)
                .addClass(AbstractSpecification.class)
                .addClass(Specification.class)
                .addClass(AndSpecification.class)
                .addClass(OrSpecification.class)
                .addClass(NotSpecification.class)
                .addClass(Delivery.class)
                .addClass(TransportStatus.class)
                .addClass(HandlingActivity.class)
                .addClass(RoutingStatus.class)
                .addClass(HandlingHistory.class)
                .addClass(DomainObjectUtils.class)
                .addClass(CargoRepository.class)
                .addClass(LocationRepository.class)
                .addClass(VoyageRepository.class)
                .addClass(HandlingEventRepository.class)
                .addClass(HandlingEventFactory.class)
                .addClass(CannotCreateHandlingEventException.class)
                .addClass(UnknownCargoException.class)
                .addClass(UnknownVoyageException.class)
                .addClass(UnknownLocationException.class)
                .addClass(RoutingService.class)
                        // Application layer components
                .addClass(DefaultBookingService.class)
                        // Infrastructure layer components.
                .addClass(JpaCargoRepository.class)
                .addClass(JpaVoyageRepository.class)
                .addClass(JpaHandlingEventRepository.class)
                .addClass(JpaLocationRepository.class)
                .addClass(ExternalRoutingService.class)
                .addClass(JsonMoxyConfigurationContextResolver.class)
                        // Interface components
                .addClass(TransitPath.class)
                .addClass(TransitEdge.class)
                        // Third-party system simulator
                .addClass(GraphTraversalService.class)
                .addClass(GraphDao.class)
                        // Sample data.
                .addClass(BookingServiceTestDataGenerator.class)
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class)
                .addClass(DateUtil.class)
                .addClass(BookingServiceTestRestConfiguration.class);

                .addPackage("net.java.cargotracker.application")
                .addPackage("net.java.cargotracker.application.internal")
                .addPackage("net.java.cargotracker.application.util")
                .addPackage("net.java.cargotracker.domain.model.cargo")
                .addPackage("net.java.cargotracker.domain.model.handling")
                .addPackage("net.java.cargotracker.domain.model.location")
                .addPackage("net.java.cargotracker.domain.model.voyage")
                .addPackage("net.java.cargotracker.domain.service")
                .addPackage("net.java.cargotracker.domain.shared")
                .addPackage("net.java.cargotracker.infrastructure.events.cdi")
                .addPackage("net.java.cargotracker.infrastructure.messaging.jms")
                .addPackage("net.java.cargotracker.infrastructure.persistence.jpa")
                .addPackage("net.java.cargotracker.infrastructure.routing")
                .addPackage("net.java.cargotracker.interfaces.booking.facade")
                .addPackage("net.java.cargotracker.interfaces.booking.facade.dto")
                .addPackage("net.java.cargotracker.interfaces.booking.facade.internal")
                .addPackage("net.java.cargotracker.interfaces.booking.facade.internal.assembler")
                .addPackage("net.java.cargotracker.interfaces.booking.rest")
                .addPackage("net.java.cargotracker.interfaces.booking.socket")
                .addPackage("net.java.cargotracker.interfaces.booking.web")
                .addPackage("net.java.cargotracker.interfaces.handling")
                .addPackage("net.java.cargotracker.interfaces.handling.file")
                .addPackage("net.java.cargotracker.interfaces.handling.rest")
                .addPackage("net.java.cargotracker.interfaces.tracking.web")
                .addPackage("net.java.pathfinder");
                // Merges in the files needed from the webapp source for integration testing.
                war.merge(ShrinkWrap.create(GenericArchive.class)
                        .as(ExplodedImporter.class)
                        .importDirectory(webapp_src)
                        .as(GenericArchive.class), "/", Filters.exclude("ejb-jar.xml"))
                // Resources
                .addAsResource("META-INF/batch-jobs/EventFilesProcessorJob.xml",
                        "META-INF/batch-jobs/EventFilesProcessorJob.xml")
                .addAsResource("net/java/cargotracker/messages.properties",
                        "net/java/cargotracker/messages.properties")
                .addAsResource("META-INF/persistence.xml",
                        "META-INF/persistence.xml")
                .addAsWebInfResource("test-web.xml", "web.xml")
                //.addAsWebInfResource("test-ejb-jar.xml", "ejb-jar.xml")
                .addAsLibraries(
                        Maven.resolver().loadPomFromFile("pom.xml")
                                .resolve("org.apache.commons:commons-lang3")
                                .withTransitivity().asFile());
                */

        System.out.println(war.toString(true));
        return war;
    }

    @ArquillianResource
    private URI deploymentUrl;

    @Rule
    public TestName testName = new TestName();

    private WebClient browser;

    private HtmlPage landingPageResponse;

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
