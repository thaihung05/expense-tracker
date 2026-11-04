package com.kei.backend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    /** Chi luu ban da ma hoa (BCrypt), khong bao gio luu mat khau goc. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Thoi diem va phien ban Chinh sach quyen rieng tu ma nguoi dung da dong y. */
    @Column(name = "privacy_accepted_at")
    private Instant privacyAcceptedAt;

    @Column(name = "privacy_version", length = 20)
    private String privacyVersion;

    protected User() {
    }

    public User(String email, String passwordHash, String displayName, String privacyVersion) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.createdAt = Instant.now();
        this.privacyAcceptedAt = this.createdAt;
        this.privacyVersion = privacyVersion;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPrivacyAcceptedAt() { return privacyAcceptedAt; }
    public String getPrivacyVersion() { return privacyVersion; }
}
