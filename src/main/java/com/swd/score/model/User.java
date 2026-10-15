package com.swd.score.model;

/** 登录用户模型：用户名 + SHA-256 口令摘要 + 角色（teacher/student）。 */
public class User {
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_STUDENT = "student";

    private int id;
    private String username;
    private String passwordHash;
    private String role;
    private String displayName;

    public User() {}

    public User(int id, String username, String passwordHash, String role, String displayName) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.displayName = displayName;
    }

    public boolean isTeacher() { return ROLE_TEACHER.equals(role); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
