package com.axconstantino.auth.domain.model;

import java.util.*;

public class User {

    private UUID id;
    private String name;
    private String email;
    private String password;
    private final Set<Role> roles;
    private boolean active;
    private boolean emailVerified;
    private final Set<Token> tokens;

    public static User register(String name, String email, String password, Set<Role> roles) {
        return new User(UUID.randomUUID(), name, email, password, roles != null ? roles : new HashSet<>(), true, false, new HashSet<>());
    }

    public User(UUID id, String name, String email, String password, Set<Role> roles,
                boolean active, boolean emailVerified, Set<Token> tokens) {
        this.id = id;
        this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password);
        this.roles = new HashSet<>(Objects.requireNonNull(roles));
        this.active = active;
        this.emailVerified = emailVerified;
        this.tokens = new HashSet<>(Objects.requireNonNull(tokens));
    }

    public UUID getId() { return id;}
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public boolean isActive() { return active; }
    public boolean isEmailVerified() { return emailVerified; }
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

    public void changeEmail(String newEmail) {
        this.email = Objects.requireNonNull(newEmail);
        this.emailVerified = false;
    }

    public void changeName(String newName) {
        this.name = Objects.requireNonNull(newName);
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
