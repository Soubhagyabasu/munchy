package com.munchy.gateway.users;

import java.util.Optional;

public interface UserAccountRepository {
    Optional<LocalUser> findByGoogleSubject(String googleSubject);
    Optional<LocalUser> findById(String id);
    LocalUser save(LocalUser user);
}
