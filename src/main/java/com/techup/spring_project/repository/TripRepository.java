package com.techup.spring_project.repository;

import com.techup.spring_project.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    List<Trip> findByAuthorId(Long authorId);
    
    @Query(value = "SELECT DISTINCT t.* FROM trips t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(t.tags) tag WHERE LOWER(tag) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY t.created_at DESC", 
           nativeQuery = true)
    List<Trip> searchTrips(@Param("query") String query);
    
    List<Trip> findAllByOrderByCreatedAtDesc();
}
