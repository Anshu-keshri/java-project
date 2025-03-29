package com.mycompany.parkinglotvisualization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingLotVisualization extends JFrame {
    private static final int ROWS = 5;
    private static final int COLUMNS = 10;
    private JPanel parkingGridPanel;
    private List<ParkingSpace> parkingSpaces;
    private JButton startParkingButton;
    private JButton stopParkingButton;
    private JButton resetButton;
    private JLabel statusLabel;

    public ParkingLotVisualization() {
        initializeComponents();
        setupLayout();
        setupInteractions();
    }

    private void initializeComponents() {
        setTitle("Parking Lot Visualization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        parkingSpaces = new ArrayList<>();
        parkingGridPanel = new JPanel(new GridLayout(ROWS, COLUMNS, 5, 5));
        
        // Create parking spaces
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            ParkingSpace space = new ParkingSpace(i + 1);
            parkingSpaces.add(space);
            parkingGridPanel.add(space);
        }

        startParkingButton = new JButton("Start Parking");
        stopParkingButton = new JButton("Stop Parking");
        resetButton = new JButton("Reset");
        statusLabel = new JLabel("Ready: Select a parking space");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(startParkingButton);
        controlPanel.add(stopParkingButton);
        controlPanel.add(resetButton);

        add(parkingGridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void setupInteractions() {
        startParkingButton.addActionListener(e -> startParking());
        stopParkingButton.addActionListener(e -> stopParking());
        resetButton.addActionListener(e -> resetParking());
    }

    private void startParking() {
        ParkingSpace selectedSpace = parkingSpaces.stream()
            .filter(ParkingSpace::isSelected)
            .findFirst()
            .orElse(null);

        if (selectedSpace != null) {
            if (!selectedSpace.isOccupied()) {
                selectedSpace.parkCar();
                statusLabel.setText("Parked at Space " + selectedSpace.getSpaceNumber());
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Space is already occupied!", 
                    "Parking Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a parking space first!", 
                "Selection Required", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void stopParking() {
        ParkingSpace selectedSpace = parkingSpaces.stream()
            .filter(ParkingSpace::isSelected)
            .findFirst()
            .orElse(null);

        if (selectedSpace != null) {
            if (selectedSpace.isOccupied()) {
                selectedSpace.removeCar();
                statusLabel.setText("Space " + selectedSpace.getSpaceNumber() + " is now available");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Space is already empty!", 
                    "Stop Parking Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a parking space first!", 
                "Selection Required", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetParking() {
        parkingSpaces.forEach(ParkingSpace::reset);
        statusLabel.setText("Parking lot reset");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ParkingLotVisualization().setVisible(true);
        });
    }

    // Inner class for parking space representation
    private class ParkingSpace extends JPanel {
        private boolean occupied;
        private boolean selected;
        private int spaceNumber;

        public ParkingSpace(int spaceNumber) {
            this.spaceNumber = spaceNumber;
            setPreferredSize(new Dimension(60, 80));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Deselect all other spaces
                    parkingSpaces.forEach(ps -> ps.setSelected(false));
                    setSelected(true);
                }
            });
        }

        public void parkCar() {
            occupied = true;
            setBackground(Color.RED);
            repaint();
        }

        public void removeCar() {
            occupied = false;
            setBackground(Color.GREEN);
            repaint();
        }

        public void reset() {
            occupied = false;
            selected = false;
            setBackground(null);
            repaint();
        }

        public boolean isOccupied() {
            return occupied;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
            } else {
                setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }
            repaint();
        }

        public boolean isSelected() {
            return selected;
        }

        public int getSpaceNumber() {
            return spaceNumber;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(spaceNumber), 5, 15);
        }
    }
}