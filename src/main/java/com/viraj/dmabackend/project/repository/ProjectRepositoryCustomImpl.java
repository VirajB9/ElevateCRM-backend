package com.viraj.dmabackend.project.repository;

import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryCustomImpl implements ProjectRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Project> searchProjects(String keyword, Pageable pageable) {

        Query query = TextQuery
                .queryText(
                        TextCriteria
                                .forDefaultLanguage()
                                .matching(keyword)
                ).sortByScore();

        query.addCriteria(
                Criteria.where("status")
                        .ne(ProjectStatus.ARCHIVED));

        long total = mongoTemplate.count(query, Project.class);

        query.with(pageable);

        List<Project> projects = mongoTemplate.find(query, Project.class);

        return new PageImpl<>(projects, pageable, total);
    }
}
