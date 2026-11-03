import java.util.*;

// ==========================================
// AGRICULTURAL AUTOMATION SYSTEM (AAS)
// Demonstrates 5 design patterns in one file.
// ==========================================

// 1. SINGLETON PATTERN
// Only one controller object is created.
class AASController {
    private static final AASController INSTANCE = new AASController();

    private AASController() { }

    public static AASController getInstance() {
        return INSTANCE;
    }

    public void control() {
        System.out.println("Controller is working.");
    }
}

// 2. FACTORY PATTERN
// Creates the requested farm device.
interface FarmDevice {
    void operate();
}

class WaterPump implements FarmDevice {
    public void operate() {
        System.out.println("Water pump is ON.");
    }
}

class FarmLight implements FarmDevice {
    public void operate() {
        System.out.println("Farm light is ON.");
    }
}

class AASFactory {
    public static FarmDevice create(String type) {
        if (type.equalsIgnoreCase("pump")) return new WaterPump();
        if (type.equalsIgnoreCase("light")) return new FarmLight();
        throw new IllegalArgumentException("Unknown device");
    }
}

// 3. OBSERVER PATTERN
// Sensor sends a message to all registered observers.
interface AASObserver {
    void update(String message);
}

class Farmer implements AASObserver {
    public void update(String message) {
        System.out.println("Farmer notified: " + message);
    }
}

class SoilSensor {
    private final List<AASObserver> observers = new ArrayList<>();

    public void addObserver(AASObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(String message) {
        for (AASObserver observer : observers) {
            observer.update(message);
        }
    }
}

// 4. STRATEGY PATTERN
// We can change the watering strategy when needed.
interface WateringStrategy {
    void water();
}

class NormalWatering implements WateringStrategy {
    public void water() {
        System.out.println("Normal watering selected.");
    }
}

class SaveWatering implements WateringStrategy {
    public void water() {
        System.out.println("Water-saving mode selected.");
    }
}

class WateringSystem {
    private WateringStrategy strategy;

    public WateringSystem(WateringStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(WateringStrategy strategy) {
        this.strategy = strategy;
    }

    public void run() {
        strategy.water();
    }
}

// 5. ADAPTER PATTERN
// Adapter makes the old sensor work with the new interface.
class OldMoistureSensor {
    public int readValue() {
        return 70; // Example moisture value
    }
}

interface MoistureSensor {
    int readMoisture();
}

class MoistureAdapter implements MoistureSensor {
    private final OldMoistureSensor oldSensor = new OldMoistureSensor();

    public int readMoisture() {
        return oldSensor.readValue();
    }
}

// MAIN CLASS
public class AAS {
    public static void main(String[] args) {
        System.out.println("=== AAS DEMO ===");

        // Singleton
        AASController controller = AASController.getInstance();
        controller.control();

        // Factory
        FarmDevice pump = AASFactory.create("pump");
        pump.operate();

        // Observer
        SoilSensor sensor = new SoilSensor();
        sensor.addObserver(new Farmer());
        sensor.notifyObservers("Soil is dry!");

        // Strategy
        WateringSystem watering =
                new WateringSystem(new NormalWatering());
        watering.run();
        watering.setStrategy(new SaveWatering());
        watering.run();

        // Adapter
        MoistureSensor moisture = new MoistureAdapter();
        System.out.println("Soil moisture: " + moisture.readMoisture() + "%");

        System.out.println("AAS demo completed.");
    }
}
