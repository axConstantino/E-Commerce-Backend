package com.axconstantino.auth.domain.model;

import java.time.Instant;
import java.util.*;

public class User {

    private UUID id;
    private String userName;
    private String email;
    private String password;
    private final Set<Role> roles;
    private boolean active;
    private boolean emailVerified;
    private Instant deletedAt;
    private final Set<Token> tokens;

    public static User register(String userName, String email, String password, Set<Role> roles) {
        return new User(UUID.randomUUID(), userName, email, password, roles != null ? roles : new HashSet<>(), true, false, null, new HashSet<>());
    }

    public User(UUID id, String name, String email, String password, Set<Role> roles,
                boolean active, boolean emailVerified, Instant deletedAt, Set<Token> tokens) {
        this.id = id;
        this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password);
        this.roles = new HashSet<>(Objects.requireNonNull(roles));
        this.active = active;
        this.emailVerified = emailVerified;
        this.deletedAt = deletedAt;
        this.tokens = new HashSet<>(Objects.requireNonNull(tokens));
    }

    public UUID getId() { return id;}
    public String getUserName() { return userName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public boolean isActive() { return active; }
    public boolean isEmailVerified() { return emailVerified; }
    public Instant getDeletedAt() { return deletedAt; }
    public Set<Token> getTokens() { return Collections.unmodifiableSet(tokens); }

    public boolean isAdmin() {
        return roles.contains(Role.ROLE_ADMIN);
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void changeEmail(String newEmail) {
        this.email = Objects.requireNonNull(newEmail);
        this.emailVerified = false;
    }

    public void changeName(String newName) {
        this.userName = Objects.requireNonNull(newName);
    }

    public void changePassword(String newPassword) {
        this.password = Objects.requireNonNull(newPassword);
    }

    public void assignRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role roleName) {
        roles.remove(roleName);
    }

    public void revokeAllTokens() {
        tokens.forEach(Token::revoke);
    }

    public void addToken(Token token) {
        tokens.add(token);
    }

    public void removeToken(String tokenValue) {
        tokens.removeIf(token -> token.getToken().equals(tokenValue));
    }
}
