package com.mycompany.parkingsystem;
import com.mycompany.parkingsystem.model.ParkingSpace;
import com.mycompany.parkingsystem.model.Vehicle;
import com.mycompany.parkingsystem.model.ParkingSpaceType;
import com.mycompany.parkingsystem.algorithm.ParkingAlgorithm;
import com.mycompany.parkingsystem.visualization.ParkingVisualization;
import com.mycompany.parkingsystem.simulation.SimulationEngine;
import com.mycompany.parkingsystem.controller.VehicleController;
import com.mycompany.parkingsystem.controller.KeyboardController;
import java.awt.Color;

import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class ParkingSystem {
    // Simulation constants
    private static final int SIMULATION_FPS = 60;
    private static final long FRAME_TIME_MS = 1000 / SIMULATION_FPS;
    private static final int PARKING_LOT_WIDTH = 1000;
    private static final int PARKING_LOT_HEIGHT = 800;
    
    // Main components
    private List<ParkingSpace> parkingSpaces;
    private List<Vehicle> vehicles;
    private SimulationEngine simulationEngine;
    private ParkingVisualization visualization;
    private JFrame visualizationFrame;
    
    public ParkingSystem() {
        // Initialize components
        initializeParkingLot();
        initializeVehicles();
        
        // Create the simulation engine with collision detection
        simulationEngine = new SimulationEngine(parkingSpaces, vehicles);
        simulationEngine.setParkingLotDimensions(PARKING_LOT_WIDTH, PARKING_LOT_HEIGHT);
        
        // Create visualization
        SwingUtilities.invokeLater(() -> {
            visualizationFrame = ParkingVisualization.createAndShowGUI(parkingSpaces, vehicles);
            visualization = (ParkingVisualization)visualizationFrame.getContentPane().getComponent(0);
            
            // Add keyboard controller for user-controlled vehicle
            Vehicle userVehicle = vehicles.stream()
                .filter(Vehicle::isUserControlled)
                .findFirst()
                .orElse(null);
                
            if (userVehicle != null) {
                KeyboardController keyboardController = new KeyboardController(userVehicle);
                visualizationFrame.addKeyListener(keyboardController);
                visualizationFrame.setFocusable(true);
                visualizationFrame.requestFocus();
                
                // Add controller to simulation engine
                simulationEngine.setUserController(keyboardController);
            }
        });
    }
    
    private void initializeParkingLot() {
        parkingSpaces = new ArrayList<>();
    Random rand = new Random();
    int spaceCounter = 1; // For generating unique IDs
    
    // Section constants
    final double STANDARD_WIDTH = 2.5;
    final double STANDARD_LENGTH = 5.0;
    final double COMPACT_WIDTH = 2.0;
    final double COMPACT_LENGTH = 4.0;
    final double DISABLED_WIDTH = 3.5;
    final double DISABLED_LENGTH = 5.0;
    
    // Section 1: Standard perpendicular parking (right side)
    spaceCounter = createParkingSection(100, 200, 8, 25, 
                                      STANDARD_WIDTH, STANDARD_LENGTH, 
                                      ParkingSpaceType.PERPENDICULAR, 90, spaceCounter);
    
    // Section 2: Parallel parking (top)
    spaceCounter = createParkingSection(100, 50, 5, 50, 
                                      STANDARD_WIDTH, STANDARD_LENGTH + 1.0, // Longer for parallel
                                      ParkingSpaceType.PARALLEL, 0, spaceCounter);
    
    // Section 3: Angled parking (left side)
    spaceCounter = createParkingSection(100, 300, 6, 30, 
                                      STANDARD_WIDTH, STANDARD_LENGTH, 
                                      ParkingSpaceType.ANGLE, 45, spaceCounter);
    
    // Section 4: Compact car parking (bottom)
    spaceCounter = createParkingSection(100, 500, 10, 20, 
                                      COMPACT_WIDTH, COMPACT_LENGTH, 
                                      ParkingSpaceType.COMPACT, 90, spaceCounter);
    
    // Section 5: Disabled parking spaces
    for (int i = 0; i < 3; i++) {
        ParkingSpace space = new ParkingSpace(
            new Point2D.Double(400 + i * 35, 150), 
            DISABLED_WIDTH, DISABLED_LENGTH, 
            ParkingSpaceType.DISABLED,
            "DIS-" + (spaceCounter++));
        space.setOccupied(rand.nextDouble() > 0.7);
        parkingSpaces.add(space);
    }
    
    // Add obstacles with proper IDs
    parkingSpaces.add(new ParkingSpace(
        new Point2D.Double(250, 250), 3.0, 3.0, 
        ParkingSpaceType.OBSTACLE,
        "OBS-1"));
    parkingSpaces.add(new ParkingSpace(
        new Point2D.Double(600, 400), 4.0, 4.0, 
        ParkingSpaceType.OBSTACLE,
        "OBS-2"));
    }
    
   private int createParkingSection(double startX, double startY, int count, double spacing, 
                              double width, double length, ParkingSpaceType type, 
                              double angle, int startId) {
    Random rand = new Random();
    String prefix = type.name().substring(0, 3) + "-";
    
    for (int i = 0; i < count; i++) {
        ParkingSpace space = new ParkingSpace(
            new Point2D.Double(startX + i * spacing, startY), 
            width, length, 
            type,
            prefix + (startId + i));
        space.setAngle(angle);
        space.setOccupied(rand.nextDouble() > 0.5);
        parkingSpaces.add(space);
    }
    
    return startId + count;
}
    
    private void initializeVehicles() {
        vehicles = new ArrayList<>();
        Random rand = new Random();
        
        // Add user-controlled vehicle
        Vehicle userVehicle = new Vehicle(new Point2D.Double(50, 150), 4.5, 1.8);
        userVehicle.setUserControlled(true);
        userVehicle.setColor(new Color(94, 129, 172)); // Blue color for user vehicle
        vehicles.add(userVehicle);
        
        // Add AI-controlled vehicles with different types
        for (int i = 0; i < 5; i++) {
            double x = 200 + rand.nextInt(600);
            double y = 100 + rand.nextInt(600);
            
            // Random vehicle sizes
            double vehicleLength = 4.0 + rand.nextDouble() * 1.5; // 4.0-5.5m
            double vehicleWidth = 1.7 + rand.nextDouble() * 0.5;  // 1.7-2.2m
            
            Vehicle aiVehicle = new Vehicle(new Point2D.Double(x, y), vehicleLength, vehicleWidth);
            aiVehicle.setColor(new Color(
                150 + rand.nextInt(100), // R
                50 + rand.nextInt(100),  // G
                50 + rand.nextInt(100)   // B
            ));
            
            // Set different behaviors
            if (rand.nextBoolean()) {
                aiVehicle.setTargetParkingSpace(findRandomAvailableSpace());
            }
            
            vehicles.add(aiVehicle);
        }
    }
    
    private ParkingSpace findRandomAvailableSpace() {
        List<ParkingSpace> available = parkingSpaces.stream()
            .filter(space -> !space.isOccupied() && space.getType() != ParkingSpaceType.OBSTACLE)
            .toList();
            
        if (available.isEmpty()) return null;
        return available.get(new Random().nextInt(available.size()));
    }
    
    public void start() {
        System.out.println("Starting enhanced parking simulation...");
        System.out.println("Parking spaces: " + parkingSpaces.size());
        System.out.println("Vehicles: " + vehicles.size());
        
        // Wait for visualization to initialize
        while (visualization == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Main simulation loop with fixed timestep
        long lastTime = System.nanoTime();
        double deltaTime = 0;
        double nsPerUpdate = 1000000000.0 / SIMULATION_FPS;
        
        while (visualizationFrame != null && visualizationFrame.isVisible()) {
            long now = System.nanoTime();
            deltaTime += (now - lastTime) / nsPerUpdate;
            lastTime = now;
            
            while (deltaTime >= 1) {
                // Update simulation with fixed timestep
                simulationEngine.update(1.0 / SIMULATION_FPS);
                deltaTime--;
            }
            
            // Update visualization (independent of simulation rate)
            visualization.updateSimulation(vehicles, parkingSpaces);
            
            // Sleep to prevent CPU overuse
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Simulation ended");
    }
    
    public static void main(String[] args) {
        ParkingSystem parkingSystem = new ParkingSystem();
        parkingSystem.start();
    }
}