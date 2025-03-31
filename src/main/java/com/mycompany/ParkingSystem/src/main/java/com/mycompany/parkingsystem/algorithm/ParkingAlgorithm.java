package com.mycompany.parkingsystem.algorithm;
import com.mycompany.parkingsystem.model.ParkingSpace;
import com.mycompany.parkingsystem.model.ParkingSpaceType;
import com.mycompany.parkingsystem.model.Vehicle;
import java.awt.geom.Point2D;
import java.util.*;
import static javax.swing.plaf.synth.SynthConstants.DISABLED;
import static javax.swing.text.html.HTML.Attribute.COMPACT;

public class ParkingAlgorithm {
    private List<ParkingSpace> parkingSpaces;
    private final double SAFE_DISTANCE = 0.5; // Safe distance in meters
    private final double PARKING_PRECISION = 0.1; // Precision threshold for parking
    private final double MIN_TURNING_RADIUS = 5.0; // Minimum turning radius in meters
    
    public ParkingAlgorithm(List<ParkingSpace> parkingSpaces) {
        this.parkingSpaces = parkingSpaces;
    }

    // Enhanced parking space finder with multiple criteria
    public ParkingSpace findOptimalParkingSpace(Vehicle vehicle) {
        return parkingSpaces.stream()
            .filter(space -> !space.isOccupied())
            .filter(space -> isSpaceSuitable(vehicle, space))
            .min(Comparator.comparingDouble(space -> {
                // Combined score based on distance and difficulty
                double distanceScore = Point2D.distance(
                    vehicle.getPosition().x, vehicle.getPosition().y,
                    space.getLocation().x, space.getLocation().y);
                
                double difficultyScore = getParkingDifficulty(space.getType());
                
                // Prefer closer and easier spaces
                return distanceScore * (1 + difficultyScore * 0.3);
            }))
            .orElseGet(() -> findFallbackSpace(vehicle));
    }

    // Find a fallback space when no ideal space is available
    private ParkingSpace findFallbackSpace(Vehicle vehicle) {
        return parkingSpaces.stream()
            .filter(space -> !space.isOccupied())
            .filter(space -> space.getWidth() >= vehicle.getWidth() && 
                           space.getLength() >= vehicle.getLength())
            .findFirst()
            .orElse(null);
    }

    // Parking difficulty score (0=easy, 1=hard)
    private double getParkingDifficulty(ParkingSpaceType type) {
        switch (type) {
            case PARALLEL: return 0.8;
            case ANGLE: return 0.4;
            case PERPENDICULAR: return 0.2;
            case COMPACT: return 0.3;
            case DISABLED: return 0.1; // Disabled spaces are usually easier
            default: return 0.5;
        }
    }

    // Improved space suitability check with vehicle clearance
    private boolean isSpaceSuitable(Vehicle vehicle, ParkingSpace space) {
        double requiredWidth, requiredLength;
        
        switch (space.getType()) {
            case PARALLEL:
                requiredWidth = vehicle.getWidth() + SAFE_DISTANCE * 1.5;
                requiredLength = vehicle.getLength() + SAFE_DISTANCE * 3;
                break;
            case PERPENDICULAR:
                requiredWidth = vehicle.getLength() + SAFE_DISTANCE;
                requiredLength = vehicle.getWidth() + SAFE_DISTANCE * 1.2;
                break;
            case ANGLE:
                requiredWidth = vehicle.getWidth() * 1.4;
                requiredLength = vehicle.getLength() * 1.4;
                break;
            case COMPACT:
                requiredWidth = vehicle.getWidth() * 1.1;
                requiredLength = vehicle.getLength() * 1.1;
                break;
            case DISABLED:
                requiredWidth = vehicle.getWidth() * 1.3;
                requiredLength = vehicle.getLength() * 1.3;
                break;
            default:
                requiredWidth = vehicle.getWidth() * 1.2;
                requiredLength = vehicle.getLength() * 1.2;
        }
        
        return space.getWidth() >= requiredWidth && 
               space.getLength() >= requiredLength;
    }

    // Enhanced parking procedure with path planning
    public boolean parkVehicle(Vehicle vehicle, ParkingSpace space) {
        if (space == null) return false;
        
        System.out.printf("Initiating parking maneuver for %s space at (%.2f,%.2f)%n",
            space.getType(), space.getLocation().x, space.getLocation().y);
        
        try {
            switch (space.getType()) {
                case PARALLEL:
                    return executeParallelPark(vehicle, space);
                case PERPENDICULAR:
                    return executePerpendicularPark(vehicle, space);
                case ANGLE:
                    return executeAnglePark(vehicle, space);
                case COMPACT:
                    return executeCompactPark(vehicle, space);
                default:
                    return executeStandardPark(vehicle, space);
            }
        } catch (Exception e) {
            System.err.println("Parking failed: " + e.getMessage());
            return false;
        }
    }
    
