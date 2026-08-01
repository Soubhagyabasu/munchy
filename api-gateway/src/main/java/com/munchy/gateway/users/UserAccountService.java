package com.munchy.gateway.users;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserAccountService {
    private final UserAccountRepository repository;

    public UserAccountService(UserAccountRepository repository) {
        this.repository = repository;
    }

    public LocalUser findOrCreate(OAuth2User principal) {
        String subject = required(principal, "sub");
        String email = required(principal, "email");
        String name = required(principal, "name");
        String picture = principal.getAttribute("picture");

        return repository.findByGoogleSubject(subject).orElseGet(() -> repository.save(
                new LocalUser(UUID.randomUUID().toString(), subject, email, name, picture, List.of("ROLE_CUSTOMER"))
        ));
    }

    public LocalUser requireById(String id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Unknown Munchy user"));
    }

    private String required(OAuth2User principal, String claim) {
        String value = principal.getAttribute(claim);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Google principal is missing required claim: " + claim);
        }
        return value;
    }
}
