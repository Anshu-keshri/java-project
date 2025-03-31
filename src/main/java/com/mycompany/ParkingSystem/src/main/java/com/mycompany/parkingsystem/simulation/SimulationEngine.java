package com.mycompany.parkingsystem.simulation;

import com.mycompany.parkingsystem.model.ParkingSpace;
import com.mycompany.parkingsystem.model.Vehicle;
import com.mycompany.parkingsystem.model.ParkingSpaceType;
import com.mycompany.parkingsystem.algorithm.ParkingAlgorithm;
import com.mycompany.parkingsystem.controller.KeyboardController;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SimulationEngine {
    private List<ParkingSpace> parkingSpaces;
    private List<Vehicle> vehicles;
    private ParkingAlgorithm parkingAlgorithm;
    private KeyboardController userController;
    private Random random;
    
    // Simulation parameters
    private static final double AI_DECISION_INTERVAL = 1.5; // seconds
    private static final double COLLISION_CHECK_INTERVAL = 0.1; // seconds
    private static final double PARKING_ATTEMPT_RATE = 0.3; // probability
    
    // World boundaries
    private static final double WORLD_WIDTH = 1000;
    private static final double WORLD_HEIGHT = 800;
    
    // Timers
    private double aiTimer = 0;
    private double collisionTimer = 0;
    
    public SimulationEngine(List<ParkingSpace> parkingSpaces, List<Vehicle> vehicles) {
        this.parkingSpaces = parkingSpaces;
        this.vehicles = vehicles;
        this.parkingAlgorithm = new ParkingAlgorithm(parkingSpaces);
        this.random = new Random();
    }
    
    public void update(double deltaTime) {
        // Update all vehicles
        vehicles.forEach(vehicle -> {
            vehicle.update(deltaTime);
            
            // Handle AI-controlled vehicles
            if (!vehicle.isUserControlled() && !vehicle.isParked()) {
                updateAIBehavior(vehicle, deltaTime);
            }
            
            // Keep vehicles within world bounds
            enforceWorldBoundaries(vehicle);
        });
        
        // Periodic collision checking
        collisionTimer += deltaTime;
        if (collisionTimer >= COLLISION_CHECK_INTERVAL) {
            collisionTimer = 0;
            checkCollisions();
        }
    }
    
    private void updateAIBehavior(Vehicle vehicle, double deltaTime) {
        aiTimer += deltaTime;
        
        if (aiTimer >= AI_DECISION_INTERVAL) {
            aiTimer = 0;
            
            // Decision making for AI vehicles
            if (random.nextDouble() < PARKING_ATTEMPT_RATE) {
                attemptParking(vehicle);
            } else {
                randomDriving(vehicle);
            }
        }
    }
    
    private void attemptParking(Vehicle vehicle) {
        ParkingSpace targetSpace = parkingAlgorithm.findOptimalParkingSpace(vehicle);
        
        if (targetSpace != null) {
            // Calculate path to parking space
            Point2D.Double approachPoint = calculateApproachPoint(vehicle, targetSpace);
            
            // If close enough, execute parking maneuver
            if (isWithinRange(vehicle.getPosition(), targetSpace.getLocation(), 15)) {
                boolean parked = parkingAlgorithm.parkVehicle(vehicle, targetSpace);
                if (parked) {
                    vehicle.setParked(true);
                }
            } else {
                // Move toward approach point
                navigateToPoint(vehicle, approachPoint);
            }
        } else {
            // No available space - wander around
            randomDriving(vehicle);
        }
    }
    
    private Point2D.Double calculateApproachPoint(Vehicle vehicle, ParkingSpace space) {
        // Calculate appropriate approach point based on parking space type
        double offsetX = 0, offsetY = 0;
        
        switch (space.getType()) {
            case PARALLEL:
                offsetX = space.getLength() * 0.7;
                offsetY = -vehicle.getWidth() - 1.0;
                break;
            case PERPENDICULAR:
                offsetX = 0;
                offsetY = -vehicle.getLength() - 2.0;
                break;
            case ANGLE:
                double angleRad = Math.toRadians(45); // Standard parking angle
                offsetX = -Math.cos(angleRad) * 10;
                offsetY = -Math.sin(angleRad) * 10;
                break;
            default:
                offsetX = 0;
                offsetY = -5;
        }
        
        return new Point2D.Double(
            space.getLocation().x + offsetX,
            space.getLocation().y + offsetY
        );
    }
    
    private void navigateToPoint(Vehicle vehicle, Point2D.Double target) {
        // Calculate direction vector
        double dx = target.x - vehicle.getPosition().x;
        double dy = target.y - vehicle.getPosition().y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Calculate desired angle (0-360 degrees)
        double desiredAngle = Math.toDegrees(Math.atan2(dy, dx));
        
        // Normalize angles for comparison
        double currentAngle = normalizeAngle(vehicle.getAngle());
        desiredAngle = normalizeAngle(desiredAngle);
        
        // Calculate steering direction
        double angleDiff = normalizeAngle(desiredAngle - currentAngle);
        double steerDirection = Math.signum(angleDiff);
        
        // Adjust steering based on angle difference
        if (Math.abs(angleDiff) > 30) {
            vehicle.steer(steerDirection * 0.8);
        } else if (Math.abs(angleDiff) > 10) {
            vehicle.steer(steerDirection * 0.5);
        } else {
            vehicle.centerSteering();
        }
        
        // Adjust speed based on distance
        if (distance > 20) {
            vehicle.accelerate(0.8);
        } else if (distance > 10) {
            vehicle.accelerate(0.5);
        } else {
            vehicle.brake(0.3);
        }
    }
    
    private void randomDriving(Vehicle vehicle) {
        // Random direction changes
        if (random.nextDouble() < 0.2) {
            vehicle.steer(random.nextDouble() * 2 - 1);
        }
        
        // Random speed changes
        if (random.nextDouble() < 0.3) {
            if (random.nextBoolean()) {
                vehicle.accelerate(random.nextDouble() * 0.5 + 0.3);
            } else {
                vehicle.brake(random.nextDouble() * 0.5);
            }
        }
    }
    
    private void enforceWorldBoundaries(Vehicle vehicle) {
        double margin = 5.0;
        double x = vehicle.getPosition().x;
        double y = vehicle.getPosition().y;
        
        if (x < margin) {
            vehicle.setPosition(new Point2D.Double(margin, y));
            vehicle.setSpeed(-vehicle.getSpeed() * 0.5);
        } else if (x > WORLD_WIDTH - margin) {
            vehicle.setPosition(new Point2D.Double(WORLD_WIDTH - margin, y));
            vehicle.setSpeed(-vehicle.getSpeed() * 0.5);
        }
        
        if (y < margin) {
            vehicle.setPosition(new Point2D.Double(x, margin));
            vehicle.setSpeed(-vehicle.getSpeed() * 0.5);
        } else if (y > WORLD_HEIGHT - margin) {
            vehicle.setPosition(new Point2D.Double(x, WORLD_HEIGHT - margin));
            vehicle.setSpeed(-vehicle.getSpeed() * 0.5);
        }
    }
    
    private void checkCollisions() {
        // Check vehicle-vehicle collisions
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle v1 = vehicles.get(i);
            if (v1.isParked()) continue;
            
            Path2D v1Shape = createVehicleShape(v1);
            
            for (int j = i + 1; j < vehicles.size(); j++) {
                Vehicle v2 = vehicles.get(j);
                if (v2.isParked()) continue;
                
                Path2D v2Shape = createVehicleShape(v2);
                
                if (v1Shape.intersects(v2Shape.getBounds2D())) {
                    handleCollision(v1, v2);
                }
            }
            
            // Check vehicle-space collisions
            for (ParkingSpace space : parkingSpaces) {
                if (space.isOccupied() && space.getType() == ParkingSpaceType.OBSTACLE) {
                    Rectangle2D spaceBounds = new Rectangle2D.Double(
                        space.getLocation().x,
                        space.getLocation().y,
                        space.getLength(),
                        space.getWidth());
                    
                    if (v1Shape.intersects(spaceBounds)) {
                        handleObstacleCollision(v1, space);
                    }
                }
            }
        }
    }
    
    private Path2D createVehicleShape(Vehicle vehicle) {
        Path2D shape = new Path2D.Double();
        Point2D.Double[] corners = vehicle.getCornerPoints();
        
        shape.moveTo(corners[0].x, corners[0].y);
        for (int i = 1; i < corners.length; i++) {
            shape.lineTo(corners[i].x, corners[i].y);
        }
        shape.closePath();
        
        return shape;
    }
    
    private void handleCollision(Vehicle v1, Vehicle v2) {
        // Calculate collision normal (direction from v1 to v2)
        double dx = v2.getPosition().x - v1.getPosition().x;
        double dy = v2.getPosition().y - v1.getPosition().y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        dx /= distance;
        dy /= distance;
        
        // Calculate relative velocity
        double vx = v2.getVelocity().x - v1.getVelocity().x;
        double vy = v2.getVelocity().y - v1.getVelocity().y;
        
        // Calculate impulse along collision normal
        double impulse = (vx * dx + vy * dy) * 0.8; // Coefficient of restitution
        
        // Apply impulse
        v1.setSpeed(v1.getSpeed() - impulse * 0.5);
        v2.setSpeed(v2.getSpeed() + impulse * 0.5);
        
        // Small random angle change to avoid deadlock
        v1.rotate((random.nextDouble() - 0.5) * 10);
        v2.rotate((random.nextDouble() - 0.5) * 10);
    }
    
    private void handleObstacleCollision(Vehicle vehicle, ParkingSpace obstacle) {
        // Simple bounce back from obstacle
        vehicle.setSpeed(-vehicle.getSpeed() * 0.7);
        vehicle.rotate((random.nextDouble() - 0.5) * 20);
    }
    
    private double normalizeAngle(double angle) {
        angle %= 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
    
    private boolean isWithinRange(Point2D.Double p1, Point2D.Double p2, double range) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return (dx * dx + dy * dy) <= (range * range);
    }
    
    public void setUserController(KeyboardController controller) {
        this.userController = controller;
        for (Vehicle v : vehicles) {
        if (v.isUserControlled()) {
            controller.setControlledVehicle(v);
            break;
        }
    }
    }
    
    public List<Vehicle> getVehicles() {
        return vehicles;
    }
    
    public List<ParkingSpace> getParkingSpaces() {
        return parkingSpaces;
    }

    public void setParkingLotDimensions(int PARKING_LOT_WIDTH, int PARKING_LOT_HEIGHT) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}