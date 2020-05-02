package com.infrean_study.module.study.event;

import com.infrean_study.module.study.Study;
import org.springframework.context.ApplicationEvent;

public class StudyUpdateEvent extends ApplicationEvent {
    public StudyUpdateEvent(Study study, String s) {
        super();
    }
}
