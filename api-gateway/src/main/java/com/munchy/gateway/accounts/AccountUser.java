package com.munchy.gateway.accounts;

import java.util.List;

public record AccountUser(
        String id,
        String email,
        String name,
        String phoneNumber,
        String pictureUrl,
        List<String> roles
) {
}
