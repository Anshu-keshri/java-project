package com.mycompany.parkingsystem.visualization;

import com.mycompany.parkingsystem.model.ParkingSpace;
import com.mycompany.parkingsystem.model.Vehicle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Random;

public class ParkingVisualization extends JPanel {

    public static JFrame createAndShowGUI(List<ParkingSpace> parkingSpaces, List<Vehicle> vehicles) {
        JFrame frame = new JFrame("Parking System Visualization");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    ParkingVisualization visualization = new ParkingVisualization(parkingSpaces, vehicles);
    frame.add(visualization);
    
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    
    return frame;
    }
    private List<ParkingSpace> parkingSpaces;
    private List<Vehicle> vehicles;
    private static final Random RANDOM = new Random();
    
    // Display constants
    private static final Color BACKGROUND_COLOR = new Color(46, 52, 64);
    private static final Color ROAD_COLOR = new Color(67, 76, 94);
    private static final Color PARKING_SPACE_COLOR = new Color(216, 222, 233);
    private static final Color PARKING_SPACE_BORDER_COLOR = new Color(129, 161, 193);
    private static final Color AVAILABLE_SPACE_COLOR = new Color(163, 190, 140, 200);
    private static final Color USER_VEHICLE_COLOR = new Color(94, 129, 172);
    private static final Color AI_VEHICLE_COLOR = new Color(191, 97, 106);
    private static final Font INFO_FONT = new Font("Arial", Font.BOLD, 16);
    private static final double SCALE = 2.0;
    
    // Textures for parking lot
    private static final Color[] ASPHALT_TEXTURE = {
        new Color(60, 70, 90), new Color(65, 75, 95), new Color(70, 80, 100)
    };
    
    public ParkingVisualization(List<ParkingSpace> parkingSpaces, List<Vehicle> vehicles) {
        this.parkingSpaces = parkingSpaces;
        this.vehicles = vehicles;
        setPreferredSize(new Dimension((int) (1000 * SCALE), (int) (800 * SCALE)));
        setBackground(BACKGROUND_COLOR);
    }
    
