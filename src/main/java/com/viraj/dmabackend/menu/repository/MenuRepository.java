package com.viraj.dmabackend.menu.repository;

import com.viraj.dmabackend.menu.entity.Menu;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends MongoRepository<Menu, String> {

    List<Menu> findByActiveTrueOrderByOrderIndexAsc();

    Optional<Menu> findByTitle(String title);

    Optional<Menu> findByPath(String path);

    boolean existsByTitleAndIdNot(String title, String id);

    boolean existsByPathAndIdNot(String path, String id);
}
