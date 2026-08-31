package com.viraj.dmabackend.client.repository;

import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.enums.ClientStatus;
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

        Query query = TextQuery
                .queryText(TextCriteria.forDefaultLanguage().matching(keyword))
                .sortByScore();
                
        // ensure we don't return ARCHIVED clients
        query.addCriteria(Criteria.where("status").ne(ClientStatus.ARCHIVED));

        long total = mongoTemplate.count(query, Client.class);

        query.with(pageable);

        List<Client> clients = mongoTemplate.find(query, Client.class);

        return new PageImpl<>(clients, pageable, total);
    }
}
