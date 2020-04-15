package com.infrean_study.main;

import com.infrean_study.account.AccountRepository;
import com.infrean_study.account.AccountService;
import com.infrean_study.account.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;


    @BeforeEach
    public void createAccount(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("kuroneko2");
        signUpForm.setEmail("kuroneko2@naver.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    public void deleteAccount(){
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("이메일 로그인 확인")
    void login_with_email() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "kuroneko2@naver.com")
                .param("password", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("kuroneko2"));
    }

    @Test
    @DisplayName("닉네임 로그인 확인")
    void login_with_nickname() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "kuroneko2")
                .param("password", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("kuroneko2"));
    }

    @Test
    @DisplayName("로그인 실패")
    void login_fail() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "11111")
                .param("password", "222222222222")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }


    @Test
//    @WithMockUser username - user, password - password 인 유저를 추가해서 테스트 할 수 있다.
    @DisplayName("로그아웃")
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

}