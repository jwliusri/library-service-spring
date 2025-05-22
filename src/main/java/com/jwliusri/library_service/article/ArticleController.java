package com.jwliusri.library_service.article;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("api/articles")
public class ArticleController {


    private final ArticleService articleService;

    ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<ArticleResponse> getAllArticles(Authentication auth) {
        return articleService.getAllArticlesByUserAccess(auth);
    }

    @GetMapping("/{id}")
    public ArticleResponse getArticleById(@PathVariable Long id, Authentication auth) {
        return articleService.getArticleById(id, auth);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EDITOR', 'CONTRIBUTOR')")
    public ArticleResponse createArticle(@Valid @RequestBody ArticleRequest request, Authentication auth) {
        return articleService.createArticle(request, auth);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EDITOR', 'CONTRIBUTOR')")
    public ArticleResponse updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleRequest request, Authentication auth) {
        return articleService.updateArticle(id, request, auth);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EDITOR')")
    public void deleteArticle(@PathVariable Long id, Authentication auth) {
        articleService.deleteArticle(id, auth);
    }

}
