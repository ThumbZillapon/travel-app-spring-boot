package com.techup.spring_project.controller;

import com.techup.spring_project.dto.TripDto;
import com.techup.spring_project.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    
    private final TripService tripService;
    
    @GetMapping
    public ResponseEntity<List<TripDto>> getAllTrips(
            @RequestParam(required = false) String query) {
        if (query != null && !query.trim().isEmpty()) {
            return ResponseEntity.ok(tripService.searchTrips(query.trim()));
        }
        return ResponseEntity.ok(tripService.getAllTrips());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TripDto> getTripById(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }
    
    @GetMapping("/mine")
    public ResponseEntity<List<TripDto>> getMyTrips(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(tripService.getMyTrips(username));
    }
    
    @PostMapping
    public ResponseEntity<TripDto> createTrip(
            @Valid @RequestBody TripDto tripDto,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(tripService.createTrip(tripDto, username));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TripDto> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripDto tripDto,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(tripService.updateTrip(id, tripDto, username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        tripService.deleteTrip(id, username);
        return ResponseEntity.noContent().build();
    }
}
