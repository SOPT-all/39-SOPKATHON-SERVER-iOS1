package org.sopt.sopkathon.domain.match.entity;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "match_team",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_match_team_game_match_team_slot",
                columnNames = {"game_match_id", "team_slot"}
        ),
        check = @CheckConstraint(name = "ck_match_team_team_slot", constraint = "team_slot IN (1, 2)")
)
public class MatchTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_match_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_team_game_match"))
    private GameMatch gameMatch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_team_school_class"))
    private SchoolClass schoolClass;

    @Column(name = "team_slot", nullable = false)
    private byte teamSlot;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 10)
    private MatchResult result;

    @Column(name = "average_finish_time_milliseconds", precision = 12, scale = 3)
    private BigDecimal averageFinishTimeMilliseconds;

    @Column(name = "average_voice_score", precision = 7, scale = 3)
    private BigDecimal averageVoiceScore;

    @Column(name = "ranking_points")
    private Integer rankingPoints;

    public MatchTeam(GameMatch gameMatch, SchoolClass schoolClass, byte teamSlot) {
        this.gameMatch = gameMatch;
        this.schoolClass = schoolClass;
        this.teamSlot = teamSlot;
    }
}
