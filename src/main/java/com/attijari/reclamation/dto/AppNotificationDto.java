package com.attijari.reclamation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppNotificationDto {
    private String id;
    private String reclamationId;
    private String type;
    private String title;
    private String body;
    private Boolean read;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
