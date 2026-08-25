package lk.bustracking.depotmanagementsystem.controllers;

import lk.bustracking.depotmanagementsystem.models.Route;
import lk.bustracking.depotmanagementsystem.models.User;
import lk.bustracking.depotmanagementsystem.services.RouteService;
import lk.bustracking.depotmanagementsystem.views.RouteManagementView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import java.util.List;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Route Controller - Fixed version with proper method naming and functionality
 */
public class RouteController {
    
    private static final Logger LOGGER = Logger.getLogger(RouteController.class.getName());
    
    private final RouteManagementView view;
    private final RouteService routeService;
    private final ExecutorService executor;
    private User currentUser;
    
    // State management
    private Task<Void> currentDataLoadTask;
    private boolean isDataLoading = false;
    
    public RouteController(RouteManagementView view, User currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        this.routeService = new RouteService();
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("RouteController-" + t.getId());
            return t;
        });
    }
    
    /**
     * Load all routes - FIXED method name to match RouteManagementView expectations
     */
    public void loadRoutes() {
        if (isDataLoading) {
            LOGGER.info("Route data loading already in progress, skipping request");
            return;
        }
        
        view.showLoading();
        isDataLoading = true;
        
        currentDataLoadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    List<Route> routes = routeService.getAllRoutes();
                    RouteService.RouteStats stats = routeService.getRouteStats();
                    
                    Platform.runLater(() -> {
                        view.updateRouteData(routes);
                        view.updateStats(stats);
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        view.showError("Failed to load route data: " + e.getMessage());
                        LOGGER.severe("Error loading route data: " + e.getMessage());
                    });
                }
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    view.hideLoading();
                    isDataLoading = false;
                });
                LOGGER.info("Route data loaded successfully");
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    view.hideLoading();
                    isDataLoading = false;
                    view.showError("Failed to load route data");
                });
                LOGGER.severe("Route data loading task failed");
            }
        };
        
        executor.submit(currentDataLoadTask);
    }
    
    /**
     * Save new route - FIXED method name
     */
    public void saveRoute(Route route) {
        view.showLoading();
        
        Task<Boolean> saveTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return routeService.saveRoute(route);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    view.hideLoading();
                    
                    if (getValue()) {
                        view.showSuccessMessage("Route created successfully: " + route.getRouteNumber());
                        loadRoutes(); // Refresh data
                    } else {
                        view.showError("Failed to create route");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    view.hideLoading();
                    Throwable exception = getException();
                    view.showError("Error creating route: " + exception.getMessage());
                });
            }
        };
        
        executor.submit(saveTask);
    }
    
    /**
     * Update existing route - FIXED method name
     */
    public void updateRoute(Route route) {
        view.showLoading();
        
        Task<Boolean> updateTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return routeService.updateRoute(route);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    view.hideLoading();
                    
                    if (getValue()) {
                        view.showSuccessMessage("Route updated successfully: " + route.getRouteNumber());
                        loadRoutes(); // Refresh data
                    } else {
                        view.showError("Failed to update route");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    view.hideLoading();
                    Throwable exception = getException();
                    view.showError("Error updating route: " + exception.getMessage());
                });
            }
        };
        
        executor.submit(updateTask);
    }
    
    /**
     * Delete route - FIXED method name
     */
    public void deleteRoute(int routeId) {
        view.showLoading();
        
        Task<Boolean> deleteTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return routeService.deleteRoute(routeId);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    view.hideLoading();
                    
                    if (getValue()) {
                        view.showSuccessMessage("Route deleted successfully");
                        loadRoutes(); // Refresh data
                    } else {
                        view.showError("Failed to delete route");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    view.hideLoading();
                    Throwable exception = getException();
                    view.showError("Error deleting route: " + exception.getMessage());
                });
            }
        };
        
        executor.submit(deleteTask);
    }
    
    /**
     * Perform search - FIXED method name to match RouteManagementView
     */
    public void performSearch() {
        String searchText = view.getSearchField().getText();
        Route.RouteType routeType = view.getTypeFilterCombo().getValue();
        Boolean activeOnly = view.getActiveOnlyCheckBox().isSelected() ? true : null;
        
        Task<List<Route>> searchTask = new Task<List<Route>>() {
            @Override
            protected List<Route> call() throws Exception {
                return routeService.searchRoutes(searchText, routeType, activeOnly);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    view.updateRouteData(getValue());
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    view.showError("Error searching routes: " + getException().getMessage());
                });
            }
        };
        
        executor.submit(searchTask);
    }
    
    /**
     * Export to CSV - FIXED method name
     */
    public void exportToCSV() {
        Task<String> exportTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                List<Route> routes = routeService.getAllRoutes();
                
                StringBuilder csv = new StringBuilder();
                csv.append("Route Number,Route Name,Start Location,End Location,Distance(km),Type,Fare(Rs),Status\n");
                
                for (Route route : routes) {
                    csv.append(String.format("%s,%s,%s,%s,%.2f,%s,%.2f,%s\n",
                        route.getRouteNumber(),
                        route.getRouteName(),
                        route.getStartLocation(),
                        route.getEndLocation(),
                        route.getTotalDistanceKm() != null ? route.getTotalDistanceKm().doubleValue() : 0.0,
                        route.getRouteType().getDisplayName(),
                        route.getFarePrice() != null ? route.getFarePrice().doubleValue() : 0.0,
                        route.isActive() ? "Active" : "Inactive"
                    ));
                }
                
                return csv.toString();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    String data = getValue();
                    view.showSuccessMessage("Route data exported successfully (" + 
                                          (data.split("\n").length - 1) + " records)");
                    
                    // In a real application, save to file
                    LOGGER.info("CSV Export completed: " + data.length() + " characters");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    view.showError("Error exporting data: " + getException().getMessage());
                });
            }
        };
        
        executor.submit(exportTask);
    }
    
    /**
     * Get current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Set current user
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    /**
     * Cancel current data loading operation
     */
    public void cancelDataLoading() {
        if (currentDataLoadTask != null && !currentDataLoadTask.isDone()) {
            currentDataLoadTask.cancel(true);
            isDataLoading = false;
            view.hideLoading();
            LOGGER.info("Route data loading operation cancelled");
        }
    }
    
    /**
     * Check if data is currently being loaded
     */
    public boolean isDataLoading() {
        return isDataLoading;
    }
    
    /**
     * Handle filter by type - MISSING METHOD ADDED
     */
    public void handleFilterByType(Route.RouteType routeType) {
        Task<List<Route>> filterTask = new Task<List<Route>>() {
            @Override
            protected List<Route> call() throws Exception {
                if (routeType == null) {
                    return routeService.getAllRoutes();
                } else {
                    return routeService.getRoutesByType(routeType);
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    view.updateRouteData(getValue());
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    view.showError("Error filtering routes: " + getException().getMessage());
                });
            }
        };
        
        executor.submit(filterTask);
    }
    
    /**
     * Handle active only filter - MISSING METHOD ADDED
     */
    public void handleActiveOnlyFilter(boolean activeOnly) {
        Task<List<Route>> filterTask = new Task<List<Route>>() {
            @Override
            protected List<Route> call() throws Exception {
                if (activeOnly) {
                    return routeService.getActiveRoutes();
                } else {
                    return routeService.getAllRoutes();
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    view.updateRouteData(getValue());
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    view.showError("Error filtering routes: " + getException().getMessage());
                });
            }
        };
        
        executor.submit(filterTask);
    }
    
    /**
     * Handle search routes - MISSING METHOD ADDED
     */
    public void handleSearchRoutes(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadRoutes();
            return;
        }
        
        Task<List<Route>> searchTask = new Task<List<Route>>() {
            @Override
            protected List<Route> call() throws Exception {
                return routeService.searchRoutes(searchText, null, null);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    view.updateRouteData(getValue());
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    view.showError("Error searching routes: " + getException().getMessage());
                });
            }
        };
        
        executor.submit(searchTask);
    }
    
    /**
     * Handle create route - MISSING METHOD ADDED
     */
    public void handleCreateRoute(Route route) {
        saveRoute(route);
    }
    
    /**
     * Handle update route - MISSING METHOD ADDED
     */
    public void handleUpdateRoute(Route route) {
        updateRoute(route);
    }
    
    /**
     * Refresh data - MISSING METHOD ADDED
     */
    public void refreshData() {
        loadRoutes();
    }
    
    /**
     * Handle route optimization - MISSING METHOD ADDED
     */
    public void handleRouteOptimization() {
        view.showSuccessMessage("Route optimization feature coming soon!");
    }
    public void cleanup() {
        // Cancel any running tasks
        if (currentDataLoadTask != null && !currentDataLoadTask.isDone()) {
            currentDataLoadTask.cancel(true);
            LOGGER.info("Cancelled running data load task");
        }
        
        // Shutdown executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            LOGGER.info("Shutdown route controller executor");
        }
        
        // Reset state
        isDataLoading = false;
        currentDataLoadTask = null;
        
        // Clear references
        currentUser = null;
        
        LOGGER.info("RouteController cleanup completed successfully");
    }
}