package com.viraj.dmabackend.common.counter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CounterRepositoryImpl implements CounterRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public long getNextSequence(String sequenceName) {

        Query query = new Query(Criteria.where("_id").is(sequenceName));

        Update update = new Update().inc("sequence", 1);

        FindAndModifyOptions options = FindAndModifyOptions.options()
                .returnNew(true)
                .upsert(true);

        Counter counter = mongoTemplate.findAndModify(
                query,
                update,
                options,
                Counter.class);

        assert counter != null;
        return counter.getSequence();
    }
}