package com.infrean_study.event;

import com.infrean_study.account.CurrentUser;
import com.infrean_study.account.Account;
import com.infrean_study.domain.Enrollment;
import com.infrean_study.domain.Event;
import com.infrean_study.domain.Study;
import com.infrean_study.event.validator.EventValidator;
import com.infrean_study.study.StudyRepository;
import com.infrean_study.study.StudyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study/{path}")
public class EventController {

    @Autowired private StudyService studyService;
    @Autowired private EventService eventService;
    @Autowired private ModelMapper modelMapper;
    @Autowired private EventValidator eventValidator;
    @Autowired private StudyRepository studyRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;

    @InitBinder("eventForm")
    public void eventFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateTFValues(path, account);
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(EventForm.builder().build());

        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentUser Account account, @PathVariable String path, @Valid EventForm eventForm, Errors errors,
                                 Model model) {
        Study study = studyService.getStudyToUpdateTFValues(path, account);
        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }
        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), study, account);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvents(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event, Model model) {
        model.addAttribute(event);
        model.addAttribute(account);
        model.addAttribute(studyRepository.findStudyWithStatusByPath(path));

        return "event/view";
    }

    @GetMapping("/events")
    public String eventStudy(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        List<Event> events = eventRepository.findByStudy(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        events.forEach(event -> {
            if(event.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(event);
            }else {
                newEvents.add(event);
            }
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);
        return "study/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long id
                                  , Model model) {
        Study study = studyService.getStudyToUpdatePath(account, path);
        Event event = eventRepository.findById(id).orElseThrow();

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));

        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long id
                                , @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdatePath(account, path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateFomr(eventForm, event, errors);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvents(eventForm, event);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

//    @PostMapping("/events/{id}/delete")
    @DeleteMapping("/events/{id}")
    public String deleteEvents(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToUpdateTFValues(path, account);
        eventService.deleteEvent(eventRepository.findById(id).orElseThrow());

        return "redirect:/study/" + study.getEncodePath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String enrollEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(eventRepository.findById(id).orElseThrow(), account);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + id;
    }

    @PostMapping("/events/{id}/disenroll")
    public String disenrollEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.cancelEnrollment(eventRepository.findById(id).orElseThrow(), account);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + id;
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentsId}/accept")
    public String enrollmentsAccept(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event,
                                    @PathVariable("enrollmentsId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(path, account);
        eventService.enrollmentAccepted(event, enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentsId}/reject")
    public String enrollmentsReject(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event,
                                    @PathVariable("enrollmentsId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(path, account);
        eventService.enrollmentReject(event, enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentsId}/checkin")
    public String enrollmentsCheckin(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event,
                                     @PathVariable("enrollmentsId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(path, account);
        eventService.enrollmentCheckin(event, enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentsId}/cancel-checkin")
    public String enrollmentsCancel_Checkin(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event,
                                            @PathVariable("enrollmentsId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(path, account);
        eventService.enrollmentCancelCheckin(event, enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

}
