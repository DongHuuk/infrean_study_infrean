package com.infrean_study.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrean_study.account.AccountService;
import com.infrean_study.account.CurrentUser;
import com.infrean_study.domain.Account;
import com.infrean_study.domain.Tag;
import com.infrean_study.domain.Zone;
import com.infrean_study.settings.form.NicknameForm;
import com.infrean_study.settings.form.NotificationsForm;
import com.infrean_study.settings.form.PasswordForm;
import com.infrean_study.settings.form.Profile;
import com.infrean_study.settings.validator.NicknameValidator;
import com.infrean_study.settings.validator.PasswordFormValidator;
import com.infrean_study.tag.TagForm;
import com.infrean_study.tag.TagRepository;
import com.infrean_study.zone.ZoneForm;
import com.infrean_study.zone.ZoneRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.infrean_study.settings.SettingsController.ROOT;
import static com.infrean_study.settings.SettingsController.SETTINGS;

@Controller
@RequestMapping(ROOT + SETTINGS)
public class SettingsController {
    static final String ROOT = "/";
    static final String SETTINGS = "settings";
    static final String PROFILE = "/profile";
    static final String PASSWORD = "/password";
    static final String NOTIFICATION = "/notifications";
    static final String ACCOUNT = "/account";
    static final String TAGS = "/tags";
    static final String ZONES = "/zones";

    @Autowired private AccountService accountService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ModelMapper modelMapper;
    @Autowired private NicknameValidator nicknameValidator;
    @Autowired private TagRepository tagRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ZoneRepository zoneRepository;


    @InitBinder("passwordForm")
    public void initBinder_password(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void initBinder_nickname(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping(PROFILE)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));

        return SETTINGS + PROFILE;
    }

    @PostMapping(PROFILE)
    public String profileUpdate(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PROFILE;
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/" + SETTINGS + PROFILE;
    }

    @GetMapping(PASSWORD)
    public String passwordUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return SETTINGS + PASSWORD;
    }

    @PostMapping(PASSWORD)
    public String passwordUpdate(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors
            , Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PASSWORD;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다");

        return "redirect:/" + SETTINGS + PASSWORD;
    }

    @GetMapping(NOTIFICATION)
    public String notificationUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NotificationsForm.class));

        return SETTINGS + NOTIFICATION;
    }

    @PostMapping(NOTIFICATION)
    public String notificationUpdate(@CurrentUser Account account, @Valid NotificationsForm notificationsForm, Errors errors,
                                     RedirectAttributes redirectAttributes, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + NOTIFICATION;
        }

        accountService.setNotification(account, notificationsForm);
        redirectAttributes.addFlashAttribute("message", "알림 적용 완료");
        return "redirect:/" + SETTINGS + NOTIFICATION;
    }

    @GetMapping(ACCOUNT)
    public String nicknameUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));

        return SETTINGS + ACCOUNT;
    }

    @PostMapping(ACCOUNT)
    public String nicknameUpdate(@CurrentUser Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                 Model model, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + ACCOUNT;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        redirectAttributes.addFlashAttribute("message", "닉네임이 변경되었습니다");
        return "redirect:/" + SETTINGS + ACCOUNT;
    }

    @GetMapping(TAGS)
    public String updateTagsForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        final Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        final List<String> collect = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(collect));

        return SETTINGS + TAGS;
    }

    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity addTags(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
//        Tag tag = tagRepository.findByTitle(title).orElseGet(tagRepository.save(Tag.builder()
//                .title(tagForm.getTagTitle())
//                .build())); used Optional<?>

        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(title).build());
        }

        accountService.addTag(account, tag);

        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTags(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if(tag == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String updateZoneForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        final Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        final List<String> zoneList = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(zoneList));

        return SETTINGS + ZONES;
    }

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addZones(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }//정해진 whitelist만 사용하도록

        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeZones(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCity(), zoneForm.getProvince());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }//정해진 whitelist만 사용하도록

        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }

}
