package com.quarkus.chat.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class User extends PanacheEntityBase {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_id_seq_gen")
    @SequenceGenerator(name = "app_user_id_seq_gen", sequenceName = "app_user_id_seq", allocationSize = 1)
    private Long userId;

    @Column(name = "username", unique = true)
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;

    public static void persistUser(User user) {
        persist(user);
    }

    public static User findByUsername(String username) {
        return (User) find("username", username).firstResultOptional()
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found"));
    }
}
