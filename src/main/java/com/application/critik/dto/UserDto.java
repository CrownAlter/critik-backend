package com.application.critik.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User data transfer object")
public class UserDto {
    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "Username", example = "johndoe")
    private String username;
    
    @Schema(description = "Display name", example = "John Doe")
    private String displayName;
    
    @Schema(description = "Email address", example = "john@example.com")
    private String email;
    
    @Schema(description = "User biography", example = "Art enthusiast and photographer")
    private String bio;

    @Schema(description = "Whether the current user is following this user", example = "true")
    private Boolean isFollowing;
}
