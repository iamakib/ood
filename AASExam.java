
import java.util.*;

// Main class
public class AASExam {

    // ==================== 1. SINGLETON ====================

    static class Field {
        String id, cropType;

        Field(String id, String cropType) {
            this.id = id;
            this.cropType = cropType;
        }
    }

    static class FarmDataManager {
        private static final FarmDataManager instance =
                new FarmDataManager();

        private Map<String, Field> fields = new HashMap<>();

        private FarmDataManager() { }

        public static FarmDataManager getInstance() {
            return instance;
        }

        void save(Field field) {
            fields.put(field.id, field);
        }

        Field find(String id) {
            return fields.get(id);
        }
    }

    // ==================== 2. FACTORY ====================

    interface Sensor {
        String type();
        double read();
    }

    static class SoilMoistureSensor implements Sensor {
        double value;

        SoilMoistureSensor(double value) {
            this.value = value;
        }

        public String type() {
            return "moisture";
        }

        public double read() {
            return value;
        }
    }

    static class TemperatureSensor implements Sensor {
        double value;

        TemperatureSensor(double value) {
            this.value = value;
        }

        public String type() {
            return "temperature";
        }

        public double read() {
            return value;
        }
    }

    // Simple Factory
    static class SensorFactory {
        static Sensor create(String type, double value) {
            if (type.equalsIgnoreCase("moisture")) {
                return new SoilMoistureSensor(value);
            } else if (type.equalsIgnoreCase("temperature")) {
                return new TemperatureSensor(value);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported sensor: " + type);
            }
        }
    }

    // ==================== 3. ADAPTER ====================

    static class WeatherData {
        boolean rainExpected;

        WeatherData(boolean rainExpected) {
            this.rainExpected = rainExpected;
        }
    }

    interface WeatherService {
        WeatherData current();
    }

    // Simulated external weather API
    static class ExternalWeatherAPI {
        String forecast;

        ExternalWeatherAPI(String forecast) {
            this.forecast = forecast;
        }

        String fetchForecastCode() {
            return forecast;
        }
    }

    static class WeatherAPIAdapter implements WeatherService {
        private ExternalWeatherAPI api;

        WeatherAPIAdapter(ExternalWeatherAPI api) {
            this.api = api;
        }

        public WeatherData current() {
            String code = api.fetchForecastCode();

            if (code.equals("RAIN")) {
                return new WeatherData(true);
            } else if (code.equals("CLEAR")) {
                return new WeatherData(false);
            } else {
                throw new IllegalArgumentException(
                        "Unknown weather code: " + code);
            }
        }
    }

    // ==================== 4. STRATEGY ====================

    interface IrrigationStrategy {
        boolean shouldIrrigate(double moisture, WeatherData weather);
    }

    static class NormalIrrigationStrategy
            implements IrrigationStrategy {

        public boolean shouldIrrigate(double moisture, WeatherData weather) {
            return moisture < 40 && !weather.rainExpected;
        }
    }

    static class WaterSavingStrategy
            implements IrrigationStrategy {

        public boolean shouldIrrigate(double moisture, WeatherData weather) {
            return moisture < 25 && !weather.rainExpected;
        }
    }

    // ==================== 5. OBSERVER ====================

    interface AlertObserver {
        void update(String message);
    }

    static class FarmerNotifier implements AlertObserver {
        int received = 0;

        public void update(String message) {
            received++;
            System.out.println("Farmer: " + message);
        }
    }

    static class FarmDashboard implements AlertObserver {
        int received = 0;

        public void update(String message) {
            received++;
            System.out.println("Dashboard: " + message);
        }
    }

    static class AlertPublisher {
        private List<AlertObserver> observers = new ArrayList<>();

        void subscribe(AlertObserver observer) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }

        void unsubscribe(AlertObserver observer) {
            observers.remove(observer);
        }

