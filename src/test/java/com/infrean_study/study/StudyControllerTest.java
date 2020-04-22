package com.infrean_study.study;

import com.infrean_study.WithAccount;
import com.infrean_study.account.AccountRepository;
import com.infrean_study.domain.Account;
import com.infrean_study.domain.Study;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StudyControllerTest {

    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MockMvc mockMvc;

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("스터디 생성 폼")
    public void newStudyForm() throws Exception{
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));

    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("스터디 생성 - 성공")
    public void addStudy_clear() throws Exception{
        mockMvc.perform(post("/new-study")
                .param("path", "spring-boot")
                .param("title", "Spring Boot")
                .param("shortDescription", "Spring Boot Test")
                .param("fullDescription", "Spring Boot Test fullDescription")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/spring-boot"));

        Study study = studyRepository.findByPath("spring-boot");
        assertNotNull(study);
        Account account = accountRepository.findByNickname("kuroneko2");
        assertTrue(study.getManagers().contains(account));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("스터디 생성 - 실패")
    public void addStudy_fail() throws Exception{
        mockMvc.perform(post("/new-study")
                .param("path", "Spring-boot")
                .param("title", "Spring Boot")
                .param("shortDescription", "Spring Boot Test")
                .param("fullDescription", "Spring Boot Test fullDescription")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors());

        Study study = studyRepository.findByPath("spring-boot");
        assertNull(study);
    }
}