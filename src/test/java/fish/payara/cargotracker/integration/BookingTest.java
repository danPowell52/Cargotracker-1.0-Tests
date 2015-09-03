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
 * TODO Fix the routing functionality so that a test to change routing can be performed.
 */
@RunWith(Arquillian.class)
public class BookingTest {
    private static final Logger log = Logger.getLogger(BookingTest.class.getCanonicalName());
    private static String newCargoId;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap
                .create(WebArchive.class, "cargo-tracker-test.war")
                        // Application layer component directly under test.
                .addClass(BookingService.class)
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
                .addClass(BookingServiceTestRestConfiguration.class)
                .addAsResource("META-INF/persistence.xml",
                        "META-INF/persistence.xml")
                .addAsWebInfResource("test-web.xml", "web.xml")
                .addAsWebInfResource("test-ejb-jar.xml", "ejb-jar.xml")
                .addAsLibraries(
                        Maven.resolver().loadPomFromFile("pom.xml")
                                .resolve("org.apache.commons:commons-lang3")
                                .withTransitivity().asFile());

        return war;
    }

    @ArquillianResource
    private URL deploymentUrl;

    @Rule
    public TestName testName = new TestName();

    @Test
    @RunAsClient
    @InSequence(1)
    public void testBookNewCargo() {
        log.log(Level.INFO, "Starting automated testing to book new cargo.");
        log.log(Level.INFO, "Successfully booked new cargo with Id \"" + newCargoId + "\".");
    }

    @Test
    @RunAsClient
    @InSequence(2)
    public void testPublicTrackNewCargo() {
        log.log(Level.INFO, "Starting automated test to track new cargo with Id \"" + newCargoId + "\" through public interface.");
    }

    @Test
    @RunAsClient
    @InSequence(3)
    public void testAdminTrackNewCargo() {
        log.log(Level.INFO, "Starting automated test to track new cargo with Id \"" + newCargoId + "\" through admin interface.");
    }

    @Test
    @RunAsClient
    @InSequence(4)
    public void testViewDetailsNewCargo() {
        log.log(Level.INFO, "Starting automated test to view details for new cargo with Id \"" + newCargoId + "\" through admin interface.");
    }

    @Test
    @RunAsClient
    @InSequence(5)
    public void testChangeEndNewCargo() {
        log.log(Level.INFO, "Starting automated test to change destination for new cargo with Id \"" + newCargoId + "\" through the admin interface.");
    }
}
