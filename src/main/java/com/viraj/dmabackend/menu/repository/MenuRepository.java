package com.viraj.dmabackend.menu.repository;

import com.viraj.dmabackend.menu.entity.Menu;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends MongoRepository<Menu, String> {

    List<Menu> findByActiveTrueOrderByOrderIndexAsc();

    Optional<Menu> findByTitleAndActiveTrue(String title);

    Optional<Menu> findByPathAndActiveTrue(String path);

    boolean existsByTitleAndIdNotAndActiveTrue(String title, String id);

    boolean existsByPathAndIdNotAndActiveTrue(String path, String id);

    default Optional<Menu> findByTitle(String title) {
        return findByTitleAndActiveTrue(title);
    }

    default Optional<Menu> findByPath(String path) {
        return findByPathAndActiveTrue(path);
    }

    default boolean existsByTitleAndIdNot(String title, String id) {
        return existsByTitleAndIdNotAndActiveTrue(title, id);
    }

    default boolean existsByPathAndIdNot(String path, String id) {
        return existsByPathAndIdNotAndActiveTrue(path, id);
    }

    boolean existsByParentIdAndActiveTrue(String parentId);
}
