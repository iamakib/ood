import java.util.*;

// ==================== OBSERVER PATTERN ====================

// Observer interface
interface ComplaintObserver {
    void update(Complaint complaint);
}

// Notifies only the citizen who owns the complaint
class CitizenNotifier implements ComplaintObserver {
    private final String citizenId;
    public int received = 0;

    public CitizenNotifier(String citizenId) {
        this.citizenId = citizenId;
    }

    public void update(Complaint c) {
        if (!citizenId.equals(c.citizenId)) return;

        received++;
        System.out.println("Citizen " + citizenId + ": "
                + c.id + " -> " + c.getStatus());
    }
}

// Government dashboard receives all status updates
class GovernmentDashboard implements ComplaintObserver {
    public int received = 0;

    public void update(Complaint c) {
        received++;
        System.out.println("Dashboard: " + c.id + " -> " + c.getStatus());
    }
}

// Publisher manages observers and sends notifications
class StatusPublisher {
    private final List<ComplaintObserver> observers = new ArrayList<>();

    public void subscribe(ComplaintObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unsubscribe(ComplaintObserver observer) {
        observers.remove(observer);
    }

    public void publish(Complaint complaint) {
        for (ComplaintObserver o : new ArrayList<>(observers)) {
            o.update(complaint);
        }
    }
}

// ==================== COMPLAINT CLASSES ====================

// Possible complaint statuses
enum Status {
    OPEN, IN_PROGRESS, RESOLVED
}

// Stores complaint information
class Complaint {
    public final String id;
    public final String citizenId;
    public final String serviceType;
    public final String location;

    private Status status = Status.OPEN;

    public Complaint(String id, String citizenId,
                     String serviceType, String location) {
        this.id = id;
        this.citizenId = citizenId;
        this.serviceType = serviceType;
        this.location = location;
    }

    public Status getStatus() {
        return status;
    }

    // Changes complaint status
    void setStatus(Status status) {
        this.status = status;
    }
}

// ==================== SINGLETON PATTERN ====================

// Only one shared database object can exist
class UrbanDatabase {
    private static final UrbanDatabase INSTANCE = new UrbanDatabase();

    private final Map<String, Complaint> complaints =
            new LinkedHashMap<>();

    private UrbanDatabase() {
        // Private constructor prevents external object creation
    }

    public static UrbanDatabase getInstance() {
        return INSTANCE;
    }

    // Stores a complaint and rejects duplicate IDs
    public void add(Complaint c) {
        if (complaints.containsKey(c.id)) {
            throw new IllegalArgumentException(
                    "Duplicate complaint ID: " + c.id);
        }

        complaints.put(c.id, c);
    }

    // Finds a complaint by ID
    public Complaint find(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Complaint ID is required");
        }

        Complaint c = complaints.get(id);

        if (c == null) {
            throw new IllegalArgumentException(
                    "Complaint not found: " + id);
        }

        return c;
    }
}

// ==================== FACTORY PATTERN ====================

// Common interface for urban services
interface UrbanService {
    String assign(Complaint complaint);
}

// Waste management service
class WasteService implements UrbanService {
    public String assign(Complaint c) {
        return "Waste team assigned to " + c.location;
    }
}

// Traffic management service
class TrafficService implements UrbanService {
    public String assign(Complaint c) {
        return "Traffic team assigned to " + c.location;
    }
}

// Emergency management service
class EmergencyService implements UrbanService {
    public String assign(Complaint c) {
        return "Emergency team assigned to " + c.location;
    }
}

// Factory creates the appropriate service
class UrbanServiceFactory {
    public static UrbanService create(String type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Service type is required");
        }

        switch (type.toUpperCase()) {
            case "WASTE":
                return new WasteService();

            case "TRAFFIC":
                return new TrafficService();

            case "EMERGENCY":
                return new EmergencyService();

            default:
                throw new IllegalArgumentException(
                        "Unknown service type: " + type);
        }
    }
}

// ==================== ADAPTER PATTERN ====================

// Route information in standard units
class Route {
    public final String id;
    public final double km;
    public final double minutes;

    public Route(String id, double km, double minutes) {
        if (!Double.isFinite(km) || !Double.isFinite(minutes)
                || km < 0 || minutes < 0) {
            throw new IllegalArgumentException(
                    "Invalid route measurements");
        }

        this.id = id;
        this.km = km;
        this.minutes = minutes;
    }
}

// External API returns distance in metres and time in seconds
class ExternalTrafficAPI {
    public String[][] fetchCandidates(String destination) {
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Destination is required");
        }

