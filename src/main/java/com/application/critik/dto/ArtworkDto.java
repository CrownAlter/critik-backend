package com.application.critik.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtworkDto {
    private Long id;
    private UserDto user;
    private String title;
    private String imageUrl;
    private Double locationLat;
    private Double locationLon;
    private String locationName;
    private String interpretation;
    private List<String> tags;
    private LocalDateTime createdAt;
}
