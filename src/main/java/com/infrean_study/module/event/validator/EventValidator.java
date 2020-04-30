package com.infrean_study.event.validator;

import com.infrean_study.domain.Event;
import com.infrean_study.event.EventForm;
import com.infrean_study.event.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;

        if (isEnrollmentValid(eventForm)) {
            errors.rejectValue("endEnrollmentDateTime", "wrong.datetime", "모임 접수 종료 일시를 정확히 입력하세요.");
        }

        if(isNotValidEndDateTime(eventForm)){
            errors.rejectValue("endDateTime", "wrong.datetime", "모임 종료 일시를 정확히 입력하세요.");
        }

        if(isNotValidStartDateTime(eventForm)){
            errors.rejectValue("startDateTime", "wrong.datetime", "모임 시작 일시를 정확히 입력하세요.");
        }
    }

    private boolean isNotValidStartDateTime(EventForm eventForm) {
        return eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    private boolean isEnrollmentValid(EventForm eventForm) {
        return eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    private boolean isNotValidEndDateTime(EventForm eventForm){
        return eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime()) ||
                eventForm.getEndDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    public void validateUpdateFomr(EventForm eventForm, Event event, Errors errors) {
        if(event.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()){
            errors.rejectValue("limitOfEnrollments", "wrong.value", "모집 인원은 기존보다 적을 수 없습니다.");
        }
    }
}