        return new String[][] {
                {"A", "5000", "1800"},  // 5 km, 30 minutes
                {"B", "8000", "1200"},  // 8 km, 20 minutes
                {"C", "6500", "1500"}   // 6.5 km, 25 minutes
        };
    }
}

// Interface expected by our internal system
interface TrafficServiceAPI {
    List<Route> getRoutes(String destination);
}

// Adapter converts external units into standard units
class TrafficAPIAdapter implements TrafficServiceAPI {
    private final ExternalTrafficAPI api;

    public TrafficAPIAdapter(ExternalTrafficAPI api) {
        this.api = api;
    }

    public List<Route> getRoutes(String destination) {
        List<Route> routes = new ArrayList<>();

        for (String[] row : api.fetchCandidates(destination)) {
            double km = Double.parseDouble(row[1]) / 1000.0;
            double minutes = Double.parseDouble(row[2]) / 60.0;

            routes.add(new Route(row[0], km, minutes));
        }

        return routes;
    }
}

// ==================== STRATEGY PATTERN ====================

// Interface for route-selection strategies
interface RouteStrategy {
    Route choose(List<Route> routes);
}

// Selects the shortest route
class ShortestRouteStrategy implements RouteStrategy {
    public Route choose(List<Route> routes) {
        if (routes == null || routes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No routes available");
        }

        return routes.stream()
                .min(Comparator
                        .comparingDouble((Route r) -> r.km)
                        .thenComparing(r -> r.id))
                .get();
    }
}

// Selects the fastest route
class FastestRouteStrategy implements RouteStrategy {
    public Route choose(List<Route> routes) {
        if (routes == null || routes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No routes available");
        }

        return routes.stream()
                .min(Comparator
                        .comparingDouble((Route r) -> r.minutes)
                        .thenComparing(r -> r.id))
                .get();
    }
}

// Context class uses the selected strategy
class RoutePlanner {
    private RouteStrategy strategy;
    private final TrafficServiceAPI traffic;

    public RoutePlanner(RouteStrategy strategy,
                        TrafficServiceAPI traffic) {
        this.strategy = strategy;
        this.traffic = traffic;
    }

    // Allows changing the strategy at runtime
    public void setStrategy(RouteStrategy strategy) {
        this.strategy = strategy;
    }

    public Route plan(String destination) {
        List<Route> routes = traffic.getRoutes(destination);
        return strategy.choose(routes);
    }
}

// ==================== COMPLAINT MANAGER ====================

// Controls submission, dispatch and resolution
class ComplaintManager {
    private final UrbanDatabase db;
    private final StatusPublisher publisher;
    private final RoutePlanner planner;

    public ComplaintManager(UrbanDatabase db,
                            StatusPublisher publisher,
                            RoutePlanner planner) {
        this.db = db;
        this.publisher = publisher;
        this.planner = planner;
    }

    // Submits a new complaint
    public Complaint submit(String id, String citizen,
                            String type, String location) {
        if (id == null || id.trim().isEmpty()
                || citizen == null || citizen.trim().isEmpty()
                || location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ID, citizen ID and location are required");
        }

        // Check whether the service type is supported
        UrbanServiceFactory.create(type);

        Complaint c = new Complaint(
                id, citizen, type.toUpperCase(), location);

        db.add(c);
        publisher.publish(c);

        return c;
    }

    // Assigns a service team and selects a route
    public Route dispatch(String id) {
        Complaint c = db.find(id);

        if (c.getStatus() != Status.OPEN) {
            throw new IllegalStateException(
                    "Only OPEN complaints can be dispatched");
        }

        UrbanService service =
                UrbanServiceFactory.create(c.serviceType);

        // If no route exists, the complaint stays OPEN
        Route route = planner.plan(c.location);

        String assignment = service.assign(c);

        c.setStatus(Status.IN_PROGRESS);
        publisher.publish(c);

        System.out.println(assignment
                + "; selected route " + route.id);

        return route;
    }

    // Resolves a complaint after it is in progress
    public void resolve(String id) {
        Complaint c = db.find(id);

        if (c.getStatus() != Status.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Only IN_PROGRESS complaints can be resolved");
        }

        c.setStatus(Status.RESOLVED);
        publisher.publish(c);
    }
}

// ==================== MAIN CLASS ====================

public class DUMSExam {

