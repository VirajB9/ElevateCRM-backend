package com.viraj.dmabackend.lead.repository;

import com.viraj.dmabackend.lead.entity.Lead;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
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

        Query query = TextQuery
                .queryText(TextCriteria.forDefaultLanguage().matching(keyword)).sortByScore();

        query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("status").ne(com.viraj.dmabackend.lead.enums.LeadStatus.DELETED));

        long total = mongoTemplate.count(query, Lead.class);

        query.with(pageable);

        List<Lead> leads = mongoTemplate.find(query, Lead.class);

        return new PageImpl<>(leads, pageable, total);
    }
}