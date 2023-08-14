package com.quarkus.chat.repository;

import com.quarkus.chat.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityNotFoundException;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findByUsername(String username) {
        return find("username", username).firstResultOptional()
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found"));
    }
}
