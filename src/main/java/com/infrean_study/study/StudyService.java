package com.infrean_study.study;

import com.infrean_study.domain.Account;
import com.infrean_study.domain.Study;
import com.infrean_study.domain.Tag;
import com.infrean_study.domain.Zone;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class StudyService {

    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private StudyTagFormRepository studyTagFormRepository;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }//신규생성

    public Study getStudyToUpdate(String path, Account account) {
        Study study = this.getStudy(path);
        checkIfManager(study, account);
        return study;
    }//path를 이용해서 찾아오는 것

    private void checkIfManager(Study study, Account account) {
        if(!study.isManager(account)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다");
        }
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(study, path);
        return study;
    }

    public void checkIfExistingStudy(Study study, String path){
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm descriptionForm) {
        modelMapper.map(descriptionForm, study);
    }

    public void updateStudyImage(String image, Study study) {
        study.setImage(image);
    }

    public void enableBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableBanner(Study study) {
        study.setUseBanner(false);
    }

    public void addStudyTag(String title, Tag tag) {
        Study study = this.getStudy(title);
        study.getTags().add(tag);
    }

    public void removeStudyTag(String title, Tag tag) {
        Study study = this.getStudy(title);
        study.getTags().remove(tag);
    }

    public void addStudyZone(String path, Zone zone) {
        Study study = getStudy(path);
        study.getZones().add(zone);
    }

    public void removeStudyZone(String path, Zone zone) {
        Study study = getStudy(path);
        study.getZones().remove(zone);
    }

    public String imgIfNonImg(String image) {
        String str = image.substring(image.indexOf(".") + 1).toLowerCase();
        if(str.equals("jpg") || str.equals("png") || str.equals("bmp")){
            return image;
        }else
            return "no";

    }
}
