package fish.payara.cargotracker.integration;

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
import java.util.logging.Level;
import java.util.logging.Logger;

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

    // TODO Create test to book new cargo through admin interface.
    @Test
    @RunAsClient
    @InSequence(1)
    public void testBookNewCargo() {
        log.log(Level.INFO, "Starting automated testing to book new cargo.");
        log.log(Level.INFO, "Successfully booked new cargo with Id \"" + newCargoId + "\".");
    }

    // TODO Create test to track new cargo through public interface.
    @Test
    @RunAsClient
    @InSequence(2)
    public void testPublicTrackNewCargo() {
        log.log(Level.INFO, "Starting automated test to track new cargo with Id \"" + newCargoId + "\" through public interface.");
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
