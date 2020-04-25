package com.infrean_study.study;

import com.infrean_study.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByPath(String path);

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(value = "Study.withRecruitingAndPublishedAndClosed", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithStatusByPath(String path);

    @EntityGraph(value = "Study.withMembersAndPath", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithStudypathByPath(String path);

    @EntityGraph(value = "Study.withMembersAndTitle", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithStudytitleByPath(String path);
}
