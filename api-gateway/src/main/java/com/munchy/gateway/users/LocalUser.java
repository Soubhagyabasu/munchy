package com.munchy.gateway.users;

import java.util.List;

public record LocalUser(
        String id,
        String googleSubject,
        String email,
        String name,
        String picture,
        List<String> roles
) {}
