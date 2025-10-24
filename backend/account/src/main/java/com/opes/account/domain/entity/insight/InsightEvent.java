// com/opes/account/domain/entity/insight/InsightEvent.java
package com.opes.account.domain.entity.insight;

import com.fasterxml.jackson.databind.JsonNode;
import com.opes.account.domain.entity.AppUser;
import com.opes.account.domain.enums.InsightType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "insight_event",
        uniqueConstraints = @UniqueConstraint(name = "uk_insight_user_date", columnNames = {"user_id","date"})
)
public class InsightEvent {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InsightType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "json", nullable = false)
    private JsonNode payloadJson;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public InsightType getType() { return type; }
    public void setType(InsightType type) { this.type = type; }
    public JsonNode getPayloadJson() { return payloadJson; }
    public void setPayloadJson(JsonNode payloadJson) { this.payloadJson = payloadJson; }
}
