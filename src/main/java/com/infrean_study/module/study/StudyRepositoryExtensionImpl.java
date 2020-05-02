package com.infrean_study.module.study;

import java.util.List;

public interface StudyRepositoryExtension {

    List<Study> findByKeywork(String keywork);

}
