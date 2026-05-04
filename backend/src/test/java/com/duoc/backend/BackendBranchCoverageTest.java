package com.duoc.backend;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BackendBranchCoverageTest {

    private static final String SECRET = "SeguridadCalidadJWTSecretKey2026SeguridadCalidadJWT!";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtFilterShouldNotFilterCoversEveryPublicAndPrivateBranch() {
        JWTAuthorizationFilter filter = new JWTAuthorizationFilter(new JWTAuthenticationConfig(SECRET, 60000));

        assertThat(filter.shouldNotFilter(request("POST", "/login"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/login"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/recipes"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/recipes/latest"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/recipes/popular"))).isTrue();
        assertThat(filter.shouldNotFilter(request("GET", "/recipes/search"))).isTrue();

        assertThat(filter.shouldNotFilter(request("POST", "/recipes"))).isFalse();
        assertThat(filter.shouldNotFilter(request("GET", "/recipes/1"))).isFalse();
        assertThat(filter.shouldNotFilter(request("DELETE", "/recipes/1"))).isFalse();
    }

    @Test
    void jwtFilterCoversBearerHeaderBranchesAndInvalidTokenBranch() throws ServletException, IOException {
        JWTAuthenticationConfig config = new JWTAuthenticationConfig(SECRET, 60000);
        JWTAuthorizationFilter filter = new JWTAuthorizationFilter(config);

        MockHttpServletRequest missingHeader = request("POST", "/recipes/1/comments");
        MockHttpServletResponse missingResponse = new MockHttpServletResponse();
        filter.doFilter(missingHeader, missingResponse, new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        MockHttpServletRequest wrongPrefix = request("POST", "/recipes/1/comments");
        wrongPrefix.addHeader(Constants.AUTHORIZATION_HEADER, "Basic abc123");
        MockHttpServletResponse wrongResponse = new MockHttpServletResponse();
        filter.doFilter(wrongPrefix, wrongResponse, new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        MockHttpServletRequest invalidBearer = request("POST", "/recipes/1/comments");
        invalidBearer.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER_PREFIX + "token.invalido");
        MockHttpServletResponse invalidResponse = new MockHttpServletResponse();
        filter.doFilter(invalidBearer, invalidResponse, new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        MockHttpServletRequest validBearer = request("POST", "/recipes/1/comments");
        validBearer.addHeader(Constants.AUTHORIZATION_HEADER, config.getJWTToken("chefana", Constants.USER_ROLE));
        MockHttpServletResponse validResponse = new MockHttpServletResponse();
        filter.doFilter(validBearer, validResponse, new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void recipeSearchCoversNullBlankPositiveAndNegativeFilterBranches() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        Recipe fullRecipe = ServiceAndControllerTest.recipe(1L, "Sopaipillas", "Chilena", "Chile", "Fácil", List.of("Zapallo", "Harina"), 2);
        fullRecipe.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        Recipe nullFieldsRecipe = ServiceAndControllerTest.recipe(2L, "Sin datos", null, null, null, List.of(), 1);
        nullFieldsRecipe.setCreatedAt(LocalDateTime.of(2026, 5, 2, 10, 0));

        when(recipes.findAll()).thenReturn(List.of(fullRecipe, nullFieldsRecipe));
        RecipeController controller = new RecipeController(
                recipes,
                mock(UserRepository.class),
                mock(RecipeCommentRepository.class),
                mock(RecipeShareRepository.class),
                "http://localhost/recetas"
        );

        assertThat(controller.searchRecipes(null, null, null, null, null)).hasSize(2);
        assertThat(controller.searchRecipes(" ", " ", " ", " ", " ")).hasSize(2);
        assertThat(controller.searchRecipes("sopa", "chil", "chi", "fác", "zapa")).containsExactly(fullRecipe);
        assertThat(controller.searchRecipes("sopa", "peruana", null, null, null)).isEmpty();
        assertThat(controller.searchRecipes("sopa", null, "perú", null, null)).isEmpty();
        assertThat(controller.searchRecipes("sopa", null, null, "difícil", null)).isEmpty();
        assertThat(controller.searchRecipes("sopa", null, null, null, "queso")).isEmpty();
        assertThat(controller.searchRecipes("sin", "chil", null, null, null)).isEmpty();
    }

    @Test
    void recipeControllerCreateCoversNullAndNonNullCreatedAtBranches() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        RecipeController controller = new RecipeController(
                recipes,
                mock(UserRepository.class),
                mock(RecipeCommentRepository.class),
                mock(RecipeShareRepository.class),
                "http://localhost/recetas"
        );

        Recipe recipeWithoutDate = ServiceAndControllerTest.recipe(1L, "Cazuela", "Chilena", "Chile", "Media", List.of("Carne"), 0);
        recipeWithoutDate.setCreatedAt(null);
        when(recipes.save(recipeWithoutDate)).thenReturn(recipeWithoutDate);

        ResponseEntity<Recipe> createdWithoutDate = controller.createRecipe(recipeWithoutDate);
        assertThat(createdWithoutDate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdWithoutDate.getBody().getCreatedAt()).isNotNull();

        LocalDateTime fixedDate = LocalDateTime.of(2026, 5, 3, 11, 30);
        Recipe recipeWithDate = ServiceAndControllerTest.recipe(2L, "Humitas", "Chilena", "Chile", "Fácil", List.of("Choclo"), 0);
        recipeWithDate.setCreatedAt(fixedDate);
        when(recipes.save(recipeWithDate)).thenReturn(recipeWithDate);

        ResponseEntity<Recipe> createdWithDate = controller.createRecipe(recipeWithDate);
        assertThat(createdWithDate.getBody().getCreatedAt()).isEqualTo(fixedDate);
    }

    @Test
    void recipeControllerCommentsAndSharesCoverAuthenticationNameBranches() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        UserRepository users = mock(UserRepository.class);
        Recipe recipe = ServiceAndControllerTest.recipe(1L, "Porotos", "Chilena", "Chile", "Media", List.of("Porotos"), 0);
        when(recipes.findById(1L)).thenReturn(Optional.of(recipe));

        RecipeController controller = new RecipeController(
                recipes,
                users,
                mock(RecipeCommentRepository.class),
                mock(RecipeShareRepository.class),
                "http://localhost/recetas"
        );

        CreateCommentRequest commentRequest = new CreateCommentRequest();
        commentRequest.setCommentText("Comentario válido");
        commentRequest.setRating(5);

        ShareRecipeRequest shareRequest = new ShareRecipeRequest();
        shareRequest.setPlatform("sitio");

        Authentication nullNameAuth = mock(Authentication.class);
        when(nullNameAuth.getName()).thenReturn(null);

        assertThatThrownBy(() -> controller.addComment(1L, commentRequest, nullNameAuth))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Debe iniciar sesión");
        assertThatThrownBy(() -> controller.shareRecipe(1L, shareRequest, nullNameAuth))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Debe iniciar sesión");
    }

    @Test
    void recipeCollectionsCoverNullAndNonNullDefensiveCopyBranches() {
        Recipe recipe = new Recipe();

        recipe.setIngredients(null);
        recipe.setPhotos(null);
        recipe.setVideos(null);
        assertThat(recipe.getIngredients()).isEmpty();
        assertThat(recipe.getPhotos()).isEmpty();
        assertThat(recipe.getVideos()).isEmpty();

        List<String> ingredients = new java.util.ArrayList<>(List.of("Arroz"));
        List<String> photos = new java.util.ArrayList<>(List.of("https://example.com/uno.jpg"));
        List<String> videos = new java.util.ArrayList<>(List.of("https://youtube.com/watch?v=abc"));

        recipe.setIngredients(ingredients);
        recipe.setPhotos(photos);
        recipe.setVideos(videos);

        ingredients.add("No debe aparecer");
        photos.add("https://example.com/dos.jpg");
        videos.add("https://youtube.com/watch?v=def");

        assertThat(recipe.getIngredients()).containsExactly("Arroz");
        assertThat(recipe.getPhotos()).containsExactly("https://example.com/uno.jpg");
        assertThat(recipe.getVideos()).containsExactly("https://youtube.com/watch?v=abc");
    }

    @Test
    void recipeControllerGetCommentsCoversExistingAndMissingRecipeBranches() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        RecipeCommentRepository comments = mock(RecipeCommentRepository.class);
        when(recipes.existsById(1L)).thenReturn(true);
        when(recipes.existsById(2L)).thenReturn(false);
        when(comments.findByRecipeIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        RecipeController controller = new RecipeController(
                recipes,
                mock(UserRepository.class),
                comments,
                mock(RecipeShareRepository.class),
                "http://localhost/recetas"
        );

        assertThat(controller.getComments(1L)).isEmpty();
        assertThatThrownBy(() -> controller.getComments(2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Receta no encontrada");
    }

    private static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }
}
