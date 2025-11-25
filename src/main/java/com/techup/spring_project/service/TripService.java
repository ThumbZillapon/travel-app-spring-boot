package com.techup.spring_project.service;

import com.techup.spring_project.dto.TripDto;
import com.techup.spring_project.entity.Trip;
import com.techup.spring_project.entity.User;
import com.techup.spring_project.exception.ForbiddenException;
import com.techup.spring_project.exception.ResourceNotFoundException;
import com.techup.spring_project.repository.TripRepository;
import com.techup.spring_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {
    
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    
    public List<TripDto> getAllTrips() {
        return tripRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public TripDto getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
        return convertToDto(trip);
    }
    
    public List<TripDto> getMyTrips(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return tripRepository.findByAuthorId(user.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<TripDto> searchTrips(String query) {
        return tripRepository.searchTrips(query).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TripDto createTrip(TripDto tripDto, String username) {
        // ✅ หา user จาก username (email)
        User author = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Trip trip = convertToEntity(tripDto);
        trip.setAuthor(author); // ✅ ตั้ง author เป็นคนที่ login
        
        Trip saved = tripRepository.save(trip);
        return convertToDto(saved);
    }
    
    @Transactional
    public TripDto updateTrip(Long id, TripDto tripDto, String username) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
        
        // ✅ ตรวจสอบว่าเป็นเจ้าของหรือไม่
        if (!trip.getAuthor().getEmail().equals(username)) {
            throw new ForbiddenException("You can only edit your own trips");
        }
        
        // ✅ Partial Update - อัปเดตเฉพาะฟิลด์ที่ส่งมา (ไม่ใช่ null)
        if (tripDto.getTitle() != null && !tripDto.getTitle().trim().isEmpty()) {
            trip.setTitle(tripDto.getTitle());
        }
        
        if (tripDto.getDescription() != null) {
            trip.setDescription(tripDto.getDescription());
        }
        
        if (tripDto.getPhotos() != null) {
            trip.setPhotos(tripDto.getPhotos());
        }
        
        if (tripDto.getTags() != null) {
            trip.setTags(tripDto.getTags());
        }
        
        if (tripDto.getLatitude() != null) {
            trip.setLatitude(tripDto.getLatitude());
        }
        
        if (tripDto.getLongitude() != null) {
            trip.setLongitude(tripDto.getLongitude());
        }
        
        Trip updated = tripRepository.save(trip);
        return convertToDto(updated);
    }
    
    @Transactional
    public void deleteTrip(Long id, String username) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
        
        // ✅ ตรวจสอบว่าเป็นเจ้าของหรือไม่
        if (!trip.getAuthor().getEmail().equals(username)) {
            throw new ForbiddenException("You can only delete your own trips");
        }
        
        tripRepository.deleteById(id);
    }
    
    private TripDto convertToDto(Trip trip) {
        TripDto dto = new TripDto();
        dto.setId(trip.getId());
        dto.setTitle(trip.getTitle());
        dto.setDescription(trip.getDescription());
        dto.setPhotos(trip.getPhotos());
        dto.setTags(trip.getTags());
        dto.setLatitude(trip.getLatitude());
        dto.setLongitude(trip.getLongitude());
        
        if (trip.getAuthor() != null) {
            dto.setAuthorId(trip.getAuthor().getId());
            dto.setAuthorDisplayName(trip.getAuthor().getDisplayName());
        }
        
        dto.setCreatedAt(trip.getCreatedAt());
        dto.setUpdatedAt(trip.getUpdatedAt());
        
        return dto;
    }
    
    private Trip convertToEntity(TripDto dto) {
        Trip trip = new Trip();
        trip.setTitle(dto.getTitle());
        trip.setDescription(dto.getDescription());
        trip.setPhotos(dto.getPhotos() != null ? dto.getPhotos() : new String[0]);
        trip.setTags(dto.getTags() != null ? dto.getTags() : new String[0]);
        trip.setLatitude(dto.getLatitude());
        trip.setLongitude(dto.getLongitude());
        
        if (dto.getAuthorId() != null) {
            User author = userRepository.findById(dto.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
            trip.setAuthor(author);
        }
        
        return trip;
    }
}
