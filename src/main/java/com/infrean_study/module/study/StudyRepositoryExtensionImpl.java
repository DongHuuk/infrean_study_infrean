package com.infrean_study.module.study;

import java.util.List;

public interface StudyRepositoryExtension {

    List<com.infrean_study.domain.Study> findByKeywork(String keywork);

}
