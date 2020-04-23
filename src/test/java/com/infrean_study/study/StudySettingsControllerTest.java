package com.infrean_study.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrean_study.WithAccount;
import com.infrean_study.account.AccountRepository;
import com.infrean_study.domain.Account;
import com.infrean_study.domain.Study;
import com.infrean_study.domain.Tag;
import com.infrean_study.tag.TagForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.InitBinder;

import static com.infrean_study.domain.Study.DEFAULT_IMAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StudySettingsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;
    @Autowired private StudyFactory studyFactory;
    @Autowired private StudyRepository studyRepository;
    @Autowired private StudyService studyService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private StudyTagFormRepository studyTagFormRepository;

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("소개 수정 폼 - 성공")
    public void descriptionForm_success() throws Exception{
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-room");

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("studyDescriptionForm"));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("소개 수정 폼 - 실패(Non manager account)")
    public void descriptionForm_fail() throws Exception{
        Account account = accountRepository.findByNickname("kuroneko");
        Study study = studyFactory.getStudy(account, "spring-room");

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().is4xxClientError());
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("배너 수정 폼 - 성공")
    public void bannerUpdateForm_success() throws Exception {
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-room");

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/banner"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/banner"));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("배너 수정 폼 - 실패 (Non manager account)")
    public void bannerUpdateForm_fail() throws Exception {
        Account account = accountRepository.findByNickname("kuroneko");
        Study study = studyFactory.getStudy(account, "spring-room");

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/banner"))
                .andExpect(status().is4xxClientError());
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("배너 수정 - 성공")
    public void bannerUpdate_success() throws Exception {
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-room");

        mockMvc.perform(post("/study/" + study.getPath() + "/settings/banner")
                .param("path", study.getPath())
                .param("image", study.getImage())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists())
                .andExpect(redirectedUrl("/study/"+study.getPath()+"/settings/banner"));

        Study newStudy = studyRepository.findByPath(study.getPath());
        assertNotNull(newStudy);
        assertNotEquals(studyService.imgIfNonImg(newStudy.getImage()), "no");
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("배너 수정 - 실패(미확인 이미지 확장자 사용)")
    public void bannerUpdate_fail() throws Exception {
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-room");

        mockMvc.perform(post("/study/" + study.getPath() + "/settings/banner")
                .param("path", study.getPath())
                .param("image", "qwerty")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name("study/settings/banner"));

        Study newStudy = studyRepository.findByPath(study.getPath());
        assertNotNull(newStudy);
        assertEquals(newStudy.getImage(), DEFAULT_IMAGE);
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("배너 활성화 - 성공")
    public void bannerEnable() throws Exception {
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-room");

        mockMvc.perform(post("/study/" + study.getPath() + "/settings/banner/enable")
                .param("path", study.getPath())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodePath() + "/settings/banner"));

        Study newStudy = studyRepository.findByPath(study.getPath());
        assertNotNull(newStudy);
        assertTrue(newStudy.isUseBanner());
    }
    //실패는 아예 error 발생해버려서 Test 코드 작성 불가능

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("배너 비활성화 - 성공")
    public void bannerDisable() throws Exception {
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-room");

        mockMvc.perform(post("/study/" + study.getPath() + "/settings/banner/disable")
                .param("path", study.getPath())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodePath() + "/settings/banner"));

        Study newStudy = studyRepository.findByPath(study.getPath());
        assertNotNull(newStudy);
        assertFalse(newStudy.isUseBanner());
    }
    //실패는 아예 error 발생해버려서 Test 코드 작성 불가능

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("배너 태그 폼")
    public void studyTagForm() throws Exception{
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-room");

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(view().name("study/settings/tags"));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("배너 태그 추가 - 성공")
    public void addStudyTag_success() throws Exception{
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-room");
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/study/" + study.getPath() + "/settings/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag tag = studyTagFormRepository.findByTitle("newTag");
        assertNotNull(tag);
        assertTrue(studyRepository.findByPath(study.getPath()).getTags().contains(tag));
    }

    //TODO 배너 태그 추가 - 실패 + zones 테스트문 작성

}