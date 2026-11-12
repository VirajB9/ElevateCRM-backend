package com.viraj.dmabackend.client.repository;

import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.common.util.PaginationUtils;
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
public class ClientRepositoryCustomImpl implements ClientRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Client> searchClients(String keyword, Pageable pageable) {

        String cleanKeyword = PaginationUtils.clampKeyword(keyword);
        Query query;
        if (cleanKeyword != null && !cleanKeyword.isEmpty()) {
            query = TextQuery
                    .queryText(TextCriteria.forDefaultLanguage().matching(cleanKeyword))
                    .sortByScore();
        } else {
            query = new Query();
        }
                
        // ensure we don't return ARCHIVED clients
        query.addCriteria(Criteria.where("status").ne(ClientStatus.ARCHIVED).and("deleted").is(false));

        long total = mongoTemplate.count(query, Client.class);

        Pageable effectivePageable = PaginationUtils.clampPageable(pageable);
        if (total == 0) {
            return new PageImpl<>(java.util.Collections.emptyList(), effectivePageable, 0);
        }

        query.with(effectivePageable);

        List<Client> clients = mongoTemplate.find(query, Client.class);

        return new PageImpl<>(clients, effectivePageable, total);
    }
}
