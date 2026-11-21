package org.sopt.sopkathon.domain.match.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "game_match")
public class GameMatch {

    private static final ZoneId MATCH_ZONE = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "game_type", length = 20)
    private GameType gameType;

    @Column(name = "team_size", nullable = false)
    private byte teamSize;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private MatchStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, secondPrecision = 6)
    private LocalDateTime createdAt;

    @Column(name = "starts_at", secondPrecision = 6)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", secondPrecision = 6)
    private LocalDateTime endsAt;

    @Column(name = "finished_at", secondPrecision = 6)
    private LocalDateTime finishedAt;

    public GameMatch(GameType gameType, byte teamSize) {
        this.gameType = gameType;
        this.teamSize = teamSize;
        this.status = MatchStatus.SEARCHING;
    }

    public void completePreparation() {
        if (status == MatchStatus.SEARCHING) {
            status = MatchStatus.FOUND;
        }
    }

    public boolean hasSelectedGameType() {
        return gameType != null && startsAt != null;
    }

    public void startGame(GameType gameType) {
        this.gameType = gameType;
        this.startsAt = LocalDateTime.now(MATCH_ZONE).truncatedTo(ChronoUnit.MICROS);
        this.status = MatchStatus.IN_PROGRESS;
    }

    public void finishGame() {
        if (status == MatchStatus.IN_PROGRESS) {
            this.finishedAt = LocalDateTime.now(MATCH_ZONE).truncatedTo(ChronoUnit.MICROS);
            this.status = MatchStatus.FINISHED;
        }
    }

    public OffsetDateTime getStartsAtWithOffset() {
        return startsAt == null ? null : startsAt.atZone(MATCH_ZONE).toOffsetDateTime();
    }
}
