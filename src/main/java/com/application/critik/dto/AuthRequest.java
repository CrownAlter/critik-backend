package com.application.critik.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Authentication request containing login credentials")
public class AuthRequest {
    @Schema(description = "Username", example = "johndoe", required = true)
    private String username;
    
    @Schema(description = "Password", example = "SecurePass123!", required = true)
    private String password;
}
