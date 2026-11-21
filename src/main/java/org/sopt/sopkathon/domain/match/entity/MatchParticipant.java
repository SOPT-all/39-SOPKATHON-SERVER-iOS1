package org.sopt.sopkathon.domain.match.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.sopt.sopkathon.domain.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "match_participant",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_match_participant_match_team_member",
                columnNames = {"match_team_id", "member_id"}
        )
)
public class MatchParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_participant_match_team"))
    private MatchTeam matchTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_participant_member"))
    private Member member;

    @Column(name = "is_ready", nullable = false)
    private boolean isReady;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "record_status", nullable = false, length = 16)
    private RecordStatus recordStatus;

    @Column(name = "finish_time_milliseconds")
    private Long finishTimeMilliseconds;

    @Column(name = "voice_score", precision = 7, scale = 3)
    private BigDecimal voiceScore;

    @Column(name = "submitted_at", secondPrecision = 6)
    private LocalDateTime submittedAt;

    public MatchParticipant(MatchTeam matchTeam, Member member) {
        this.matchTeam = matchTeam;
        this.member = member;
        this.recordStatus = RecordStatus.PENDING;
    }

    public void submitVoiceScore(BigDecimal voiceScore, LocalDateTime submittedAt) {
        this.voiceScore = voiceScore.setScale(3);
        this.recordStatus = RecordStatus.SUBMITTED;
        this.submittedAt = submittedAt;
    }
}
