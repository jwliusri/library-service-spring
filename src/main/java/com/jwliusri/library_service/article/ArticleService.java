package com.jwliusri.library_service.article;

import java.util.List;
import java.util.Set;

import com.jwliusri.library_service.user.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.jwliusri.library_service.user.RoleEnum;
import com.jwliusri.library_service.user.User;

@Service
public class ArticleService {

    private final UserService userService;

    private final ArticleRepository articleRepository;

    ArticleService(ArticleRepository articleRepository, UserService userService) {
        this.articleRepository = articleRepository;
        this.userService = userService;
    }

    public List<ArticleResponseDto> getAllArticles() {
        return articleRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public List<ArticleResponseDto> getAllPublicOrOwnedArticles(User authUser) {
        return articleRepository.findAllPublicOrAuthored(authUser.getId()).stream().map(this::mapToResponse).toList();
    }

    public List<ArticleResponseDto> getAllPublicOrOwnedArticles(Authentication auth) {
        User authUser = userService.getAuthUser(auth);
        return getAllPublicOrOwnedArticles(authUser);
    }

    public List<ArticleResponseDto> getAllArticlesByUserAccess(Authentication auth) {
        User authUser = userService.getAuthUser(auth);

        Set<RoleEnum> limitedRoles = Set.of(RoleEnum.ROLE_VIEWER, RoleEnum.ROLE_CONTRIBUTOR); 
        if (limitedRoles.contains(authUser.getRole())) {
            return getAllPublicOrOwnedArticles(authUser);
        }

        return getAllArticles();
    }

    public ArticleResponseDto getArticleById(Long id, Authentication auth) {
        User authUser = userService.getAuthUser(auth);
        Article article = articleRepository.findById(id)
            .orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));

        Set<RoleEnum> limitedRoles = Set.of(RoleEnum.ROLE_VIEWER, RoleEnum.ROLE_CONTRIBUTOR); 
        if (limitedRoles.contains(authUser.getRole()) && !article.isPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to view this article");
        }

        return mapToResponse(article);

    }

    public ArticleResponseDto createArticle(ArticleRequestDto request, Authentication auth) {
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

    public ArticleResponseDto updateArticle(Long id, ArticleRequestDto request, Authentication auth) {
        User authUser = userService.getAuthUser(auth);
        Article article = articleRepository.findById(id)
            .orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));

        Set<RoleEnum> limitedRoles = Set.of(RoleEnum.ROLE_EDITOR, RoleEnum.ROLE_CONTRIBUTOR); 
        if (limitedRoles.contains(authUser.getRole()) && !article.getAuthor().equals(authUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to edit this article");
        }

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setPublic(request.isPublic());

        article = articleRepository.save(article);
        return mapToResponse(article);
    }

    public void deleteArticle(Long id, Authentication auth) {
        User authUser = userService.getAuthUser(auth);
        Article article = articleRepository.findById(id)
            .orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));

        // TODO: Check if user has access
        Set<RoleEnum> limitedRoles = Set.of(RoleEnum.ROLE_EDITOR); 
        if (limitedRoles.contains(authUser.getRole()) && !article.getAuthor().equals(authUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete this article");
        }

        articleRepository.delete(article);
    }

    public ArticleResponseDto mapToResponse(Article article) {
        return new ArticleResponseDto(
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
