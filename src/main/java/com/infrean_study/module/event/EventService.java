package com.infrean_study.event;

import com.infrean_study.account.Account;
import com.infrean_study.domain.Enrollment;
import com.infrean_study.domain.Event;
import com.infrean_study.domain.Study;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
@Service
public class EventService {

    @Autowired private EventRepository eventRepository;
    @Autowired private ModelMapper modelMapper;
    @Autowired private EnrollmentRepository enrollmentRepository;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);

        return eventRepository.save(event);
    }

    public void updateEvents(EventForm eventForm, Event event) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public void newEnrollment(Event event, Account account) {
        if(!enrollmentRepository.existsByEventAndAccount(event, account)){
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if(enrollment.isAttended()){
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);

            if(event.isAbleToAcceptWaitingEnrollment()){
                Enrollment enrollmentToAccept = event.getTheFirstWaitingEnrollment();
                if(enrollmentToAccept != null){
                    enrollmentToAccept.setAccepted(true);
                }
            }
        }
    }

    public void enrollmentAccepted(Event event, Enrollment enrollment) {
        event.accept(enrollment);
//        for (Enrollment e : event.getEnrollments()) {
//            if (e.equals(enrollment) && !enrollment.isAccepted()) {
//                enrollment.setAccepted(true);
//            }
//        }
    }

    public void enrollmentReject(Event event, Enrollment enrollment) {
        event.reject(enrollment);
//        for (Enrollment e : event.getEnrollments()) {
//            if (e.equals(enrollment) && enrollment.isAccepted()) {
//                enrollment.setAccepted(false);
//            }
//        }
    }

    public void enrollmentCheckin(Event event, Enrollment enrollment) {
        event.checkin(enrollment);
    }

    public void enrollmentCancelCheckin(Event event, Enrollment enrollment) {
        event.cancel_checkin(enrollment);
    }
}
