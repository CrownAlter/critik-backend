package com.application.critik.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {
    private String displayName;
    private String email;
    private String bio;
}
