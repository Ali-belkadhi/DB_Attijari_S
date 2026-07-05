package com.attijari.reclamation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "USER_DEVICES",
        uniqueConstraints = @UniqueConstraint(name = "UK_USER_DEVICE_TOKEN", columnNames = "FCM_TOKEN")
)
@Getter
@Setter
@NoArgsConstructor
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_DEVICE", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID_USER", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private User user;

    @Column(name = "FCM_TOKEN", length = 512, nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String fcmToken;

    @Column(name = "DEVICE_TYPE", length = 30, nullable = false)
    private String deviceType;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = true;

    @Column(name = "LAST_ACTIVE", nullable = false)
    private LocalDateTime lastActive;

    @PrePersist
    @PreUpdate
    public void initializeDefaults() {
        if (active == null) active = true;
        lastActive = LocalDateTime.now();
    }
}
