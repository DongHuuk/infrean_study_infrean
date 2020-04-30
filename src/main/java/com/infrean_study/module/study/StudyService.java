package com.infrean_study.study;

import com.infrean_study.account.Account;
import com.infrean_study.domain.Study;
import com.infrean_study.domain.Tag;
import com.infrean_study.domain.Zone;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Study getStudyToUpdateTFValues(String path, Account account) {
        Study study = studyRepository.findStudyWithStatusByPath(path);
        checkIfManager(study, account);
        return study;
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(study, path);
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm descriptionForm) {
        modelMapper.map(descriptionForm, study);
    }

    public void updateStudyImage(String image, Study study) {
        study.setImage(image);
    }


    public void addStudyTag(String path, Tag tag, Account account) {
        Study study = getStudyToUpdateTag(account, path);
        study.getTags().add(tag);
    }

    public void removeStudyTag(String path, Tag tag, Account account) {
        Study study = getStudyToUpdateTag(account, path);
        study.getTags().remove(tag);
    }

    public void addStudyZone(String path, Zone zone, Account account) {
        Study study = getStudyToUpdateZone(account, path);
        study.getZones().add(zone);
    }

    public void removeStudyZone(String path, Zone zone) {
        Study study = getStudy(path);
        study.getZones().remove(zone);
    }

    private Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path);
        checkIfManager(study, account);
        return study;
    }

    private Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        checkIfManager(study, account);
        return study;
    }

    public void enableBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableBanner(Study study) {
        study.setUseBanner(false);
    }

    public String imgIfNonImg(String image) {
        String str = image.substring(image.indexOf(".") + 1).toLowerCase();
        if(str.equals("jpg") || str.equals("png") || str.equals("bmp")){
            return image;
        }else
            return "no";
    }

    private void checkIfManager(Study study, Account account) {
        if(!study.isManager(account)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다");
        }
    }

    public void checkIfExistingStudy(Study study, String path){
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void setPublish(Study study) {
        study.publish();
    }

    public void setClose(Study study) {
        study.close();
    }

    public void setStartRecruit(Study study) {
        study.startRecruiting();
    }

    public void setStopRecruit(Study study) {
        study.stopRecruiting();
    }

    public Study getStudyToUpdatePath(Account account, String path) {
        Study study = studyRepository.findStudyWithStudypathByPath(path);
        checkIfExistingStudy(study, path);
        checkIfManager(study, account);

        return study;
    }

    public boolean isValidPath(String newPath) {
        if(newPath.length() > 50){
            return false;
        }

        return !studyRepository.existsByPath(newPath);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }

    public Study getStudyToUpdateTitle(Account account, String path) {
        Study study = studyRepository.findStudyWithStudytitleByPath(path);
        checkIfExistingStudy(study, path);
        checkIfManager(study, account);

        return study;
    }

    public void studyDelete(Study study) {
        if(study.isRemovable()){
            studyRepository.delete(study);
        }else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다");
        }
    }

    public void setMembers(Account account, Study study) {
        if(!study.getMembers().contains(account) && !study.getManagers().contains(account)){
            study.getMembers().add(account);
        }else {
            throw new IllegalArgumentException(account.getNickname() + "님은 스터디에 가입하실 수 없습니다.");
        }
    }

    public void removeMembers(Account account, Study study) {
        if(study.getMembers().contains(account)){
            study.getMembers().remove(account);
        }else {
            throw new IllegalArgumentException(account.getNickname() + "님은 스터디를 탈퇴하실 수 없습니다.");
        }
    }

    public Study getStudyToEnroll(String path) {
        Study study = studyRepository.findStudyOnlyByPath(path);
        checkIfExistingStudy(study, path);
        return study;
    }
}
