package com.infrean_study.study;

import com.infrean_study.domain.Account;
import com.infrean_study.domain.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyFactory {

    @Autowired
    private StudyService studyService;


    public Study getStudy(Account account, String path) {
        Study study = new Study();
        study.setPath(path);
        return studyService.createNewStudy(study, account);
    }
}