        void publish(String message) {
            for (AlertObserver observer :
                    new ArrayList<>(observers)) {
                observer.update(message);
            }
        }
    }

    // ==================== IRRIGATION PUMP ====================

    static class IrrigationPump {
        private boolean on = false;

        void setOn(boolean on) {
            this.on = on;
        }

        boolean isOn() {
            return on;
        }
    }

    // ==================== IRRIGATION CONTROLLER ====================

    static class IrrigationController {
        private IrrigationStrategy strategy;
        private WeatherService weather;
        private IrrigationPump pump;
        private AlertPublisher alerts;

        IrrigationController(
                IrrigationStrategy strategy,
                WeatherService weather,
                IrrigationPump pump,
                AlertPublisher alerts) {

            this.strategy = strategy;
            this.weather = weather;
            this.pump = pump;
            this.alerts = alerts;
        }

        void setStrategy(IrrigationStrategy strategy) {
            this.strategy = strategy;
        }

        boolean check(Sensor sensor) {

            // Keep pump OFF until the reading is validated
            pump.setOn(false);

            if (!sensor.type().equals("moisture")) {
                throw new IllegalArgumentException(
                        "A soil moisture sensor is required");
            }

            double moisture = sensor.read();

            // Validate moisture
            if (Double.isNaN(moisture)
                    || Double.isInfinite(moisture)
                    || moisture < 0 || moisture > 100) {

                alerts.publish("Invalid moisture! Pump OFF.");

                throw new IllegalArgumentException(
                        "Moisture must be between 0 and 100");
            }

            // Obtain weather through the Adapter
            WeatherData data = weather.current();

            // Apply the selected Strategy
            boolean irrigate =
                    strategy.shouldIrrigate(moisture, data);

            // Control pump
            pump.setOn(irrigate);

            // Notify observers
            String message = "Moisture = " + moisture
                    + "%, Pump = " + (irrigate ? "ON" : "OFF");

            alerts.publish(message);

            return irrigate;
        }
    }

    // ==================== TEST HELPER ====================

    static void expect(boolean condition, String testName) {
        if (!condition) {
            throw new AssertionError("FAILED: " + testName);
        }

        System.out.println("PASS: " + testName);
    }

    static void checkCase(
            IrrigationController controller,
            IrrigationPump pump,
            double moisture,
            boolean expected,
            String testName) {

        boolean result = controller.check(
                SensorFactory.create("moisture", moisture));

        expect(result == expected && pump.isOn() == expected,
                testName);
    }

    // ==================== MAIN METHOD ====================

    public static void main(String[] args) {

        System.out.println("=== AGRICULTURAL AUTOMATION SYSTEM ===");

        // Test 1: Singleton
        FarmDataManager manager1 = FarmDataManager.getInstance();
        FarmDataManager manager2 = FarmDataManager.getInstance();

        manager1.save(new Field("F1", "Rice"));

        expect(manager1 == manager2, "Singleton returns same instance");
        expect("Rice".equals(manager2.find("F1").cropType),
                "Farm data stored successfully");

        // Test 2: Factory
        expect(SensorFactory.create("moisture", 30)
                        instanceof SoilMoistureSensor,
                "Create moisture sensor");

        expect(SensorFactory.create("temperature", 28)
                        instanceof TemperatureSensor,
                "Create temperature sensor");

        try {
            SensorFactory.create("unknown", 10);
            throw new AssertionError("Unsupported sensor was accepted");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: Unsupported sensor rejected");
        }

        // Test 3: Observer
        AlertPublisher alerts = new AlertPublisher();
        FarmerNotifier farmer = new FarmerNotifier();
        FarmDashboard dashboard = new FarmDashboard();

        alerts.subscribe(farmer);
        alerts.subscribe(dashboard);

        alerts.publish("Test alert");

        expect(farmer.received == 1 && dashboard.received == 1,
                "Both observers receive alert");

        alerts.unsubscribe(dashboard);
        alerts.publish("Second alert");

        expect(farmer.received == 2 && dashboard.received == 1,
                "Unsubscribed observer receives no new alert");

        // Register dashboard again for irrigation tests
        alerts.subscribe(dashboard);

        // Clear weather through the Adapter
        WeatherService clearWeather = new WeatherAPIAdapter(
                new ExternalWeatherAPI("CLEAR"));

        IrrigationPump pump = new IrrigationPump();

        IrrigationController controller = new IrrigationController(
                new NormalIrrigationStrategy(),
                clearWeather, pump, alerts);

        // Test 4: Normal strategy, 30%, no rain
        checkCase(controller, pump, 30, true,
                "Normal strategy: 30%, pump ON");

        // Test 5: Water-saving strategy, 30%, no rain
        controller.setStrategy(new WaterSavingStrategy());

        checkCase(controller, pump, 30, false,
                "Water-saving strategy: 30%, pump OFF");

        // Test 6: Water-saving strategy, 25%, no rain
        checkCase(controller, pump, 25, false,
                "Water-saving boundary: 25%, pump OFF");

        // Extra test: Water-saving strategy, 20%, no rain
        checkCase(controller, pump, 20, true,
                "Water-saving strategy: 20%, pump ON");

        // Test 7: Normal strategy, 40%, no rain
        controller.setStrategy(new NormalIrrigationStrategy());

        checkCase(controller, pump, 40, false,
                "Normal boundary: 40%, pump OFF");

        // Test 8: Rain expected
        WeatherService rainyWeather = new WeatherAPIAdapter(
                new ExternalWeatherAPI("RAIN"));

        IrrigationController rainyController = new IrrigationController(
                new NormalIrrigationStrategy(),
                rainyWeather, pump, alerts);

        checkCase(rainyController, pump, 20, false,
                "Rain expected: normal strategy, pump OFF");

        rainyController.setStrategy(new WaterSavingStrategy());

        checkCase(rainyController, pump, 20, false,
                "Rain expected: water-saving strategy, pump OFF");

        // Test 9: Invalid moisture readings
        double[] invalidReadings = {-5, 105};

        for (double moisture : invalidReadings) {
            pump.setOn(true);

            try {
                controller.check(
                        SensorFactory.create("moisture", moisture));

                throw new AssertionError(
                        "Invalid reading was accepted");
            } catch (IllegalArgumentException e) {
                expect(!pump.isOn(),
                        "Invalid reading " + moisture
                                + "% rejected; pump OFF");
            }
        }

        System.out.println("\n=== ALL TESTS COMPLETED ===");
    }
}
