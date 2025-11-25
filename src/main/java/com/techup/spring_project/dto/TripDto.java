package com.techup.spring_project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripDto {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    // ไม่ต้อง @NotNull เพื่อให้รองรับ Partial Update
    private String[] photos;
    
    private String[] tags;
    
    private Double latitude;
    
    private Double longitude;
    
    private Long authorId;
    
    private String authorDisplayName;
    
    private OffsetDateTime createdAt;
    
    private OffsetDateTime updatedAt;
}

