package com.viraj.dmabackend.lead.repository;

import com.viraj.dmabackend.common.util.PaginationUtils;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadStatus;
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
public class LeadRepositoryCustomImpl
        implements LeadRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Lead> searchLeads(String keyword, Pageable pageable) {

        Query query;
        if (keyword != null && !keyword.trim().isEmpty()) {
            query = TextQuery
                    .queryText(TextCriteria.forDefaultLanguage().matching(keyword.trim())).sortByScore();
        } else {
            query = new Query();
        }

        query.addCriteria(Criteria.where("status").ne(LeadStatus.DELETED));

        long total = mongoTemplate.count(query, Lead.class);

        Pageable effectivePageable = PaginationUtils.clampPageable(pageable);
        if (total == 0) {
            return new PageImpl<>(java.util.Collections.emptyList(), effectivePageable, 0);
        }

        query.with(effectivePageable);

        List<Lead> leads = mongoTemplate.find(query, Lead.class);

        return new PageImpl<>(leads, effectivePageable, total);
    }
}