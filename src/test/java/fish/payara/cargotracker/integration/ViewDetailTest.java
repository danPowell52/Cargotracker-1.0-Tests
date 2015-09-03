package fish.payara.cargotracker.integration;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import net.java.cargotracker.application.BookingService;
import net.java.cargotracker.application.BookingServiceTestDataGenerator;
import net.java.cargotracker.application.BookingServiceTestRestConfiguration;
import net.java.cargotracker.application.internal.DefaultBookingService;
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
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author Fraser Savage
 * Automation of testing that details and itineraries can be viewed for all pre-existing cargo.
 */
@RunWith(Arquillian.class)
public class ViewDetailTest {
    private static final Logger log = Logger.getLogger(ViewDetailTest.class.getCanonicalName());
    private static final String trackingId1 = "ABC123";
    private static final String trackingId2 = "JKL567";
    private static final String trackingId3 = "MNO456";
    private static final String trackingId4 = "DEF789";

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
    private URL deploymentUrl;

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
            Assert.fail("An IOException was thrown during the test setup for class \"" + ViewDetailTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    @Test
    @RunAsClient
    public void testViewDetailsId1() {
        log.log(Level.INFO, "Starting automated test to view details for Id \"" + trackingId1 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\"." , adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage detailsPage = adminDashboard.getAnchorByText(trackingId1).click();
            Assert.assertTrue("Handling history did not show expected first voyage number.", detailsPage.asText().contains("0100S"));
            Assert.assertTrue("Handling history did not show expected second voyage number.", detailsPage.asText().contains("0200T"));
            Assert.assertTrue("Handling history did not show expected third voyage number.", detailsPage.asText().contains("0300A"));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + ViewDetailTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    @Test
    @RunAsClient
    public void testViewDetailsId2() {
        log.log(Level.INFO, "Starting automated test to view details for Id \"" + trackingId2 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\"." , adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage detailsPage = adminDashboard.getAnchorByText(trackingId2).click();
            Assert.assertTrue("Handling history did not show expected first voyage number.", detailsPage.asText().contains("0100S"));
            Assert.assertTrue("Handling history did not show expected second voyage number.", detailsPage.asText().contains("0200T"));
            Assert.assertTrue("Handling history did not show expected third voyage number.", detailsPage.asText().contains("0300A"));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + ViewDetailTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    @Test
    @RunAsClient
    public void testViewDetailsId3() {
        log.log(Level.INFO, "Starting automated test to view details for Id \"" + trackingId3 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\"." , adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage detailsPage = adminDashboard.getAnchorByText(trackingId3).click();
            Assert.assertTrue("Handling history did not show expected first voyage number.", detailsPage.asText().contains("0200T"));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + ViewDetailTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    @Test
    @RunAsClient
    public void testViewDetailsId4() {
        log.log(Level.INFO, "Starting automated test to view details for Id \"" + trackingId4 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\"." , adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage detailsPage = adminDashboard.getAnchorByText(trackingId4).click();
            Assert.assertTrue("Expected \"Not routed\" message was not found.", detailsPage.asText().contains("Not routed"));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + ViewDetailTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

}
