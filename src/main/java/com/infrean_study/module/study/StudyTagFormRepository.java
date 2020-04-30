package com.infrean_study.study;

import com.infrean_study.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyTagFormRepository  extends JpaRepository<Tag, Long> {
    Tag findByTitle(String title);
}
