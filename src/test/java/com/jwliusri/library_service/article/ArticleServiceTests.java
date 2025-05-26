package com.jwliusri.library_service.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.jwliusri.library_service.user.RoleEnum;
import com.jwliusri.library_service.user.User;
import com.jwliusri.library_service.user.UserService;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTests {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ArticleService articleService;

    private User testUser;
    private User testAdmin;
    private User testEditor;
    private User testContributor;
    private User testViewer;
    private Article testArticle;
    private Article testPrivateArticle;
    private ArticleRequestDto testRequest;

    @BeforeEach
    void setUp() {
        // Setup test users with different roles
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .role(RoleEnum.ROLE_CONTRIBUTOR)
                .build();

        testAdmin = User.builder()
                .id(2L)
                .username("admin")
                .fullName("Admin User")
                .role(RoleEnum.ROLE_SUPER_ADMIN)
                .build();

        testEditor = User.builder()
                .id(3L)
                .username("editor")
                .fullName("Editor User")
                .role(RoleEnum.ROLE_EDITOR)
                .build();

        testContributor = User.builder()
                .id(4L)
                .username("contributor")
                .fullName("Contributor User")
                .role(RoleEnum.ROLE_CONTRIBUTOR)
                .build();

        testViewer = User.builder()
                .id(5L)
                .username("viewer")
                .fullName("Viewer User")
                .role(RoleEnum.ROLE_VIEWER)
                .build();

        // Setup test articles
        testArticle = Article.builder()
                .id(1L)
                .title("Public Article")
                .content("Public content")
                .isPublic(true)
                .author(testUser)
                .build();

        testPrivateArticle = Article.builder()
                .id(2L)
                .title("Private Article")
                .content("Private content")
                .isPublic(false)
                .author(testUser)
                .build();

        // Setup test request
        testRequest = new ArticleRequestDto(
                "Test Title",
                "Test Content",
                true
        );
    }

    @Test
    void getAllArticles_ShouldReturnAllArticles() {
        // Arrange
        when(articleRepository.findAll()).thenReturn(List.of(testArticle, testPrivateArticle));

        // Act
        List<ArticleResponseDto> result = articleService.getAllArticles();

        // Assert
        assertEquals(2, result.size());
        verify(articleRepository, times(1)).findAll();
    }

    @Test
    void getAllPublicOrOwnedArticles_WithUser_ShouldReturnPublicAndOwnedArticles() {
        // Arrange
        when(articleRepository.findAllPublicOrAuthored(testUser.getId()))
                .thenReturn(List.of(testArticle, testPrivateArticle));

        // Act
        List<ArticleResponseDto> result = articleService.getAllPublicOrOwnedArticles(testUser);

        // Assert
        assertEquals(2, result.size());
        verify(articleRepository, times(1)).findAllPublicOrAuthored(testUser.getId());
    }

    @Test
    void getAllPublicOrOwnedArticles_WithDifferentUser_ShouldReturnOnlyPublicArticles() {
        // Arrange
        when(articleRepository.findAllPublicOrAuthored(testAdmin.getId()))
                .thenReturn(List.of(testArticle));

        // Act
        List<ArticleResponseDto> result = articleService.getAllPublicOrOwnedArticles(testAdmin);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testArticle.getTitle(), result.get(0).getTitle());
        verify(articleRepository, times(1)).findAllPublicOrAuthored(testAdmin.getId());
    }

    @Test
    void getAllPublicOrOwnedArticles_WithAuthentication_ShouldReturnPublicAndOwnedArticles() {
        // Arrange
        when(userService.getAuthUser(authentication)).thenReturn(testUser);
        when(articleRepository.findAllPublicOrAuthored(testUser.getId()))
                .thenReturn(List.of(testArticle, testPrivateArticle));

        // Act
        List<ArticleResponseDto> result = articleService.getAllPublicOrOwnedArticles(authentication);

        // Assert
        assertEquals(2, result.size());
        verify(userService, times(1)).getAuthUser(authentication);
        verify(articleRepository, times(1)).findAllPublicOrAuthored(testUser.getId());
    }

    @Test
    void getAllArticlesByUserAccess_WithAdmin_ShouldReturnAllArticles() {
        // Arrange
        when(userService.getAuthUser(authentication)).thenReturn(testAdmin);
        when(articleRepository.findAll()).thenReturn(List.of(testArticle, testPrivateArticle));

        // Act
        List<ArticleResponseDto> result = articleService.getAllArticlesByUserAccess(authentication);

        // Assert
        assertEquals(2, result.size());
        verify(articleRepository, times(1)).findAll();
        verify(articleRepository, never()).findAllPublicOrAuthored(any());
    }

    @Test
    void getAllArticlesByUserAccess_WithViewer_ShouldReturnPublicAndOwnedArticles() {
        // Arrange
        when(userService.getAuthUser(authentication)).thenReturn(testViewer);
        when(articleRepository.findAllPublicOrAuthored(testViewer.getId()))
                .thenReturn(List.of(testArticle));

        // Act
        List<ArticleResponseDto> result = articleService.getAllArticlesByUserAccess(authentication);

        // Assert
        assertEquals(1, result.size());
        verify(articleRepository, times(1)).findAllPublicOrAuthored(testViewer.getId());
        verify(articleRepository, never()).findAll();
    }

    @Test
    void getArticleById_WithPublicArticle_ShouldReturnArticle() {
        // Arrange
        when(articleRepository.findById(testArticle.getId())).thenReturn(Optional.of(testArticle));
        when(userService.getAuthUser(authentication)).thenReturn(testViewer);

        // Act
        ArticleResponseDto result = articleService.getArticleById(testArticle.getId(), authentication);

        // Assert
        assertNotNull(result);
        assertEquals(testArticle.getTitle(), result.getTitle());
        verify(articleRepository, times(1)).findById(testArticle.getId());
    }

    @Test
    void getArticleById_WithPrivateArticleAndAdmin_ShouldReturnArticle() {
        // Arrange
        when(articleRepository.findById(testPrivateArticle.getId())).thenReturn(Optional.of(testPrivateArticle));
        when(userService.getAuthUser(authentication)).thenReturn(testAdmin);

        // Act
        ArticleResponseDto result = articleService.getArticleById(testPrivateArticle.getId(), authentication);

        // Assert
        assertNotNull(result);
        assertEquals(testPrivateArticle.getTitle(), result.getTitle());
    }

    @Test
    void getArticleById_WithPrivateArticleAndUnauthorizedUser_ShouldThrowForbidden() {
        // Arrange
        when(articleRepository.findById(testPrivateArticle.getId())).thenReturn(Optional.of(testPrivateArticle));
        when(userService.getAuthUser(authentication)).thenReturn(testViewer);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                () -> articleService.getArticleById(testPrivateArticle.getId(), authentication));
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You don't have permission to view this article", exception.getReason());
    }

    @Test
    void getArticleById_WithNonExistentArticle_ShouldThrowNotFound() {
        // Arrange
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                () -> articleService.getArticleById(99L, authentication));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Article not found", exception.getReason());
    }

    @Test
    void createArticle_ShouldCreateAndReturnNewArticle() {
        // Arrange
        when(userService.getAuthUser(authentication)).thenReturn(testUser);
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            article.setId(10L); // Simulate DB auto-generated ID
            return article;
        });

        // Act
        ArticleResponseDto result = articleService.createArticle(testRequest, authentication);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(testRequest.getTitle(), result.getTitle());
        assertEquals(testRequest.getContent(), result.getContent());
        assertEquals(testUser.getId(), result.getAuthorId());
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    @Test
    void updateArticle_WithOwner_ShouldUpdateAndReturnArticle() {
        // Arrange
        when(articleRepository.findById(testArticle.getId())).thenReturn(Optional.of(testArticle));
        when(userService.getAuthUser(authentication)).thenReturn(testUser);
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);

        // Act
        ArticleResponseDto result = articleService.updateArticle(
                testArticle.getId(), 
                testRequest, 
                authentication);

        // Assert
        assertNotNull(result);
        assertEquals(testRequest.getTitle(), testArticle.getTitle());
        assertEquals(testRequest.getContent(), testArticle.getContent());
        verify(articleRepository, times(1)).save(testArticle);
    }

    @Test
    void updateArticle_WithEditorAndNotOwner_ShouldThrowForbidden() {
        // Arrange
        when(articleRepository.findById(testArticle.getId())).thenReturn(Optional.of(testArticle));
        when(userService.getAuthUser(authentication)).thenReturn(testEditor);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                () -> articleService.updateArticle(testArticle.getId(), testRequest, authentication));
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You don't have permission to edit this article", exception.getReason());
        verify(articleRepository, never()).save(any());
    }

    @Test
    void updateArticle_WithAdmin_ShouldUpdateAndReturnArticle() {
        // Arrange
        when(articleRepository.findById(testArticle.getId())).thenReturn(Optional.of(testArticle));
        when(userService.getAuthUser(authentication)).thenReturn(testAdmin);
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);

        // Act
        ArticleResponseDto result = articleService.updateArticle(
                testArticle.getId(), 
                testRequest, 
                authentication);

        // Assert
        assertNotNull(result);
        verify(articleRepository, times(1)).save(testArticle);
    }

    @Test
    void updateArticle_WithAdmin_ShouldThrowNotFound() {
        // Arrange
        when(userService.getAuthUser(authentication)).thenReturn(testAdmin);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                () -> articleService.updateArticle(999L, testRequest, authentication));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Article not found", exception.getReason());
        verify(articleRepository, never()).save(any());
    }

    @Test
    void deleteArticle_WithOwner_ShouldDeleteArticle() {
        // Arrange
        when(articleRepository.findById(testArticle.getId())).thenReturn(Optional.of(testArticle));
        when(userService.getAuthUser(authentication)).thenReturn(testUser);

        // Act
        articleService.deleteArticle(testArticle.getId(), authentication);

        // Assert
        verify(articleRepository, times(1)).delete(testArticle);
    }

    @Test
    void deleteArticle_WithEditorAndNotOwner_ShouldThrowForbidden() {
        // Arrange
        when(articleRepository.findById(testArticle.getId())).thenReturn(Optional.of(testArticle));
        when(userService.getAuthUser(authentication)).thenReturn(testEditor);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                () -> articleService.deleteArticle(testArticle.getId(), authentication));
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You don't have permission to delete this article", exception.getReason());
        verify(articleRepository, never()).delete(any());
    }

    @Test
    void deleteArticle_WithAdmin_ShouldDeleteArticle() {
        // Arrange
        when(articleRepository.findById(testArticle.getId())).thenReturn(Optional.of(testArticle));
        when(userService.getAuthUser(authentication)).thenReturn(testAdmin);

        // Act
        articleService.deleteArticle(testArticle.getId(), authentication);

        // Assert
        verify(articleRepository, times(1)).delete(testArticle);
    }

    @Test
    void deleteArticle_WithAdmin_ShouldThrowNotFound() {
        // Arrange
        when(userService.getAuthUser(authentication)).thenReturn(testAdmin);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
                () -> articleService.deleteArticle(999L, authentication));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Article not found", exception.getReason());
        verify(articleRepository, never()).delete(any());
    }

    @Test
    void mapToResponse_ShouldConvertArticleToResponseDto() {
        // Act
        ArticleResponseDto result = articleService.mapToResponse(testArticle);

        // Assert
        assertEquals(testArticle.getId(), result.getId());
        assertEquals(testArticle.getTitle(), result.getTitle());
        assertEquals(testArticle.getContent(), result.getContent());
        assertEquals(testArticle.getAuthor().getId(), result.getAuthorId());
        assertEquals(testArticle.getAuthor().getFullName(), result.getAuthorName());
        assertEquals(testArticle.isPublic(), result.isPublic());
        assertEquals(testArticle.getCreatedAt(), result.getCreatedAt());
        assertEquals(testArticle.getUpdatedAt(), result.getUpdatedAt());
    }
}