package fish.payara.cargotracker.integration;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import javafx.scene.control.TextFormatter;
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

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Fraser Savage
 * Tests that changing destinations for all existing cargo works.
 */
@RunWith(Arquillian.class)
public class ChangeEndTest {
    private static final Logger log = Logger.getLogger(ChangeEndTest.class.getCanonicalName());
    private static final String trackingId1 = "ABC123";
    private static final String trackingId2 = "JKL567";
    private static final String trackingId3 = "MNO456";
    private static final String trackingId4 = "DEF789";

    private static final String changeLink = "Change destination";

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

    /**
     * Set up method that runs before each test to create a new web client and load the application at its landing page.
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
            Assert.fail("An IOException was thrown during the test setup for class \"" + ChangeEndTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    /**
     * Tests that the destination can be changed for cargo with tracking Id ABC123
     */
    @Test
    @RunAsClient
    public void testChangeEndId1() {
        log.log(Level.INFO, "Starting automated test to change destination for Id \"" + trackingId1 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\"." , adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage detailsPage = adminDashboard.getAnchorByText(trackingId1).click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Administration\" but actual was \"" + detailsPage.getTitleText() + "\".", detailsPage.getTitleText(), is("Cargo Administration"));
            DomNodeList<DomElement> tbodyList =  detailsPage.getElementsByTagName("tbody");
            Assert.assertThat(tbodyList.iterator().next().asText(), allOf(containsString("Hong Kong (CNHKG)"), containsString("Helsinki (FIHEL)")));
            HtmlPage changePage = detailsPage.getAnchorByText(changeLink).click();
            HtmlSelect selectDestination = changePage.getElementByName("j_idt14:j_idt16");
            HtmlOption selectOption = selectDestination.getOptionByText("Dallas (USDAL)");
            selectDestination.setSelectedAttribute(selectOption, true);
            detailsPage = changePage.getElementByName("j_idt14:j_idt19").click();
            tbodyList =  detailsPage.getElementsByTagName("tbody");
            Assert.assertThat(tbodyList.iterator().next().asText(), allOf(containsString("Hong Kong (CNHKG)"), containsString("Dallas (USDAL)")));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + ChangeEndTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    /**
     * Tests that the destination can be changed for cargo with tracking Id JKL567
     */
    @Test
    @RunAsClient
    public void testChangeEndId2() {
        log.log(Level.INFO, "Starting automated test to change destination for Id \"" + trackingId2 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\".", adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage detailsPage = adminDashboard.getAnchorByText(trackingId2).click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Administration\" but actual was \"" + detailsPage.getTitleText() + "\"." , detailsPage.getTitleText(), is("Cargo Administration"));
            DomNodeList<DomElement> tbodyList =  detailsPage.getElementsByTagName("tbody");
            Assert.assertThat(tbodyList.iterator().next().asText(), allOf(containsString("Hangzhou (CNHGH)"), containsString("Stockholm (SESTO)")));
            HtmlPage changePage = detailsPage.getAnchorByText(changeLink).click();
            HtmlSelect selectDestination = changePage.getElementByName("j_idt14:j_idt16");
            HtmlOption selectOption = selectDestination.getOptionByText("Guttenburg (SEGOT)");
            selectDestination.setSelectedAttribute(selectOption, true);
            detailsPage = changePage.getElementByName("j_idt14:j_idt19").click();
            tbodyList =  detailsPage.getElementsByTagName("tbody");
            Assert.assertThat(tbodyList.iterator().next().asText(), allOf(containsString("Hangzhou (CNHGH)"), containsString("Guttenburg (SEGOT)")));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + ChangeEndTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    /**
     * Tests that the destination can be changed for cargo with tracking Id MNO456
     */
    @Test
    @RunAsClient
    public void testChangeEndId3() {
        log.log(Level.INFO, "Starting automated test to change destination for Id \"" + trackingId3 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\"." , adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage detailsPage = adminDashboard.getAnchorByText(trackingId3).click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Administration\" but actual was \"" + detailsPage.getTitleText() + "\".", detailsPage.getTitleText(), is("Cargo Administration"));
            DomNodeList<DomElement> tbodyList =  detailsPage.getElementsByTagName("tbody");
            Assert.assertThat(tbodyList.iterator().next().asText(), allOf(containsString("New York (USNYC)"), containsString("Dallas (USDAL)")));
            HtmlPage changePage = detailsPage.getAnchorByText(changeLink).click();
            HtmlSelect selectDestination = changePage.getElementByName("j_idt14:j_idt16");
            HtmlOption selectOption = selectDestination.getOptionByText("Chicago (USCHI)");
            selectDestination.setSelectedAttribute(selectOption, true);
            detailsPage = changePage.getElementByName("j_idt14:j_idt19").click();
            tbodyList =  detailsPage.getElementsByTagName("tbody");
            Assert.assertThat(tbodyList.iterator().next().asText(), allOf(containsString("New York (USNYC)"), containsString("Chicago (USCHI)")));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + ChangeEndTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }

    /**
     * Tests that the destination can be changed for cargo with tracking Id DEF789
     */
    @Test
    @RunAsClient
    public void testChangeEndId4() {
        log.log(Level.INFO, "Starting automated test to change destination for Id \"" + trackingId4 + "\" through admin interface.");
        try {
            System.out.println(landingPageResponse.getUrl());
            HtmlPage adminDashboard = landingPageResponse.getElementById("adminLandingLink").click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Dashboard\" but actual was \"" + adminDashboard.getTitleText() + "\".", adminDashboard.getTitleText(), is("Cargo Dashboard"));
            HtmlPage detailsPage = adminDashboard.getAnchorByText(trackingId4).click();
            Assert.assertThat("Page title was not as expected for the admin dashboard. Expected \"Cargo Administration\" but actual was \"" + detailsPage.getTitleText() + "\"." , detailsPage.getTitleText(), is("Cargo Administration"));
            DomNodeList<DomElement> tbodyList =  detailsPage.getElementsByTagName("tbody");
            Assert.assertThat(tbodyList.iterator().next().asText(), allOf(containsString("Hong Kong (CNHKG)"), containsString("Melbourne (AUMEL)")));
            HtmlPage changePage = detailsPage.getAnchorByText(changeLink).click();
            changePage.getElementByName("j_idt14:j_idt16").setAttribute("value", "Rotterdam (NLRTM)");
            HtmlSelect selectDestination = changePage.getElementByName("j_idt14:j_idt16");
            HtmlOption selectOption = selectDestination.getOptionByText("Rotterdam (NLRTM)");
            selectDestination.setSelectedAttribute(selectOption, true);
            detailsPage = changePage.getElementByName("j_idt14:j_idt19").click();
            tbodyList =  detailsPage.getElementsByTagName("tbody");
            Assert.assertThat(tbodyList.iterator().next().asText(), allOf(containsString("Hong Kong (CNHKG)"), containsString("Rotterdam (NLRTM)")));
        } catch (IOException ex) {
            Assert.fail("An IOException was thrown during the test for class \"" + ChangeEndTest.class.getSimpleName() + "\" at method \"" + testName.getMethodName() + "\" with message: " + ex.getMessage());
        }
    }
}
