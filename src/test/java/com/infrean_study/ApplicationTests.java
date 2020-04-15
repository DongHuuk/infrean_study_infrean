package com.infrean_study;

import com.infrean_study.account.AccountRepository;
import com.infrean_study.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @Test
    @DisplayName("회원 가입 페이지 확인")
    void signForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpInputCheck() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "kuroneko")
                .param("email", "kuroneko2@naver.com")
                .param("password", "1234567890")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("kuroneko"));


        final Account account = accountRepository.findByEmail("kuroneko2@naver.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "1234567890");
        assertNotNull(account.getEmailCheckToken());

        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpInputCheck_error() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "kuroneko2")
                .param("email", "kuroneko2.com")
                .param("password", "123456")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());

        assertFalse(accountRepository.existsByEmail("kuroneko2@naver.com"));
    }

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "fdsafdsa")
                .param("email", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checkd-Email"))
                .andExpect(unauthenticated());

    }

    @DisplayName("인증 메일 확인 - 입력값 성공")
    @Test
    void checkEMailToken() throws Exception {
        Account account = Account.builder()
                .email("test@email.com")
                .password("123456789")
                .nickname("kuroneko")
                .build();
        final Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfuser"))
                .andExpect(view().name("account/checkd-Email"))
                .andExpect(authenticated());

    }

    @Test
    void gettest() throws Exception {
        mockMvc.perform(get("/recheck-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

}
