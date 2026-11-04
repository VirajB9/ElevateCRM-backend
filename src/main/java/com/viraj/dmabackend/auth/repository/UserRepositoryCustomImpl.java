package com.viraj.dmabackend.auth.repository;

import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
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
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {

        Query query;
        if (keyword != null && !keyword.trim().isEmpty()) {
            query = TextQuery
                    .queryText(TextCriteria.forDefaultLanguage().matching(keyword.trim())).sortByScore();
        } else {
            query = new Query();
        }

        query.addCriteria(Criteria.where("status").ne(UserStatus.DELETED));

        long total = mongoTemplate.count(query, User.class);

        query.with(pageable);

        List<User> users = mongoTemplate.find(query, User.class);

        return new PageImpl<>(users, pageable, total);
    }
}
