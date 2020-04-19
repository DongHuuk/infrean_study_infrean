package com.infrean_study.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrean_study.WithAccount;
import com.infrean_study.account.AccountRepository;
import com.infrean_study.account.AccountService;
import com.infrean_study.domain.Account;
import com.infrean_study.domain.Tag;
import com.infrean_study.tag.TagForm;
import com.infrean_study.tag.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TagRepository tagRepository;
    @Autowired private AccountService accountService;

    @AfterEach
    public void deleteAccount(){
        accountRepository.deleteAll();
    }

//    @WithUserDetails("kuroneko2") 현재 error 진행형
    @WithAccount("kuroneko2")
    @Test
    @DisplayName("프로필 수정하기 - 입력값 정상")
    void profile_test_success() throws Exception{
        final String bioText = "소개를 수정했을 경우";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bioText)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists());

        final Account account = accountRepository.findByNickname("kuroneko2");
        assertEquals(account.getBio(), bioText);
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("프로필 수정하기 - 입력값 실패")
    void profile_test_error() throws Exception{
        final String bioText = "소개를 수정했을 경우소개를 수정했을 경우소개를 수정했을 경우소개를 수정했을 경우소개를 수정했을 경우소개를 수정했을 경우";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bioText)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        final Account account = accountRepository.findByNickname("kuroneko2");
        assertNull(account.getBio());
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("프로필 수정 폼")
    void profile_test_form() throws Exception{
        final String bioText = "소개를 수정했을 경우";
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("비밀번호 수정 폼")
    void password_update_form() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("비밀번호 수정 성공")
    void password_update_success() throws Exception{
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "qwertyui")
                .param("newPasswordConfirm", "qwertyui")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"))
                .andExpect(model().hasNoErrors());

        Account account = accountRepository.findByNickname("kuroneko2");
        assertTrue(passwordEncoder.matches("qwertyui", account.getPassword()));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("비밀번호 수정 실패 - front")
    void password_update_fail() throws Exception{
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "1111")
                .param("newPasswordConfirm", "1245")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("kuroneko2");
        assertFalse(passwordEncoder.matches("1111", account.getPassword()));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("비밀번호 수정 실패 - back")
    void password_update_fail_server() throws Exception{
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "qwertyuiop")
                .param("newPasswordConfirm", "poiuytrewq")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("kuroneko2");
        assertFalse(passwordEncoder.matches("qwertyuiop", account.getPassword()));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("닉네임 변경 성공")
    void nickname_update_success() throws Exception{
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "qwertyuiop")
                .param("newPasswordConfirm", "poiuytrewq")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("kuroneko2");
        assertFalse(passwordEncoder.matches("qwertyuiop", account.getPassword()));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("계정의 태그 수정 폼")
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("계정에 태그 add")
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        final Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("kuroneko2").getTags().contains(newTag));

    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("계정에 태그 remove")
    void removeTag() throws Exception {
        Account account = accountRepository.findByNickname("kuroneko2");
        Tag tag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account, tag);

        assertTrue(account.getTags().contains(tag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getTags().contains(tag));

    }

}