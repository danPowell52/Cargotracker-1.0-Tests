package fish.payara.cargotracker.integration;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDateInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import java.io.IOException;
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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import org.junit.Before;

/**
 * @author Fraser Savage
 * This test class is used to automate testing that books a new cargo journey, views the details and itinerary and changes the destination.
 */
@RunWith(Arquillian.class)
public class BookingTest {
    private static final Logger log = Logger.getLogger(BookingTest.class.getCanonicalName());
    private static String newCargoId;

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
            Assert.fail("An IOException was thrown during the test setup for class \"" + BookingTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }
    
    // TODO Create test to book new cargo through admin interface.
    @Test
    @RunAsClient
    @InSequence(1)
    public void testBookNewCargo() {
        log.log(Level.INFO, "Starting automated testing to book new cargo.");
        try {
            HtmlPage admin = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + admin.getTitleText() + "\"." , admin.getTitleText(), is("Cargo Dashboard"));
            HtmlPage makeBooking = admin.getElementById("adminBooking").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Administration\" but actual was \"" + makeBooking.getTitleText() + "\"." , makeBooking.getTitleText(), is("Cargo Administration"));
            HtmlSelect getDestinations = makeBooking.getElementByName("registrationForm:j_idt17");
            HtmlOption selectDestination = getDestinations.getOptionByText("Tokyo (JNTKO)");
            getDestinations.setSelectedAttribute(selectDestination, true);
            HtmlDateInput dateInput = makeBooking.getElementByName("registrationForm:j_idt20");
            dateInput.setValueAttribute("2028-06-06");
            HtmlPage confirmationPage = makeBooking.getElementByName("registrationForm:j_idt22").click();
            Assert.assertTrue("", confirmationPage.asText().contains("Chicago (USCHI)") );
            Assert.assertTrue("", confirmationPage.asText().contains("Tokyo (JNTKO)") );
            List<?> getID = confirmationPage.getByXPath("//span[@class='success label']/text()");
            Object cargoIDPhrase = getID.get(0);
            String [] fragments = cargoIDPhrase.toString().split(" ");
            newCargoId = fragments[3];
        }
        catch(IOException ie) {
            Assert.fail("An IOException was thrown during the test for class \"" + BookingTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ie.getMessage());

        }
            
        log.log(Level.INFO, "Successfully booked new cargo with Id \"" + newCargoId + "\".");
    }

    // TODO Create test to track new cargo through public interface.
    @Test
    @RunAsClient
    @InSequence(2)
    public void testPublicTrackNewCargo() {
        log.log(Level.INFO, "Starting automated test to track new cargo with Id \"" + newCargoId + "\" through public interface.");
        log.log(Level.INFO, "Starting automated test to track Id \"" + newCargoId + "\" through public interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage enterCargoIdPage = landingPageResponse.getElementById("publicLandingLink").click();
            enterCargoIdPage.getElementById("trackingForm:trackingIdInput").setAttribute("value", newCargoId);
            System.out.println(enterCargoIdPage.getElementById("trackingForm:trackingIdInput").getAttribute("value"));
            HtmlPage trackingPage = enterCargoIdPage.getElementById("trackingForm:submitTrack").click();
            Assert.assertTrue("Handling history did not show expected first event.", trackingPage.asText().contains("Received in Hong Kong, at 03/01/2014 12:00 AM PST"));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + BookingTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    
    }

    // TODO Create test to track new cargo through admin interface.
    @Test
    @RunAsClient
    @InSequence(3)
    public void testAdminTrackNewCargo() {
        log.log(Level.INFO, "Starting automated test to track new cargo with Id \"" + newCargoId + "\" through admin interface.");
    }

    // TODO Create test to view details of new cargo through the admin interface.
    @Test
    @RunAsClient
    @InSequence(4)
    public void testViewDetailsNewCargo() {
        log.log(Level.INFO, "Starting automated test to view details for new cargo with Id \"" + newCargoId + "\" through admin interface.");
    }

    // TODO Create test to change the destination of the new cargo through the admin interface.
    @Test
    @RunAsClient
    @InSequence(5)
    public void testChangeEndNewCargo() {
        log.log(Level.INFO, "Starting automated test to change destination for new cargo with Id \"" + newCargoId + "\" through the admin interface.");
    }
}
