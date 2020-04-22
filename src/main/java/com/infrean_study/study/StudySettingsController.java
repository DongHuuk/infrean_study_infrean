package com.infrean_study.study;

import com.infrean_study.account.CurrentUser;
import com.infrean_study.domain.Account;
import com.infrean_study.domain.Study;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/study/{path}/settings")
public class StudySettingsController {

    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyService studyService;
    @Autowired private ModelMapper modelMapper;

    @GetMapping("/description")
    public String descriptionForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(path); //path의 null check 메서드
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/description";
    }


    @PostMapping("/description")
    public String changedDescription(@CurrentUser Account account, @Valid StudyDescriptionForm descriptionForm,
                                  Errors errors, @PathVariable String path, Model model){

        Study study = studyRepository.findByPath(path);
        if(!study.getManagers().contains(account)){
            model.addAttribute(account);
            model.addAttribute(descriptionForm);
            return "study/" + path + "/settings";
        }

        studyService.updateStudyDescription(study, descriptionForm);

        return "redirect:/study/"+path;
    }

}
