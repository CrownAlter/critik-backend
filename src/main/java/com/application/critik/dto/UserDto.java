package com.application.critik.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String bio;

    private Boolean isFollowing;
}
