package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.models.User;
import lk.bustracking.depotmanagementsystem.db.Database;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import netscape.javascript.JSObject;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;

/**
 * Optimized GPS Tracking Panel with Bug Fixes (OpenStreetMap Version)
 * - Fixed duplicate data issues
 * - Search-based bus display (not auto-show all)
 * - Realistic route paths using actual roads
 * - Performance optimizations
 */
public class GPSTrackingPanel extends BorderPane {
    
    private static final Logger LOGGER = Logger.getLogger(GPSTrackingPanel.class.getName());
    
    // UI Components
    private User currentUser;
    private WebView mapWebView;
    private WebEngine webEngine;
    private ComboBox<String> busSearchCombo;
    private Button searchButton;
    private Button showAllButton;
    private Button showDevicesButton;
    private Label statusLabel;
    private TableView<GPSDeviceData> deviceTable;
    private ObservableList<GPSDeviceData> deviceData;
    
    // Auto-refresh
    private ScheduledExecutorService refreshScheduler;
    private volatile boolean autoRefreshEnabled = true;
    private boolean mapInitialized = false;
    
    // Tracking state
    private String currentTrackedBus = null;
    private Set<String> displayedBuses = new HashSet<>();
    
    /**
     * GPS Device Data Model for Table View
     */
    public static class GPSDeviceData {
        private String gpsDeviceId;
        private String busNumber;
        private String routeName;
        private Double latitude;
        private Double longitude;
        private Double speedKmh;
        private String lastUpdate;
        private String status;
        
        public GPSDeviceData(String gpsDeviceId, String busNumber, String routeName, 
                             Double latitude, Double longitude, Double speedKmh, String lastUpdate, String status) {
            this.gpsDeviceId = gpsDeviceId;
            this.busNumber = busNumber;
            this.routeName = routeName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.speedKmh = speedKmh;
            this.lastUpdate = lastUpdate;
            this.status = status;
        }
        
        // Getters and setters
        public String getGpsDeviceId() { return gpsDeviceId; }
        public void setGpsDeviceId(String gpsDeviceId) { this.gpsDeviceId = gpsDeviceId; }
        public String getBusNumber() { return busNumber; }
        public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
        public String getRouteName() { return routeName; }
        public void setRouteName(String routeName) { this.routeName = routeName; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Double getSpeedKmh() { return speedKmh; }
        public void setSpeedKmh(Double speedKmh) { this.speedKmh = speedKmh; }
        public String getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public GPSTrackingPanel(User user) {
        this.currentUser = user;
        this.deviceData = FXCollections.observableArrayList();
        
        initializeUI();
        loadBusData();
        startAutoRefresh();
        
        LOGGER.info("Optimized GPS Tracking Panel initialized for user: " + user.getUsername());
    }
    
    private void initializeUI() {
        this.setStyle("-fx-background-color: #f8f9fc;");
        
        HBox topBar = createTopControlBar();
        VBox mapArea = createMapArea();
        createDeviceTable();
        
        this.setTop(topBar);
        this.setCenter(mapArea);
        
        BorderPane.setMargin(topBar, new Insets(10));
        BorderPane.setMargin(mapArea, new Insets(0, 10, 10, 10));
    }
    
    private HBox createTopControlBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e1e5e9; -fx-border-radius: 8;");
        
        Label titleLabel = new Label("🗺 Live GPS Tracking");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Label searchLabel = new Label("Search Bus:");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        busSearchCombo = new ComboBox<>();
        busSearchCombo.setPromptText("Select bus number...");
        busSearchCombo.setPrefWidth(150);
        busSearchCombo.setEditable(false);
        
        searchButton = new Button("🔍 Track Bus");
        searchButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        searchButton.setOnAction(e -> searchAndTrackBus());
        
        showAllButton = new Button("🌍 Show All");
        showAllButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        showAllButton.setOnAction(e -> showAllBuses());
        
        Button clearButton = new Button("🗑 Clear");
        clearButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        clearButton.setOnAction(e -> clearMap());
        
        showDevicesButton = new Button("📊 GPS Table");
        showDevicesButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        showDevicesButton.setOnAction(e -> showGPSDevicesTable());
        
        statusLabel = new Label("Ready - Select a bus to track");
        statusLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        topBar.getChildren().addAll(titleLabel, spacer1, searchLabel, busSearchCombo, searchButton, showAllButton, clearButton, showDevicesButton, statusLabel);
        
        return topBar;
    }
    
