package com.infrean_study.event;

import com.infrean_study.WithAccount;
import com.infrean_study.account.AccountRepository;
import com.infrean_study.account.Account;
import com.infrean_study.domain.Event;
import com.infrean_study.domain.EventType;
import com.infrean_study.domain.Study;
import com.infrean_study.study.StudyFactory;
import com.infrean_study.study.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private StudyRepository studyRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private StudyFactory studyFactory;
    @Autowired private EventRepository eventRepository;
    @Autowired private EventService eventService;
    @Autowired private ModelMapper modelMapper;

    private Event createEvent(EventType eventType, int limit, String title, String description,
                              Study study, Account account){
        EventForm eventForm = EventForm.builder()
                .title(title)
                .description(description)
                .eventType(eventType)
                .endEnrollmentDateTime(LocalDateTime.now().plusHours(2))
                .startDateTime(LocalDateTime.now().plusDays(2))
                .endDateTime(LocalDateTime.now().plusDays(5))
                .limitOfEnrollments(limit)
                .build();
        return eventService.createEvent(modelMapper.map(eventForm, Event.class), study, account);
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("모임 생성 폼 - View")
    public void createEventView() throws Exception{
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-test");

        mockMvc.perform(get("/study/" + study.getPath() + "/new-event"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("eventForm"))
                .andExpect(view().name("event/form"));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("모임 생성 폼 - 성공")
    public void createEvent_Success() throws Exception{
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-test");

        mockMvc.perform(post("/study/" + study.getPath() + "/new-event")
                .param("title", "Title Test")
                .param("description", "모임 테스트 진행중")
                .param("eventType", String.valueOf(EventType.FCFS))
                .param("limitOfEnrollments", "3")
                .param("endDateTime", String.valueOf(LocalDateTime.now().plusDays(5)))
                .param("startDateTime", String.valueOf(LocalDateTime.now().plusDays(2)))
                .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now().plusHours(2)))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    Event event = eventService.createEvent(modelMapper.map(result, Event.class), study, account);
                    redirectedUrl(("/study/" + study.getEncodePath() + "/events/" + event.getId()));
                });
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("모임 생성 폼 - 실패")
    public void createEvent_fail() throws Exception{
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-test");

        mockMvc.perform(post("/study/" + study.getPath() + "/new-event")
                .param("title", "Title Test")
                .param("description", "모임 테스트 진행중")
                .param("eventType", String.valueOf(EventType.FCFS))
                .param("limitOfEnrollments", "3")
                .param("endDateTime", String.valueOf(LocalDateTime.now()))
                .param("startDateTime", String.valueOf(LocalDateTime.now()))
                .param("endEnrollmentDateTime", String.valueOf(LocalDateTime.now()))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("eventForm"))
                .andExpect(view().name("event/form"));
    }

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("모임 상세 화면")
    public void eventView() throws Exception{
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-test3");
        Event event = createEvent(EventType.FCFS, 3, "모임 테스트", "모임 테스트 진행중", study, account);


        mockMvc.perform(get("/study/" + study.getPath() + "/events/" + event.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/view"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }//error 발생 이유 - 인증된 사용자가 아니래 몰라

    @WithAccount("kuroneko2")
    @Test
    @DisplayName("스터디 화면에서의 모임 화면")
    public void eventStudyView() throws  Exception{
        Account account = accountRepository.findByNickname("kuroneko2");
        Study study = studyFactory.getStudy(account, "spring-test");
        Event event = createEvent(EventType.FCFS, 3, "모임 테스트", "모임 테스트 진행중", study, account);
        Event event2 = createEvent(EventType.FCFS, 5, "모임 테스트2", "모임 테스트 진행중", study, account);

        mockMvc.perform(get("/study/" + study.getPath() + "/events"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("newEvents"))
                .andExpect(model().attributeExists("oldEvents"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name("study/events"));
    }
}