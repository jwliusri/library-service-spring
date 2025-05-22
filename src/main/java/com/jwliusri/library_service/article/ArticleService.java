package com.jwliusri.library_service.article;

import java.util.List;
import com.jwliusri.library_service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jwliusri.library_service.user.User;

@Service
public class ArticleService {

    private final UserService userService;

    private final ArticleRepository articleRepository;

    ArticleService(ArticleRepository articleRepository, UserService userService) {
        this.articleRepository = articleRepository;
        this.userService = userService;
    }

    public List<ArticleResponse> getAllArticles() {
        return articleRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public ArticleResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id)
            .orElseThrow();

        // TODO: Check if article is public or user has access

        return mapToResponse(article);

    }

    public ArticleResponse createArticle(ArticleRequest request, Authentication auth) {
        User author = userService.getAuthUser(auth);

        Article article = Article.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .isPublic(request.isPublic())
            .author(author)
            .build();

        article = articleRepository.save(article);
        return mapToResponse(article);
    }

    public ArticleResponse updateArticle(Long id, ArticleRequest request) {
        Article article = articleRepository.findById(id)
            .orElseThrow();

        // TODO: Check if user has access

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setPublic(request.isPublic());

        article = articleRepository.save(article);
        return mapToResponse(article);
    }

    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
            .orElseThrow();

        // TODO: Check if user has access

        articleRepository.delete(article);
    }

    private ArticleResponse mapToResponse(Article article) {
        return new ArticleResponse(
            article.getId(),
            article.getTitle(),
            article.getContent(),
            article.getAuthor().getId(),
            article.getAuthor().getFullName(),
            article.isPublic(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }

    
}