    // Advanced parallel parking algorithm
    private boolean executeParallelPark(Vehicle vehicle, ParkingSpace space) {
        System.out.println("Executing advanced parallel parking maneuver");
        
        // Step 1: Initial positioning alongside the space
        Point2D.Double startPos = new Point2D.Double(
            space.getLocation().x + space.getLength() * 0.7,
            space.getLocation().y - vehicle.getWidth() - SAFE_DISTANCE);
        
        if (!moveVehicleTo(vehicle, startPos, 90, 0.3)) {
            System.out.println("Failed to reach initial position");
            return false;
        }
        
        // Step 2: Begin reverse maneuver with steering
        double reverseDistance = space.getLength() * 0.6;
        if (!reverseWithSteering(vehicle, -30, reverseDistance, 0.15)) {
            System.out.println("Failed first reverse maneuver");
            return false;
        }
        
        // Step 3: Counter-steer to straighten vehicle
        reverseDistance = space.getLength() * 0.4;
        if (!reverseWithSteering(vehicle, 30, reverseDistance, 0.15)) {
            System.out.println("Failed counter-steering maneuver");
            return false;
        }
        
        // Step 4: Final adjustments
        if (!centerInSpace(vehicle, space)) {
            System.out.println("Failed final positioning");
            return false;
        }
        
        space.setOccupied(true);
        System.out.println("Parallel parking completed successfully");
        return true;
    }
    
    // Enhanced perpendicular parking with turning radius consideration
    private boolean executePerpendicularPark(Vehicle vehicle, ParkingSpace space) {
        System.out.println("Executing perpendicular parking with turning radius");
        
        // Calculate approach path considering turning radius
        double approachDistance = Math.max(
            vehicle.getLength() * 1.5,
            MIN_TURNING_RADIUS * 0.8);
        
        Point2D.Double approachPos = new Point2D.Double(
            space.getLocation().x,
            space.getLocation().y - approachDistance);
        
        if (!moveVehicleTo(vehicle, approachPos, 0, 0.3)) {
            System.out.println("Failed to reach approach position");
            return false;
        }
        
        // Execute turn into space
        double entryDistance = space.getLength() * 0.8;
        if (!driveWithSteering(vehicle, 0, entryDistance, 0.2)) {
            System.out.println("Failed entry maneuver");
            return false;
        }
        
        if (!centerInSpace(vehicle, space)) {
            System.out.println("Failed final positioning");
            return false;
        }
        
        space.setOccupied(true);
        System.out.println("Perpendicular parking completed successfully");
        return true;
    }
    
    // Realistic angle parking implementation
    private boolean executeAnglePark(Vehicle vehicle, ParkingSpace space) {
        System.out.println("Executing angled parking maneuver");
        
        double angle = 45; // Standard parking angle
        double approachDistance = vehicle.getLength() * 2.0;
        
        Point2D.Double approachPos = new Point2D.Double(
            space.getLocation().x - Math.cos(Math.toRadians(angle)) * approachDistance,
            space.getLocation().y - Math.sin(Math.toRadians(angle)) * approachDistance);
        
        if (!moveVehicleTo(vehicle, approachPos, 0, 0.3)) {
            System.out.println("Failed to reach approach position");
            return false;
        }
        
        // Turn to align with parking angle
        if (!rotateVehicle(vehicle, angle, 0.1)) {
            System.out.println("Failed to align with parking angle");
            return false;
        }
        
        // Drive into space
        double entryDistance = space.getLength() * 0.9;
        if (!driveWithSteering(vehicle, 0, entryDistance, 0.2)) {
            System.out.println("Failed to enter parking space");
            return false;
        }
        
        if (!centerInSpace(vehicle, space)) {
            System.out.println("Failed final positioning");
            return false;
        }
        
        space.setOccupied(true);
        System.out.println("Angled parking completed successfully");
        return true;
    }
    
    // Compact space parking with precise movements
    private boolean executeCompactPark(Vehicle vehicle, ParkingSpace space) {
        System.out.println("Executing compact space parking");
        
        // Need extra precision for compact spaces
        double approachDistance = vehicle.getLength() * 1.2;
        Point2D.Double approachPos = new Point2D.Double(
            space.getLocation().x,
            space.getLocation().y - approachDistance);
        
        if (!moveVehicleTo(vehicle, approachPos, 0, 0.2)) {
            System.out.println("Failed to reach approach position");
            return false;
        }
        
        // Slow, precise entry
        double entryDistance = space.getLength() * 0.7;
        if (!driveWithSteering(vehicle, 0, entryDistance, 0.1)) {
            System.out.println("Failed precise entry");
            return false;
        }
        
        if (!centerInSpace(vehicle, space, 0.05)) {
            System.out.println("Failed precise positioning");
            return false;
        }
        
        space.setOccupied(true);
        System.out.println("Compact parking completed successfully");
        return true;
    }
    
