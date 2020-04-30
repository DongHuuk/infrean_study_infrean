package com.infrean_study.module.settings.form;

import lombok.Data;

@Data
public class NotificationsForm {

    private Boolean studyCreatedByWeb;
    private Boolean studyCreatedByEmail;
    private Boolean studyEnrollmentResultByEmail;
    private Boolean studyEnrollmentResultByWeb;
    private Boolean studyUpdatedByEmail;
    private Boolean studyUpdatedByWeb;

}
