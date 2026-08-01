package com.munchy.gateway.users;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserAccountRepository implements UserAccountRepository {
    private final ConcurrentHashMap<String, LocalUser> usersById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> idByGoogleSubject = new ConcurrentHashMap<>();

    @Override
    public Optional<LocalUser> findByGoogleSubject(String googleSubject) {
        return Optional.ofNullable(idByGoogleSubject.get(googleSubject)).flatMap(this::findById);
    }

    @Override
    public Optional<LocalUser> findById(String id) {
        return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public LocalUser save(LocalUser user) {
        usersById.put(user.id(), user);
        idByGoogleSubject.put(user.googleSubject(), user.id());
        return user;
    }
}
