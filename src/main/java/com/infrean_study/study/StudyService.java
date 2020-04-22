package com.infrean_study.study;

import com.infrean_study.domain.Account;
import com.infrean_study.domain.Study;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudyService {

    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private ModelMapper modelMapper;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);

        return newStudy;
    }//신규생성

    public Study getStudyToUpdate(String path) {
        Study study = studyRepository.findByPath(path);
        checkStudy(study, path);
        return study;
    }//path를 이용해서 찾아오는 것

    public void checkStudy(Study study, String path){
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm descriptionForm) {
        modelMapper.map(descriptionForm, study);
    }
}