    // ... [keep createAndShowGUI and updateSimulation methods the same] ...

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(SCALE, SCALE);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        drawParkingLotBackground(g2d);
        drawParkingSpaces(g2d);
        drawParkingSpaceMarkings(g2d);
        drawParkedCars(g2d);
        drawVehicles(g2d);
        drawInformationOverlay(g2d);
    }
    
    private void drawParkingLotBackground(Graphics2D g2d) {
        // Draw road surface with texture
        g2d.setColor(ROAD_COLOR);
        g2d.fillRect(0, 0, 1000, 800);
        
        // Add subtle asphalt texture
        for (int i = 0; i < 500; i++) {
            int x = RANDOM.nextInt(1000);
            int y = RANDOM.nextInt(800);
            int size = RANDOM.nextInt(3) + 1;
            g2d.setColor(ASPHALT_TEXTURE[RANDOM.nextInt(ASPHALT_TEXTURE.length)]);
            g2d.fillOval(x, y, size, size);
        }
    }
    
    private void drawParkingSpaces(Graphics2D g2d) {
        if (parkingSpaces == null) return;
        
        for (ParkingSpace space : parkingSpaces) {
            Rectangle2D rect = new Rectangle2D.Double(
                space.getLocation().getX(), 
                space.getLocation().getY(), 
                space.getLength() * 8, 
                space.getWidth() * 8
            );
            
            // Gradient for parking space
            GradientPaint gradient = new GradientPaint(
                (float)rect.getX(), (float)rect.getY(), 
                space.isOccupied() ? PARKING_SPACE_COLOR : AVAILABLE_SPACE_COLOR,
                (float)(rect.getX() + rect.getWidth()), (float)(rect.getY() + rect.getHeight()),
                space.isOccupied() ? PARKING_SPACE_COLOR.darker() : AVAILABLE_SPACE_COLOR.darker()
            );
            
            g2d.setPaint(gradient);
            g2d.fill(rect);
            g2d.setColor(PARKING_SPACE_BORDER_COLOR);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(rect);
        }
    }
    
    private void drawParkingSpaceMarkings(Graphics2D g2d) {
        if (parkingSpaces == null) return;
        
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        for (ParkingSpace space : parkingSpaces) {
            if (!space.isOccupied()) {
                double centerX = space.getLocation().getX() + space.getLength() * 4;
                double centerY = space.getLocation().getY() + space.getWidth() * 4;
                
                // Draw parking space number
                String spaceNumber = String.valueOf(space.getId());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(spaceNumber);
                int textHeight = fm.getHeight();
                
                g2d.drawString(spaceNumber, 
                    (float)(centerX - textWidth/2), 
                    (float)(centerY + textHeight/3));
            }
        }
    }
    
    private void drawParkedCars(Graphics2D g2d) {
        if (parkingSpaces == null) return;
        
        for (ParkingSpace space : parkingSpaces) {
            if (space.isOccupied()) {
                Color baseColor = new Color(
                    RANDOM.nextInt(40) + 30, 
                    RANDOM.nextInt(40) + 30, 
                    RANDOM.nextInt(40) + 30);
                
                double x = space.getLocation().getX() + 5;
                double y = space.getLocation().getY() + 5;
                double width = space.getLength() * 6;
                double height = space.getWidth() * 6;
                
                // Car shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fill(new RoundRectangle2D.Double(x+2, y+3, width, height, 10, 10));
                
                // Car body with gradient
                GradientPaint carGradient = new GradientPaint(
                    (float)x, (float)y, baseColor.brighter(),
                    (float)x, (float)(y + height), baseColor.darker()
                );
                g2d.setPaint(carGradient);
                RoundRectangle2D carShape = new RoundRectangle2D.Double(x, y, width, height, 10, 10);
                g2d.fill(carShape);
                
                // Car details
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(carShape);
                
                // Windows
                g2d.setColor(new Color(180, 220, 255, 150));
                g2d.fill(new RoundRectangle2D.Double(
                    x + width * 0.2, y + height * 0.15, 
                    width * 0.6, height * 0.25, 5, 5));
                
                // Windshield divider
                g2d.setColor(Color.BLACK);
                g2d.drawLine(
                    (int)(x + width * 0.5), (int)(y + height * 0.15),
                    (int)(x + width * 0.5), (int)(y + height * 0.4));
                
                // Headlights
                g2d.setColor(new Color(255, 255, 200));
                g2d.fillOval((int)(x + width - 8), (int)(y + 5), 6, 4);
                g2d.fillOval((int)(x + width - 8), (int)(y + height - 9), 6, 4);
                
                // Tires with highlights
                g2d.setColor(Color.BLACK);
                g2d.fill(new Ellipse2D.Double(x + 3, y + 3, 8, 8));
                g2d.fill(new Ellipse2D.Double(x + width - 11, y + 3, 8, 8));
                g2d.fill(new Ellipse2D.Double(x + 3, y + height - 11, 8, 8));
                g2d.fill(new Ellipse2D.Double(x + width - 11, y + height - 11, 8, 8));
                
                // Tire highlights
                g2d.setColor(new Color(80, 80, 80));
                g2d.drawOval((int)(x + 4), (int)(y + 4), 6, 6);
                g2d.drawOval((int)(x + width - 10), (int)(y + 4), 6, 6);
                g2d.drawOval((int)(x + 4), (int)(y + height - 10), 6, 6);
                g2d.drawOval((int)(x + width - 10), (int)(y + height - 10), 6, 6);
            }
        }
    }
    
    private void drawVehicles(Graphics2D g2d) {
        if (vehicles == null) return;
        
        for (Vehicle vehicle : vehicles) {
            AffineTransform originalTransform = g2d.getTransform();
            AffineTransform transform = new AffineTransform();
            transform.translate(vehicle.getPosition().getX(), vehicle.getPosition().getY());
            transform.rotate(Math.toRadians(vehicle.getAngle()));
            g2d.transform(transform);
            
            // Car dimensions
            double carLength = vehicle.getLength() * 8;
            double carWidth = vehicle.getWidth() * 8;
            
            // Car shadow
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fill(new RoundRectangle2D.Double(
                -carLength/2 + 2, -carWidth/2 + 3, 
                carLength, carWidth, 10, 10));
            
            // Car body with gradient
            Color baseColor = vehicle.isUserControlled() ? USER_VEHICLE_COLOR : AI_VEHICLE_COLOR;
            GradientPaint carGradient = new GradientPaint(
                (float)(-carLength/2), (float)(-carWidth/2), baseColor.brighter(),
                (float)(-carLength/2), (float)(carWidth/2), baseColor.darker()
            );
            g2d.setPaint(carGradient);
            RoundRectangle2D carShape = new RoundRectangle2D.Double(
                -carLength/2, -carWidth/2, 
                carLength, carWidth, 10, 10);
            g2d.fill(carShape);
            
            // Car details
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(carShape);
            
            // Windows
            g2d.setColor(new Color(180, 220, 255, 150));
            g2d.fill(new RoundRectangle2D.Double(
                -carLength/2 + carLength*0.2, -carWidth/2 + carWidth*0.15, 
                carLength*0.6, carWidth*0.25, 5, 5));
            
            // Windshield divider
            g2d.setColor(Color.BLACK);
            g2d.drawLine(0, (int)(-carWidth/2 + carWidth*0.15), 
                         0, (int)(-carWidth/2 + carWidth*0.4));
            
            // Headlights (front)
            g2d.setColor(new Color(255, 255, 200));
            g2d.fillOval((int)(carLength/2 - 8), (int)(-carWidth/2 + 5), 6, 4);
            g2d.fillOval((int)(carLength/2 - 8), (int)(carWidth/2 - 9), 6, 4);
            
            // Taillights (rear)
            g2d.setColor(new Color(200, 0, 0));
            g2d.fillOval((int)(-carLength/2 + 2), (int)(-carWidth/2 + 5), 6, 4);
            g2d.fillOval((int)(-carLength/2 + 2), (int)(carWidth/2 - 9), 6, 4);
            
            // Tires
            g2d.setColor(Color.BLACK);
            g2d.fill(new Ellipse2D.Double(
                -carLength/2 + 3, -carWidth/2 + 3, 8, 8));
            g2d.fill(new Ellipse2D.Double(
                carLength/2 - 11, -carWidth/2 + 3, 8, 8));
            g2d.fill(new Ellipse2D.Double(
                -carLength/2 + 3, carWidth/2 - 11, 8, 8));
            g2d.fill(new Ellipse2D.Double(
                carLength/2 - 11, carWidth/2 - 11, 8, 8));
            
            // Tire highlights
            g2d.setColor(new Color(80, 80, 80));
            g2d.drawOval((int)(-carLength/2 + 4), (int)(-carWidth/2 + 4), 6, 6);
            g2d.drawOval((int)(carLength/2 - 10), (int)(-carWidth/2 + 4), 6, 6);
            g2d.drawOval((int)(-carLength/2 + 4), (int)(carWidth/2 - 10), 6, 6);
            g2d.drawOval((int)(carLength/2 - 10), (int)(carWidth/2 - 10), 6, 6);
            
            // Speed effect (motion blur for fast-moving vehicles)
            if (vehicle.getSpeed() > 1.0) {
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fill(new Rectangle2D.Double(
                    -carLength/2 - 5, -carWidth/2, 
                    5, carWidth));
            }
            
            g2d.setTransform(originalTransform);
        }
    }
    
    private void drawInformationOverlay(Graphics2D g2d) {
        // Semi-transparent background for info
        g2d.setColor(new Color(46, 52, 64, 200));
        g2d.fillRoundRect(10, 10, 300, 100, 15, 15);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(INFO_FONT);
        
        g2d.drawString("Real-time Parking Simulation", 20, 30);
        g2d.drawString("Controls: Arrow keys to drive, Space to brake", 20, 55);
        
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isUserControlled()) {
                // Speed indicator with color coding
                double speed = Math.abs(vehicle.getSpeed());
                if (speed > 3) g2d.setColor(Color.RED);
                else if (speed > 1.5) g2d.setColor(Color.ORANGE);
                else g2d.setColor(Color.GREEN);
                
                g2d.drawString("Speed: " + String.format("%.1f", speed) + " m/s", 20, 80);
                
                // Gear indicator
                g2d.setColor(Color.WHITE);
                String gear = vehicle.getSpeed() >= 0 ? "D" : "R";
                g2d.drawString("Gear: " + gear, 20, 105);
            }
        }
    }

    public void updateSimulation(List<Vehicle> vehicles, List<ParkingSpace> parkingSpaces) {
        if (vehicles == null || parkingSpaces == null) {
        throw new IllegalArgumentException("Arguments cannot be null");
    }
    this.vehicles = vehicles;
    this.parkingSpaces = parkingSpaces;
    repaint();
    }
}