    // Standard parking procedure
    private boolean executeStandardPark(Vehicle vehicle, ParkingSpace space) {
        System.out.println("Executing standard parking procedure");
        
        if (!moveVehicleTo(vehicle, space.getLocation(), 0, 0.3)) {
            System.out.println("Failed to reach parking position");
            return false;
        }
        
        space.setOccupied(true);
        System.out.println("Standard parking completed");
        return true;
    }
    
    // Enhanced vehicle movement with collision checking
    private boolean moveVehicleTo(Vehicle vehicle, Point2D.Double target, 
                                double targetAngle, double precision) {
        // Simulated movement with multiple steps
        int maxSteps = 100;
        double stepSize = 0.05;
        
        for (int i = 0; i < maxSteps; i++) {
            double dx = target.x - vehicle.getPosition().x;
            double dy = target.y - vehicle.getPosition().y;
            double distance = Math.sqrt(dx*dx + dy*dy);
            
            if (distance < precision && 
                Math.abs(normalizeAngle(vehicle.getAngle() - targetAngle)) < 5) {
                return true;
            }
            
            // Simple movement model (would be replaced with proper kinematics)
            double angleToTarget = Math.toDegrees(Math.atan2(dy, dx));
            double angleDiff = normalizeAngle(angleToTarget - vehicle.getAngle());
            
            // Adjust steering based on angle difference
            double steer = Math.max(-30, Math.min(30, angleDiff * 0.5));
            
            // Move vehicle
            vehicle.move(steer, stepSize * (distance > 1.0 ? 1.0 : 0.3));
            
            // Check for collisions (simplified)
            if (checkCollision(vehicle)) {
                System.out.println("Collision detected during movement");
                return false;
            }
        }
        
        return false;
    }
    
    // Helper method to reverse with steering
    private boolean reverseWithSteering(Vehicle vehicle, double steerAngle, 
                                      double distance, double stepSize) {
        int steps = (int)(distance / stepSize);
        
        for (int i = 0; i < steps; i++) {
            vehicle.move(steerAngle, -stepSize);
            
            if (checkCollision(vehicle)) {
                System.out.println("Collision detected while reversing");
                return false;
            }
        }
        
        return true;
    }
    
    // Helper method to drive forward with steering
    private boolean driveWithSteering(Vehicle vehicle, double steerAngle, 
                                    double distance, double stepSize) {
        int steps = (int)(distance / stepSize);
        
        for (int i = 0; i < steps; i++) {
            vehicle.move(steerAngle, stepSize);
            
            if (checkCollision(vehicle)) {
                System.out.println("Collision detected while driving");
                return false;
            }
        }
        
        return true;
    }
    
    // Helper method to rotate vehicle
    private boolean rotateVehicle(Vehicle vehicle, double targetAngle, double precision) {
        int maxSteps = 50;
        double stepSize = 2.0; // degrees per step
        
        for (int i = 0; i < maxSteps; i++) {
            double angleDiff = normalizeAngle(targetAngle - vehicle.getAngle());
            
            if (Math.abs(angleDiff) < precision) {
                return true;
            }
            
            double rotation = Math.max(-stepSize, Math.min(stepSize, angleDiff * 0.3));
            vehicle.rotate(rotation);
            
            if (checkCollision(vehicle)) {
                System.out.println("Collision detected while rotating");
                return false;
            }
        }
        
        return false;
    }
    
    // Enhanced centering in space with precision parameter
    private boolean centerInSpace(Vehicle vehicle, ParkingSpace space, double precision) {
        Point2D.Double targetPos;
        double targetAngle;
        
        switch (space.getType()) {
            case PARALLEL:
                targetPos = new Point2D.Double(
                    space.getLocation().x + space.getLength()/2,
                    space.getLocation().y + vehicle.getWidth()/2);
                targetAngle = 90;
                break;
            case PERPENDICULAR:
                targetPos = new Point2D.Double(
                    space.getLocation().x + vehicle.getWidth()/2,
                    space.getLocation().y + space.getLength()/2);
                targetAngle = 0;
                break;
            case ANGLE:
                targetPos = new Point2D.Double(
                    space.getLocation().x + space.getLength() * 0.4,
                    space.getLocation().y + space.getWidth() * 0.4);
                targetAngle = 45;
                break;
            default:
                targetPos = space.getLocation();
                targetAngle = 0;
        }
        
        return moveVehicleTo(vehicle, targetPos, targetAngle, precision);
    }
    
    private boolean centerInSpace(Vehicle vehicle, ParkingSpace space) {
        return centerInSpace(vehicle, space, PARKING_PRECISION);
    }
    
    // Basic collision detection
    private boolean checkCollision(Vehicle vehicle) {
        // Simplified collision check - would be implemented with proper geometry
        // in a real simulation
        return false;
    }
    
    // Helper method to normalize angles to [-180,180] range
    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}