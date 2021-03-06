package com.infrean_study.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrean_study.account.CurrentUser;
import com.infrean_study.account.Account;
import com.infrean_study.domain.Study;
import com.infrean_study.domain.Tag;
import com.infrean_study.domain.Zone;
import com.infrean_study.tag.TagForm;
import com.infrean_study.tag.TagRepository;
import com.infrean_study.zone.ZoneForm;
import com.infrean_study.zone.ZoneRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study/{path}/settings")
public class StudySettingsController {

    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyService studyService;
    @Autowired private ModelMapper modelMapper;
    @Autowired private TagRepository tagRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private StudyTagFormRepository studyTagFormRepository;
    @Autowired private ZoneRepository zoneRepository;

    @GetMapping("/description")
    public String descriptionForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(path, account); //path의 null check 메서드
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/description";
    }

    @PostMapping("/description")
    public String descriptionUpdate(@CurrentUser Account account, @Valid StudyDescriptionForm descriptionForm,
                                    Errors errors, @PathVariable String path, Model model, RedirectAttributes redirectAttributes){

        Study study = studyRepository.findByPath(path);
        if(!study.getManagers().contains(account)){
            model.addAttribute(account);
            model.addAttribute(descriptionForm);
            return "study/" + path + "/settings";
        }

        studyService.updateStudyDescription(study, descriptionForm);
        redirectAttributes.addFlashAttribute("message", "스터디 소개가 수정되었습니다.");
        return "redirect:/study/"+path;
    }

    @GetMapping("/banner")
    public String bannerForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(path, account);
        model.addAttribute(account);
        model.addAttribute(study);

        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String bannerUpdate(@CurrentUser Account account, @PathVariable String path, String image,
                               RedirectAttributes redirectAttributes, Model model) {
        Study study = studyService.getStudyToUpdate(path, account);
        String newImage = studyService.imgIfNonImg(image);

        if(newImage.equals("no")){
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/banner";
        }

        studyService.updateStudyImage(newImage, study);
        redirectAttributes.addFlashAttribute("message", "배너사진이 변경되었습니다.");
        return "redirect:/study/" + study.getEncodePath() + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableBanner(@CurrentUser Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(path, account);
        studyService.enableBanner(study);

        return "redirect:/study/" + study.getEncodePath() + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableBanner(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(path, account);
        studyService.disableBanner(study);

        return "redirect:/study/" + study.getEncodePath() + "/settings/banner";
    }

    @GetMapping("/tags")
    public String studyTagsForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("tags", study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> collect = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(collect));

        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity studyTagsAdd(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm) throws JsonProcessingException {
        String title = tagForm.getTagTitle();
        Tag tag = studyTagFormRepository.findByTitle(title);
        if(tag == null){
            tag = studyTagFormRepository.save(Tag.builder().title(title).build());
        }
        studyService.addStudyTag(path, tag, account);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity studyTagsRemove(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm) throws JsonProcessingException {
        String title = tagForm.getTagTitle();
        Tag tag = studyTagFormRepository.findByTitle(title);
        if(tag == null){
            return ResponseEntity.badRequest().build();
        }
        studyService.removeStudyTag(path, tag, account);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String studyZonesForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("zones", study.getZones().stream().map(Zone::toString).collect(Collectors.toList()));
        List<String> zonesList = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(zonesList));

        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity studyZonesAdd(@CurrentUser Account account, @PathVariable String path
                                        , @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }

        studyService.addStudyZone(path, zone, account);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity studyZonesRemove(@CurrentUser Account account, @PathVariable String path
            , @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }

        studyService.removeStudyZone(path, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String studyForm(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdateTFValues(path, account);
        //TODO 오픈 여부만 검색하도록 재구현
        model.addAttribute(account);
        model.addAttribute(study);

        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String studyPublish(@CurrentUser Account account, @PathVariable String path, Boolean publish,
                               RedirectAttributes attributes, Model model) {
        Study study = studyService.getStudyToUpdateTFValues(path, account);
        studyService.setPublish(study);
        model.addAttribute(account);
        model.addAttribute(study);

        attributes.addFlashAttribute("message", "스터디를 공개합니다.");
        return "redirect:/study/"+ study.getEncodePath() +"/settings/study";
    }

    @PostMapping("/study/unpublish")
    public String studyUnPublish(@CurrentUser Account account, @PathVariable String path, Boolean publish,
                                 RedirectAttributes attributes, Model model) {
        Study study = studyService.getStudyToUpdateTFValues(path, account);
        studyService.setClose(study);

        model.addAttribute(account);
        model.addAttribute(study);

        attributes.addFlashAttribute("message", "스터디를 종료합니다.");
        return "redirect:/study/"+ study.getEncodePath() +"/settings/study";
    }

    @PostMapping("/study/recruit/start")
    public String studyStartRecruit(@CurrentUser Account account, @PathVariable String path, Boolean recruit,
                                 RedirectAttributes attributes, Model model) {
        Study study = studyService.getStudyToUpdateTFValues(path, account);

        if(!study.canUpdateRecruiting()){
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다");
            return "redirect:/study/"+ study.getEncodePath() +"/settings/study";
        }
        studyService.setStartRecruit(study);
        model.addAttribute(account);
        model.addAttribute(study);
        //TODO 모집을 시작할 수 있는 시간은 전에 오픈한 시간에 + 1시간 이내여야만 가능하게끔 설정

        attributes.addFlashAttribute("message", "팀원 모집을 시작합니다.");
        return "redirect:/study/"+ study.getEncodePath() +"/settings/study";
    }

    @PostMapping("/study/recruit/stop")
    public String studyStopRecruit(@CurrentUser Account account, @PathVariable String path, Boolean recruit,
                               RedirectAttributes attributes, Model model) {
        Study study = studyService.getStudyToUpdateTFValues(path, account);
        if(!study.canUpdateRecruiting()){
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다");
            return "redirect:/study/"+ study.getEncodePath() +"/settings/study";
        }
        studyService.setStopRecruit(study);
        model.addAttribute(account);
        model.addAttribute(study);

        attributes.addFlashAttribute("message", "팀원 모집을 종료합니다.");
        return "redirect:/study/"+ study.getEncodePath() +"/settings/study";
    }

    @PostMapping("/study/path")
    public String studyUpdatePath(@CurrentUser Account account, @PathVariable String path, String newPath,
                                RedirectAttributes attributes, Model model) {
        Study study = studyService.getStudyToUpdatePath(account, path);

        if(!studyService.isValidPath(newPath)){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError", "해당 스터디 경로는 사용할 수 없습니다. 다른 값을 입력하세요.");
            return "study/settings/study";
        }

        studyService.updateStudyPath(study, newPath);
        attributes.addFlashAttribute("message", "경로가 변경되었습니다");
        return "redirect:/study/"+ study.getEncodePath() +"/settings/study";
    }

    @PostMapping("/study/title")
    public String studyUpdateTitle(@CurrentUser Account account, @PathVariable String path, String newTitle,
                                  RedirectAttributes attributes, Model model) {
        Study study = studyService.getStudyToUpdateTitle(account, path);

        if(!studyService.isValidPath(newTitle)){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyTitleError", "해당 스터디 이름은 사용할 수 없습니다. 다른 값을 입력하세요.");
            return "study/settings/study";
        }

        studyService.updateStudyTitle(study, newTitle);
        attributes.addFlashAttribute("message", "이름이 변경되었습니다");
        return "redirect:/study/"+ study.getEncodePath() +"/settings/study";
    }

    @PostMapping("/study/remove")
    public String studyDelete(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdateTFValues(path, account);
        studyService.studyDelete(study);

        return "redirect:/";
    }

}
