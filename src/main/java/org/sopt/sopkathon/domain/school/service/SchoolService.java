package org.sopt.sopkathon.domain.school.service;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.domain.school.code.SchoolErrorCode;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;
import org.sopt.sopkathon.domain.school.repository.SchoolClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolService {

    private final SchoolClassRepository schoolClassRepository;

    public SchoolClass getSchoolClass(Long schoolId, byte grade, short classNumber) {
        return schoolClassRepository.findBySchoolIdAndGradeAndClassNumber(schoolId, grade, classNumber)
                .orElseThrow(() -> new BusinessException(SchoolErrorCode.SCHOOL_CLASS_NOT_FOUND));
    }
}
