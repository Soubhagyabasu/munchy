package com.munchy.account.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleLoginRequest {
    @NotBlank
    private String googleSubject;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    private String pictureUrl;
    private boolean emailVerified;
    private String ipAddress;
    private String userAgent;
}
