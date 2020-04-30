package com.infrean_study.event;

import com.infrean_study.domain.Event;
import com.infrean_study.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(value = "Event.withEnrollments", type = EntityGraph.EntityGraphType.LOAD)
    List<Event> findByStudy(Study study);

    @EntityGraph(value = "Event.withLimitOfEnrollments", type = EntityGraph.EntityGraphType.LOAD)
    Event findByTitle(String title);
}
