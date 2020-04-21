package com.infrean_study.study;

import com.infrean_study.account.CurrentUser;
import com.infrean_study.domain.Account;
import com.infrean_study.domain.Study;
import com.infrean_study.study.form.StudyForm;
import com.infrean_study.study.validator.StudyFormValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class StudyController {

    @Autowired
    private StudyService studyService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private StudyFormValidator studyFormValidator;
    @Autowired
    private StudyRepository studyRepository;

    @InitBinder("studyForm")
    public void validStudyForm(WebDataBinder webDataBinder){
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());

        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentUser Account account, @Valid StudyForm studyForm, Errors errors){
        if(errors.hasErrors()){
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), account);
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8); //한글 URL일수도 있으니 인코딩을 해주어야 한다.
    }

    @GetMapping("/study/{path}")
    public String voidStudy(@CurrentUser Account account, @PathVariable String path, Model model) {
        model.addAttribute(account);
        model.addAttribute(studyRepository.findByPath(path));

        return "study/view";
    }

}
