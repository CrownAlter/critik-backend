package com.application.critik.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request to update user profile information")
public class UserUpdateRequest {
    @Schema(description = "Display name", example = "John Doe")
    private String displayName;
    
    @Schema(description = "Email address", example = "john@example.com")
    private String email;
    
    @Schema(description = "User biography", example = "Art enthusiast and photographer")
    private String bio;
}
