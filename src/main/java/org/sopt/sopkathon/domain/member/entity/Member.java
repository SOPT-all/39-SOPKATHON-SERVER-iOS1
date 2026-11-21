package org.sopt.sopkathon.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_member_school_class"))
    private SchoolClass schoolClass;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "last_seen_at", secondPrecision = 6)
    private LocalDateTime lastSeenAt;

    public Member(SchoolClass schoolClass, String displayName) {
        this.schoolClass = schoolClass;
        this.displayName = displayName;
    }
}
