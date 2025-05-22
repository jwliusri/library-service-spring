package com.jwliusri.library_service.article;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query(value = "SELECT a.* FROM articles a where a.is_public = true or author_id = ?1", nativeQuery = true)
    List<Article> findAllPublicOrAuthored(Long authorId);
}