    // Prints PASS when a test succeeds
    static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAILED: " + message);
        }

        System.out.println("PASS: " + message);
    }

    // Tests whether an invalid operation is rejected
    static void rejects(Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalArgumentException
                 | IllegalStateException e) {
            System.out.println("PASS: " + message);
            return;
        }

        throw new AssertionError("FAILED: " + message);
    }

    public static void main(String[] args) {

        // 1. SINGLETON PATTERN
        UrbanDatabase db = UrbanDatabase.getInstance();

        check(db == UrbanDatabase.getInstance(),
                "Singleton returns same instance");

        // 2. FACTORY PATTERN
        check(UrbanServiceFactory.create("WASTE")
                        instanceof WasteService,
                "Factory creates WASTE");

        check(UrbanServiceFactory.create("TRAFFIC")
                        instanceof TrafficService,
                "Factory creates TRAFFIC");

        check(UrbanServiceFactory.create("EMERGENCY")
                        instanceof EmergencyService,
                "Factory creates EMERGENCY");

        rejects(() -> UrbanServiceFactory.create("UNKNOWN"),
                "Unknown service rejected");

        // 3. ADAPTER PATTERN
        TrafficServiceAPI traffic =
                new TrafficAPIAdapter(new ExternalTrafficAPI());

        Route routeA = traffic.getRoutes("Mirpur").get(0);

        check(routeA.km == 5.0 && routeA.minutes == 30.0,
                "Adapter converts metres and seconds");

        // 4. STRATEGY PATTERN
        RoutePlanner planner = new RoutePlanner(
                new ShortestRouteStrategy(), traffic);

        check(planner.plan("Mirpur").id.equals("A"),
                "Shortest strategy selects A");

        planner.setStrategy(new FastestRouteStrategy());

        check(planner.plan("Mirpur").id.equals("B"),
                "Fastest strategy selects B");

        rejects(() ->
                        new ShortestRouteStrategy().choose(
                                Collections.emptyList()),
                "Empty route list rejected");

        // 5. OBSERVER PATTERN
        StatusPublisher publisher = new StatusPublisher();

        CitizenNotifier citizen1 = new CitizenNotifier("U1");
        CitizenNotifier citizen2 = new CitizenNotifier("U2");
        GovernmentDashboard dashboard =
                new GovernmentDashboard();

        publisher.subscribe(citizen1);
        publisher.subscribe(citizen2);
        publisher.subscribe(dashboard);

        ComplaintManager manager =
                new ComplaintManager(db, publisher, planner);

        // Submit a complaint
        Complaint c1 = manager.submit(
                "C1", "U1", "WASTE", "Mirpur");

        check(c1.getStatus() == Status.OPEN,
                "New complaint starts OPEN");

        check(citizen1.received == 1
                        && citizen2.received == 0
                        && dashboard.received == 1,
                "Citizen receives only their own notification");

        // Test invalid submissions
        rejects(() ->
                        manager.submit(
                                "C1", "U1", "WASTE", "Mirpur"),
                "Duplicate ID rejected");

        rejects(() ->
                        manager.submit(
                                "", "U1", "WASTE", "Mirpur"),
                "Blank ID rejected");

        rejects(() -> manager.resolve("C1"),
                "Cannot resolve OPEN complaint");

        // Dispatch using the FASTEST strategy
        Route chosen = manager.dispatch("C1");

        check(chosen.id.equals("B")
                        && c1.getStatus() == Status.IN_PROGRESS,
                "Dispatch selects route B and sets IN_PROGRESS");

        check(citizen1.received == 2
                        && dashboard.received == 2,
                "Dispatch status is published");

        // Unsubscribe dashboard
        publisher.unsubscribe(dashboard);

        // Resolve the complaint
        manager.resolve("C1");

        check(c1.getStatus() == Status.RESOLVED
                        && citizen1.received == 3
                        && dashboard.received == 2,
                "Resolve works after dashboard unsubscribe");

        // Test invalid dispatch operations
        rejects(() -> manager.dispatch("C1"),
                "Cannot dispatch RESOLVED complaint");

        rejects(() -> manager.dispatch("missing"),
                "Missing complaint ID rejected");

        // Test what happens when no routes are available
        TrafficServiceAPI noTraffic =
                destination -> Collections.emptyList();

        RoutePlanner noRoutePlanner = new RoutePlanner(
                new FastestRouteStrategy(), noTraffic);

        ComplaintManager noRouteManager =
                new ComplaintManager(db, publisher, noRoutePlanner);

        Complaint c2 = noRouteManager.submit(
                "C2", "U1", "TRAFFIC", "Dhanmondi");

        int notificationsBefore = citizen1.received;

        rejects(() -> noRouteManager.dispatch("C2"),
                "Dispatch rejected when no route exists");

        check(c2.getStatus() == Status.OPEN
                        && citizen1.received == notificationsBefore,
                "No-route failure keeps complaint OPEN");

        System.out.println("All tests completed.");
    }
}