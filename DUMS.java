import java.util.*;

// ==========================================
// DHAKA URBAN MANAGEMENT SYSTEM (DUMS)
// Demonstrates 5 design patterns in one file.
// Save as DUMS.java
// ==========================================

// 1. SINGLETON PATTERN
// Only one shared database object is created.
class DUMSDatabase {
    private static final DUMSDatabase INSTANCE = new DUMSDatabase();

    private DUMSDatabase() { }

    public static DUMSDatabase getInstance() {
        return INSTANCE;
    }

    public void saveComplaint() {
        System.out.println("Complaint saved in database.");
    }
}

// 2. FACTORY PATTERN
// Creates the required city service.
interface CityService {
    void handle();
}

class WasteService implements CityService {
    public void handle() {
        System.out.println("Waste team assigned.");
    }
}

class TrafficService implements CityService {
    public void handle() {
        System.out.println("Traffic team assigned.");
    }
}

class EmergencyService implements CityService {
    public void handle() {
        System.out.println("Emergency team assigned.");
    }
}

class DUMSFactory {
    public static CityService create(String type) {
        if (type.equalsIgnoreCase("waste")) return new WasteService();
        if (type.equalsIgnoreCase("traffic")) return new TrafficService();
        if (type.equalsIgnoreCase("emergency")) return new EmergencyService();
        throw new IllegalArgumentException("Unknown city service");
    }
}

// 3. OBSERVER PATTERN
// Complaint status changes notify registered observers.
interface DUMSObserver {
    void update(String status);
}

class Citizen implements DUMSObserver {
    public void update(String status) {
        System.out.println("Citizen notified: " + status);
    }
}

class GovernmentDashboard implements DUMSObserver {
    public void update(String status) {
        System.out.println("Dashboard updated: " + status);
    }
}

class Complaint {
    private final List<DUMSObserver> observers = new ArrayList<>();

    public void addObserver(DUMSObserver observer) {
        observers.add(observer);
    }

    public void changeStatus(String status) {
        System.out.println("Complaint status: " + status);
        for (DUMSObserver observer : observers) {
            observer.update(status);
        }
    }
}

// 4. STRATEGY PATTERN
// The route-planning method can be changed.
interface RouteStrategy {
    void selectRoute();
}

class ShortestRoute implements RouteStrategy {
    public void selectRoute() {
        System.out.println("Shortest route selected.");
    }
}

class FastestRoute implements RouteStrategy {
    public void selectRoute() {
        System.out.println("Fastest route selected.");
    }
}

class RoutePlanner {
    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(RouteStrategy strategy) {
        this.strategy = strategy;
    }

    public void plan() {
        strategy.selectRoute();
    }
}

// 5. ADAPTER PATTERN
// Converts the external map distance from metres to kilometres.
class ExternalMap {
    public int getDistanceInMetres() {
        return 5000;
    }
}

interface CityMap {
    double getDistanceInKm();
}

class MapAdapter implements CityMap {
    private final ExternalMap map = new ExternalMap();

    public double getDistanceInKm() {
        return map.getDistanceInMetres() / 1000.0;
    }
}

// MAIN CLASS
public class DUMS {
    public static void main(String[] args) {
        System.out.println("=== DUMS DEMO ===");

        // Singleton
        DUMSDatabase db = DUMSDatabase.getInstance();
        db.saveComplaint();

        // Factory
        CityService service = DUMSFactory.create("waste");
        service.handle();

        // Observer
        Complaint complaint = new Complaint();
        complaint.addObserver(new Citizen());
        complaint.addObserver(new GovernmentDashboard());
        complaint.changeStatus("IN_PROGRESS");

        // Strategy
        RoutePlanner planner = new RoutePlanner(new ShortestRoute());
        planner.plan();
        planner.setStrategy(new FastestRoute());
        planner.plan();

        // Adapter
        CityMap map = new MapAdapter();
        System.out.println("Distance: " + map.getDistanceInKm() + " km");

        System.out.println("DUMS demo completed.");
    }
}