    private VBox createMapArea() {
        VBox mapContainer = new VBox();
        mapContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e1e5e9; -fx-border-radius: 8;");
        
        HBox mapHeader = new HBox();
        mapHeader.setPadding(new Insets(10));
        mapHeader.setAlignment(Pos.CENTER_LEFT);
        mapHeader.setStyle("-fx-border-color: #e1e5e9; -fx-border-width: 0 0 1 0;");
        
        Label mapTitle = new Label(" 🌍 Live Bus Locations - Sri Lanka");
        mapTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label mapInfo = new Label("Search and track specific buses • Updates every 5 seconds");
        mapInfo.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        
        mapHeader.getChildren().addAll(mapTitle, spacer, mapInfo);
        
        mapWebView = new WebView();
        webEngine = mapWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", this);
                mapInitialized = true;
                Platform.runLater(() -> {
                    statusLabel.setText("Map loaded - Ready to track buses");
                });
            }
        });
        
        loadMapHTML();
        
        VBox.setVgrow(mapWebView, Priority.ALWAYS);
        mapContainer.getChildren().addAll(mapHeader, mapWebView);
        
        return mapContainer;
    }
    
    private void loadMapHTML() {
        String mapHTML = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>CTB GPS Tracking</title>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
                <link rel="stylesheet" href="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.css" />
                <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
                <script src="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.js"></script>
                <style>
                    body { margin: 0; padding: 0; }
                    #map { height: 100vh; width: 100%; }
                    .bus-marker { 
                        background-color: #e74c3c; 
                        border: 2px solid white; 
                        border-radius: 50%; 
                        width: 14px; 
                        height: 14px; 
                        box-shadow: 0 2px 4px rgba(0,0,0,0.3);
                    }
                    .bus-marker.selected { 
                        background-color: #f1c40f; 
                        width: 18px; 
                        height: 18px;
                        border: 3px solid white;
                    }
                    .bus-popup {
                        font-family: Arial, sans-serif;
                        min-width: 250px;
                    }
                    .popup-title {
                        font-weight: bold;
                        color: #2c3e50;
                        margin-bottom: 8px;
                        font-size: 16px;
                    }
                    .popup-info {
                        color: #34495e;
                        font-size: 13px;
                        margin-bottom: 3px;
                    }
                    .leaflet-routing-container {
                        display: none;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                
                <script>
                    var map = L.map('map').setView([7.8731, 80.7718], 8);
                    
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors',
                        maxZoom: 18
                    }).addTo(map);
                    
                    var busMarkers = {};
                    var routePaths = {};
                    var selectedBus = null;
                    
                    function clearMap() {
                        Object.values(busMarkers).forEach(marker => map.removeLayer(marker));
                        busMarkers = {};
                        Object.values(routePaths).forEach(route => map.removeLayer(route));
                        routePaths = {};
                        selectedBus = null;
                        console.log('Map cleared');
                    }
                    
                    function addRealisticRoute(routeId, startLat, startLon, endLat, endLon, routeName) {
                        if (routePaths[routeId]) {
                            map.removeLayer(routePaths[routeId]);
                        }
                        
                        var routing = L.Routing.control({
                            waypoints: [
                                L.latLng(startLat, startLon),
                                L.latLng(endLat, endLon)
                            ],
                            routeWhileDragging: false,
                            addWaypoints: false,
                            createMarker: function() { return null; },
                            lineOptions: {
                                styles: [{ color: '#3498db', weight: 4, opacity: 0.8 }]
                            },
                            show: false,
                            router: L.Routing.osrmv1({
                                serviceUrl: 'https://router.project-osrm.org/route/v1'
                            })
                        }).addTo(map);
                        
                        routePaths[routeId] = routing;
                        console.log('Added realistic route for: ' + routeName);
                    }
                    
                    function updateBusMarker(gpsDeviceId, busNumber, latitude, longitude, speed, routeName, isSelected) {
                        if (busMarkers[gpsDeviceId]) {
                            map.removeLayer(busMarkers[gpsDeviceId]);
                        }
                        
                        var iconClass = isSelected ? 'bus-marker selected' : 'bus-marker';
                        var busIcon = L.divIcon({
                            className: iconClass,
                            iconSize: isSelected ? [18, 18] : [14, 14],
                            iconAnchor: isSelected ? [9, 9] : [7, 7]
                        });
                        
                        var marker = L.marker([latitude, longitude], {icon: busIcon}).addTo(map);
                        
                        var statusIcon = speed > 5 ? '🟢' : '🟡';
                        var popupContent = 
                            '<div class="bus-popup">' +
                            '<div class="popup-title">' + statusIcon + ' ' + busNumber + '</div>' +
                            '<div class="popup-info"><b>Route:</b> ' + (routeName || 'Not assigned') + '</div>' +
                            '<div class="popup-info"><b>Speed:</b> ' + speed.toFixed(1) + ' km/h</div>' +
                            '<div class="popup-info"><b>GPS Device:</b> ' + gpsDeviceId + '</div>' +
                            '<div class="popup-info"><b>Location:</b> ' + latitude.toFixed(6) + ', ' + longitude.toFixed(6) + '</div>' +
                            '<div class="popup-info"><b>Status:</b> ' + (speed > 5 ? 'Moving' : 'Stopped') + '</div>' +
                            '</div>';
                        
                        marker.bindPopup(popupContent);
                        
                        if (isSelected) {
                            marker.openPopup();
                            selectedBus = gpsDeviceId;
                        }
                        
                        busMarkers[gpsDeviceId] = marker;
                    }
                    
                    function focusBus(latitude, longitude, zoomLevel) {
                        var zoom = zoomLevel || 15;
                        map.setView([latitude, longitude], zoom);
                    }
                    
                    function showAllBuses() {
                        if (Object.keys(busMarkers).length > 0) {
                            var group = new L.featureGroup(Object.values(busMarkers));
                            map.fitBounds(group.getBounds().pad(0.1));
                        }
                    }
                    
                    window.clearMap = clearMap;
                    window.updateBusMarker = updateBusMarker;
                    window.addRealisticRoute = addRealisticRoute;
                    window.focusBus = focusBus;
                    window.showAllBuses = showAllBuses;
                    
                    console.log('Optimized GPS tracking map initialized');
                </script>
            </body>
            </html>
            """;
        
        webEngine.loadContent(mapHTML);
    }
    
    private void createDeviceTable() {
        deviceTable = new TableView<>();
        deviceTable.setItems(deviceData);
        
        TableColumn<GPSDeviceData, String> deviceIdCol = new TableColumn<>("GPS Device");
        deviceIdCol.setCellValueFactory(new PropertyValueFactory<>("gpsDeviceId"));
        deviceIdCol.setPrefWidth(120);
        
        TableColumn<GPSDeviceData, String> busNumberCol = new TableColumn<>("Bus Number");
        busNumberCol.setCellValueFactory(new PropertyValueFactory<>("busNumber"));
        busNumberCol.setPrefWidth(100);
        
        TableColumn<GPSDeviceData, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(new PropertyValueFactory<>("routeName"));
        routeCol.setPrefWidth(150);
        
        TableColumn<GPSDeviceData, Double> latCol = new TableColumn<>("Latitude");
        latCol.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        latCol.setPrefWidth(100);
        latCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.6f", item));
            }
        });
        
        TableColumn<GPSDeviceData, Double> lonCol = new TableColumn<>("Longitude");
        lonCol.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        lonCol.setPrefWidth(100);
        lonCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.6f", item));
            }
        });
        
        TableColumn<GPSDeviceData, Double> speedCol = new TableColumn<>("Speed (km/h)");
        speedCol.setCellValueFactory(new PropertyValueFactory<>("speedKmh"));
        speedCol.setPrefWidth(100);
        speedCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1f", item));
            }
        });
        
        TableColumn<GPSDeviceData, String> updateCol = new TableColumn<>("Last Update");
        updateCol.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));
        updateCol.setPrefWidth(120);
        
        TableColumn<GPSDeviceData, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "ONLINE" -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        case "DELAYED" -> setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        case "OFFLINE" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });
        
        deviceTable.getColumns().addAll(deviceIdCol, busNumberCol, routeCol, latCol, lonCol, speedCol, updateCol, statusCol);
        
        deviceTable.setRowFactory(tv -> {
            TableRow<GPSDeviceData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    GPSDeviceData selectedDevice = row.getItem();
                    trackSpecificBus(selectedDevice.getBusNumber());
                }
            });
            return row;
        });
    }
    
    private void loadBusData() {
        Platform.runLater(() -> {
            try {
                String query = "SELECT DISTINCT b.bus_number FROM Buses b WHERE b.gps_device_id IS NOT NULL AND b.operational_status = 'ACTIVE' AND b.gps_device_status = 'ACTIVE' ORDER BY b.bus_number";
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    ObservableList<String> busNumbers = FXCollections.observableArrayList();
                    while (rs.next()) {
                        busNumbers.add(rs.getString("bus_number"));
                    }
                    
                    busSearchCombo.setItems(busNumbers);
                    LOGGER.info("Loaded " + busNumbers.size() + " buses for search");
                }
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading bus data", e);
                showErrorMessage("Failed to load bus data: " + e.getMessage());
            }
        });
    }
    
    private void refreshGPSDeviceData() {
        try {
            // This query is for Microsoft SQL Server. Change if using a different DB.
            String query = """
                WITH LatestGPS AS (
                    SELECT 
                        gps_device_id, latitude, longitude, speed_kmh, gps_timestamp,
                        ROW_NUMBER() OVER (PARTITION BY gps_device_id ORDER BY gps_timestamp DESC) as rn
                    FROM GPS_Tracking
                    WHERE gps_timestamp >= DATEADD(HOUR, -1, GETDATE())
                ),
                ActiveAssignments AS (
                    SELECT DISTINCT
                        b.bus_id, b.bus_number, b.gps_device_id, ba.route_id, r.route_name
                    FROM Buses b
                    LEFT JOIN Bus_Assignments ba ON b.bus_id = ba.bus_id 
                        AND ba.assignment_status = 'ACTIVE'
                        AND (ba.end_date IS NULL OR ba.end_date > GETDATE())
                    LEFT JOIN Routes r ON ba.route_id = r.route_id
                    WHERE b.gps_device_id IS NOT NULL
                    AND b.operational_status = 'ACTIVE'
                    AND b.gps_device_status = 'ACTIVE'
                )
                SELECT 
                    aa.gps_device_id, aa.bus_number, aa.route_name,
                    lg.latitude, lg.longitude, lg.speed_kmh, lg.gps_timestamp,
                    CASE 
                        WHEN DATEDIFF(SECOND, lg.gps_timestamp, GETDATE()) <= 30 THEN 'ONLINE'
                        WHEN DATEDIFF(SECOND, lg.gps_timestamp, GETDATE()) <= 300 THEN 'DELAYED'
                        ELSE 'OFFLINE'
                    END AS status
                FROM ActiveAssignments aa
                LEFT JOIN LatestGPS lg ON aa.gps_device_id = lg.gps_device_id AND lg.rn = 1
                ORDER BY aa.bus_number
                """;
            
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                deviceData.clear();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                
                while (rs.next()) {
                    String lastUpdate = rs.getTimestamp("gps_timestamp") != null ? 
                        rs.getTimestamp("gps_timestamp").toLocalDateTime().format(formatter) : "No Data";
                    
                    GPSDeviceData device = new GPSDeviceData(
                        rs.getString("gps_device_id"),
                        rs.getString("bus_number"),
                        rs.getString("route_name"),
                        rs.getObject("latitude", Double.class),
                        rs.getObject("longitude", Double.class),
                        rs.getObject("speed_kmh", Double.class),
                        lastUpdate,
                        rs.getString("status")
                    );
                    
                    deviceData.add(device);
                }
                
                LOGGER.info("Refreshed GPS data: " + deviceData.size() + " unique devices");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error refreshing GPS data", e);
        }
    }
    
    private void searchAndTrackBus() {
        String busNumber = busSearchCombo.getValue();
        if (busNumber == null || busNumber.trim().isEmpty()) {
            showWarningAlert("Search Error", "Please select a bus number to track.");
            return;
        }
        
        trackSpecificBus(busNumber);
    }
    
    private void trackSpecificBus(String busNumber) {
        if (!mapInitialized) {
            statusLabel.setText("Map not ready yet...");
            return;
        }
        
        try {
            refreshGPSDeviceData();
            
            GPSDeviceData targetBus = deviceData.stream()
                .filter(device -> busNumber.equals(device.getBusNumber()))
                .findFirst()
                .orElse(null);
            
            if (targetBus == null || targetBus.getLatitude() == null || targetBus.getLongitude() == null) {
                showWarningAlert("Bus Not Found", "Bus " + busNumber + " not found or no GPS data available.");
                statusLabel.setText("Bus not found: " + busNumber);
                return;
            }
            
            webEngine.executeScript("clearMap()");
            displayedBuses.clear();
            
            webEngine.executeScript(String.format(Locale.US,
                "updateBusMarker('%s', '%s', %f, %f, %f, '%s', true)",
                targetBus.getGpsDeviceId(),
                targetBus.getBusNumber(),
                targetBus.getLatitude(),
                targetBus.getLongitude(),
                targetBus.getSpeedKmh() != null ? targetBus.getSpeedKmh() : 0.0,
                targetBus.getRouteName() != null ? targetBus.getRouteName() : "No route"
            ));
            
            if (targetBus.getRouteName() != null) {
                addRealisticRouteForBus(targetBus);
            }
            
            webEngine.executeScript(String.format(Locale.US,
                "focusBus(%f, %f, 15)", targetBus.getLatitude(), targetBus.getLongitude()
            ));
            
            currentTrackedBus = busNumber;
            displayedBuses.add(targetBus.getGpsDeviceId());
            statusLabel.setText("Tracking: " + busNumber + " (" + targetBus.getGpsDeviceId() + ")");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error tracking bus: " + busNumber, e);
            statusLabel.setText("Error tracking bus: " + busNumber);
        }
    }
    
    private void addRealisticRouteForBus(GPSDeviceData busData) {
        try {
            // This query is for Microsoft SQL Server
            String routeQuery = """
                SELECT r.route_id, r.route_name, r.start_location, r.end_location
                FROM Routes r
                INNER JOIN Bus_Assignments ba ON r.route_id = ba.route_id
                INNER JOIN Buses b ON ba.bus_id = b.bus_id
                WHERE b.gps_device_id = ? 
                AND ba.assignment_status = 'ACTIVE'
                AND (ba.end_date IS NULL OR ba.end_date > GETDATE())
                """;
            
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(routeQuery)) {
                
                stmt.setString(1, busData.getGpsDeviceId());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int routeId = rs.getInt("route_id");
                    String routeName = rs.getString("route_name");
                    String startLocation = rs.getString("start_location");
                    String endLocation = rs.getString("end_location");
                    
                    double[] startCoords = getLocationCoordinates(startLocation);
                    double[] endCoords = getLocationCoordinates(endLocation);
                    
                    if (startCoords != null && endCoords != null) {
                        webEngine.executeScript(String.format(Locale.US,
                            "addRealisticRoute('%d', %f, %f, %f, %f, '%s')",
                            routeId,
                            startCoords[0], startCoords[1],
                            endCoords[0], endCoords[1],
                            routeName
                        ));
                        
                        LOGGER.info("Added realistic route for: " + routeName);
                    }
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error adding route for bus: " + busData.getBusNumber(), e);
        }
    }
    
    private double[] getLocationCoordinates(String location) {
        if (location == null) return null;
        
        Map<String, double[]> locations = new HashMap<>();
        
        locations.put("Colombo Fort", new double[]{6.9319, 79.8478});
        locations.put("Colombo", new double[]{6.9271, 79.8612});
        locations.put("Pettah", new double[]{6.9395, 79.8587});
        locations.put("Kandy", new double[]{7.2906, 80.6337});
        locations.put("Negombo", new double[]{7.2083, 79.8358});
        locations.put("Galle", new double[]{6.0367, 80.2170});
        locations.put("Matara", new double[]{5.9549, 80.5550});
        locations.put("Kurunegala", new double[]{7.4863, 80.3647});
        locations.put("Anuradhapura", new double[]{8.3114, 80.4037});
        locations.put("Jaffna", new double[]{9.6615, 80.0255});
        
        String locationLower = location.toLowerCase();
        for (Map.Entry<String, double[]> entry : locations.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (locationLower.contains(key) || key.contains(locationLower)) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    private void showAllBuses() {
        if (!mapInitialized) {
            statusLabel.setText("Map not ready yet...");
            return;
        }
        
        try {
            refreshGPSDeviceData();
            
            webEngine.executeScript("clearMap()");
            displayedBuses.clear();
            
            int busesAdded = 0;
            for (GPSDeviceData device : deviceData) {
                if (device.getLatitude() != null && device.getLongitude() != null) {
                    webEngine.executeScript(String.format(Locale.US,
                        "updateBusMarker('%s', '%s', %f, %f, %f, '%s', false)",
                        device.getGpsDeviceId(),
                        device.getBusNumber() != null ? device.getBusNumber() : "Unknown",
                        device.getLatitude(),
                        device.getLongitude(),
                        device.getSpeedKmh() != null ? device.getSpeedKmh() : 0.0,
                        device.getRouteName() != null ? device.getRouteName() : "No route"
                    ));
                    
                    displayedBuses.add(device.getGpsDeviceId());
                    busesAdded++;
                }
            }
            
            if (busesAdded > 0) {
                webEngine.executeScript("showAllBuses()");
                currentTrackedBus = null;
                statusLabel.setText("Showing all buses (" + busesAdded + " active)");
            } else {
                statusLabel.setText("No GPS data available");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing all buses", e);
            statusLabel.setText("Error loading bus data");
        }
    }
    
    private void clearMap() {
        if (mapInitialized) {
            webEngine.executeScript("clearMap()");
            displayedBuses.clear();
            currentTrackedBus = null;
            statusLabel.setText("Map cleared - Select a bus to track");
        }
    }
    
    private void showGPSDevicesTable() {
        refreshGPSDeviceData();
        
        Stage tableStage = new Stage();
        tableStage.initModality(Modality.NONE);
        tableStage.setTitle("GPS Devices - Live Data (No Duplicates)");
        
        VBox tableContainer = new VBox(15);
        tableContainer.setPadding(new Insets(15));
        
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label headerLabel = new Label("📊 GPS Devices - Optimized Data");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statsLabel = new Label("Total: " + deviceData.size() + " devices");
        statsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        Button refreshTableButton = new Button("🔄 Refresh");
        refreshTableButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        refreshTableButton.setOnAction(e -> {
            refreshGPSDeviceData();
            statsLabel.setText("Total: " + deviceData.size() + " devices");
            statusLabel.setText("GPS data refreshed");
        });
        
        header.getChildren().addAll(headerLabel, spacer, statsLabel, refreshTableButton);
        
        Label instructions = new Label("💡 Double-click any row to track that bus on the map");
        instructions.setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d;");
        
        tableContainer.getChildren().addAll(header, instructions, deviceTable);
        
        Scene tableScene = new Scene(tableContainer, 1000, 500);
        tableStage.setScene(tableScene);
        tableStage.show();
    }
    
    private void refreshDisplayedBuses() {
        if (!mapInitialized || displayedBuses.isEmpty()) {
            return;
        }
        
        try {
            refreshGPSDeviceData();
            
            for (GPSDeviceData device : deviceData) {
                if (displayedBuses.contains(device.getGpsDeviceId()) && 
                    device.getLatitude() != null && device.getLongitude() != null) {
                    
                    boolean isSelected = device.getBusNumber().equals(currentTrackedBus);
                    
                    webEngine.executeScript(String.format(Locale.US,
                        "updateBusMarker('%s', '%s', %f, %f, %f, '%s', %s)",
                        device.getGpsDeviceId(),
                        device.getBusNumber() != null ? device.getBusNumber() : "Unknown",
                        device.getLatitude(),
                        device.getLongitude(),
                        device.getSpeedKmh() != null ? device.getSpeedKmh() : 0.0,
                        device.getRouteName() != null ? device.getRouteName() : "No route",
                        isSelected
                    ));
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error refreshing displayed buses", e);
        }
    }
    
    private void startAutoRefresh() {
        refreshScheduler = Executors.newSingleThreadScheduledExecutor();
        refreshScheduler.scheduleAtFixedRate(() -> {
            if (autoRefreshEnabled && mapInitialized) {
                Platform.runLater(this::refreshDisplayedBuses);
            }
        }, 5, 5, TimeUnit.SECONDS);
        
        LOGGER.info("Auto-refresh started (5-second interval)");
    }
    
    public void cleanup() {
        autoRefreshEnabled = false;
        if (refreshScheduler != null && !refreshScheduler.isShutdown()) {
            refreshScheduler.shutdown();
            try {
                if (!refreshScheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    refreshScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                refreshScheduler.shutdownNow();
            }
        }
        LOGGER.info("Optimized GPS Tracking Panel cleanup completed");
    }
    
    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("GPS Tracking Error");
            error.setHeaderText("System Error");
            error.setContentText(message);
            error.showAndWait();
        });
    }
    
    private void showWarningAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}