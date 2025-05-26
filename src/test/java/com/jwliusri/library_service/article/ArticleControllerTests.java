package com.jwliusri.library_service.article;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleControllerTests {

    @Mock
    private ArticleService articleService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ArticleController articleController;

    private ArticleResponseDto testResponse;
    private ArticleRequestDto testRequest;

    @BeforeEach
    void setUp() {
        testResponse = new ArticleResponseDto(
                1L,
                "Test Article",
                "Test Content",
                1L,
                "Test Author",
                true,
                null,
                null
        );

        testRequest = new ArticleRequestDto(
                "Test Title",
                "Test Content",
                true
        );
    }

    @Test
    void getAllArticles_ShouldReturnArticles() {
        // Arrange
        when(articleService.getAllArticlesByUserAccess(authentication))
                .thenReturn(List.of(testResponse));

        // Act
        List<ArticleResponseDto> result = articleController.getAllArticles(authentication);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testResponse, result.get(0));
        verify(articleService, times(1)).getAllArticlesByUserAccess(authentication);
    }

    @Test
    void getArticleById_ShouldReturnArticle() {
        // Arrange
        when(articleService.getArticleById(1L, authentication)).thenReturn(testResponse);

        // Act
        ArticleResponseDto result = articleController.getArticleById(1L, authentication);

        // Assert
        assertEquals(testResponse, result);
        verify(articleService, times(1)).getArticleById(1L, authentication);
    }

    @Test
    void getArticleById_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(articleService.getArticleById(99L, authentication))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> articleController.getArticleById(99L, authentication));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Article not found", exception.getReason());
    }

    @Test
    void createArticle_ShouldReturnCreatedArticle() {
        // Arrange
        when(articleService.createArticle(testRequest, authentication)).thenReturn(testResponse);

        // Act
        ArticleResponseDto result = articleController.createArticle(testRequest, authentication);

        // Assert
        assertEquals(testResponse, result);
        verify(articleService, times(1)).createArticle(testRequest, authentication);
    }

    @Test
    void createArticle_WithInvalidInput_ShouldThrowException() {
        // Arrange
        ArticleRequestDto invalidRequest = new ArticleRequestDto("", "", false);
        when(articleService.createArticle(invalidRequest, authentication))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> articleController.createArticle(invalidRequest, authentication));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void updateArticle_ShouldReturnUpdatedArticle() {
        // Arrange
        when(articleService.updateArticle(1L, testRequest, authentication)).thenReturn(testResponse);

        // Act
        ArticleResponseDto result = articleController.updateArticle(1L, testRequest, authentication);

        // Assert
        assertEquals(testResponse, result);
        verify(articleService, times(1)).updateArticle(1L, testRequest, authentication);
    }

    @Test
    void updateArticle_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(articleService.updateArticle(99L, testRequest, authentication))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> articleController.updateArticle(99L, testRequest, authentication));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteArticle_ShouldCallService() {
        // Arrange - no return to verify since method is void
        doNothing().when(articleService).deleteArticle(1L, authentication);

        // Act
        articleController.deleteArticle(1L, authentication);

        // Assert
        verify(articleService, times(1)).deleteArticle(1L, authentication);
    }

    @Test
    void deleteArticle_WhenNotFound_ShouldThrowException() {
        // Arrange
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"))
                .when(articleService).deleteArticle(99L, authentication);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> articleController.deleteArticle(99L, authentication));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteArticle_WhenUnauthorized_ShouldThrowException() {
        // Arrange
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"))
                .when(articleService).deleteArticle(1L, authentication);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> articleController.deleteArticle(1L, authentication));
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }
}