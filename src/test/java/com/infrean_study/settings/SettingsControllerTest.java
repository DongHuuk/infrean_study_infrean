package com.infrean_study.settings;

import com.infrean_study.WithAccount;
import com.infrean_study.account.AccountRepository;
import com.infrean_study.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}