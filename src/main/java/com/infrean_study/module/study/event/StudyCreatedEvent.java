package com.infrean_study.module.study.event;

import com.infrean_study.module.study.Study;
import org.springframework.context.ApplicationEvent;

public class StudyCreatedEvent extends ApplicationEvent {
    public StudyCreatedEvent(Study newStudy) {
        super();
    }
}
