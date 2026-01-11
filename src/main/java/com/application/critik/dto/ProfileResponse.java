package com.application.critik.dto;

import com.application.critik.entities.Artwork;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User profile response containing user info and artworks")
public class ProfileResponse {
    @Schema(description = "User information")
    private UserDto user;
    
    @Schema(description = "List of artworks by the user")
    private List<Artwork> artworks;
    
    @Schema(description = "Whether the current user is following this user", example = "true")
    private Boolean isFollowing;
}
