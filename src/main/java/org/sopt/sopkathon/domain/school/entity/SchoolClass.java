package org.sopt.sopkathon.domain.school.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "school_class",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_school_class_school_grade_class_number",
                columnNames = {"school_id", "grade", "class_number"}
        )
)
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false, foreignKey = @ForeignKey(name = "fk_school_class_school"))
    private School school;

    @Column(nullable = false)
    private byte grade;

    @Column(name = "class_number", nullable = false)
    private short classNumber;

    public SchoolClass(School school, byte grade, short classNumber) {
        this.school = school;
        this.grade = grade;
        this.classNumber = classNumber;
    }
}
