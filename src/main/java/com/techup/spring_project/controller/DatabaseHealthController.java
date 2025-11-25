package com.techup.spring_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class DatabaseHealthController {

    private final DataSource dataSource;

    public DatabaseHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> checkDatabase() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // 5 second timeout
            
            if (isValid) {
                String url = connection.getMetaData().getURL();
                String databaseProduct = connection.getMetaData().getDatabaseProductName();
                String databaseVersion = connection.getMetaData().getDatabaseProductVersion();
                
                response.put("status", "connected");
                response.put("message", "Successfully connected to database");
                response.put("database", databaseProduct);
                response.put("version", databaseVersion);
                response.put("url", url.replaceAll("password=[^&]*", "password=***")); // Hide password
                
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Connection is not valid");
                return ResponseEntity.status(500).body(response);
            }
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Failed to connect to database");
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
}

