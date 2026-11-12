package com.viraj.dmabackend.project.repository;

import com.viraj.dmabackend.common.util.PaginationUtils;
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

        String cleanKeyword = PaginationUtils.clampKeyword(keyword);
        Query query;
        if (cleanKeyword != null && !cleanKeyword.isEmpty()) {
            query = TextQuery
                    .queryText(
                            TextCriteria
                                    .forDefaultLanguage()
                                    .matching(cleanKeyword)
                    ).sortByScore();
        } else {
            query = new Query();
        }

        query.addCriteria(
                Criteria.where("status")
                        .ne(ProjectStatus.ARCHIVED));

        long total = mongoTemplate.count(query, Project.class);

        Pageable effectivePageable = PaginationUtils.clampPageable(pageable);
        if (total == 0) {
            return new PageImpl<>(java.util.Collections.emptyList(), effectivePageable, 0);
        }

        query.with(effectivePageable);

        List<Project> projects = mongoTemplate.find(query, Project.class);

        return new PageImpl<>(projects, effectivePageable, total);
    }
}
