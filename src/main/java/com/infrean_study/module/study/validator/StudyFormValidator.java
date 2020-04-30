package com.infrean_study.study.validator;

import com.infrean_study.study.StudyRepository;
import com.infrean_study.study.form.StudyForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class StudyFormValidator implements Validator {

    @Autowired
    private StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return StudyForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm) target;
        if(studyRepository.existsByPath(studyForm.getPath())){
            errors.rejectValue("path", "wrong.path", "스터디 경로를 사용할 수 없습니다.");
        }
    }
}
