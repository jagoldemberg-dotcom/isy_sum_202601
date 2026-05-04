package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceAndControllerTest {

    @Test
    void userDetailsServiceLoadsUserIgnoringCase() {
        UserRepository repository = mock(UserRepository.class);
        User user = new User("chefana", "chefana@example.com", "hash", Constants.USER_ROLE);
        when(repository.findByUsernameIgnoreCase("CHEFANA")).thenReturn(Optional.of(user));

        MyUserDetailsService service = new MyUserDetailsService(repository);

        assertThat(service.loadUserByUsername("CHEFANA")).isSameAs(user);
        assertThat(service.passwordEncoder()).isNotNull();
    }

    @Test
    void userDetailsServiceThrowsWhenUserDoesNotExist() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByUsernameIgnoreCase("nobody")).thenReturn(Optional.empty());

        MyUserDetailsService service = new MyUserDetailsService(repository);

        assertThatThrownBy(() -> service.loadUserByUsername("nobody"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loginReturnsJwtWhenCredentialsAreValid() {
        JWTAuthenticationConfig jwtConfig = mock(JWTAuthenticationConfig.class);
        MyUserDetailsService userDetailsService = mock(MyUserDetailsService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User user = new User("chefana", "chefana@example.com", "hash", Constants.USER_ROLE);
        when(userDetailsService.loadUserByUsername("chefana")).thenReturn(user);
        when(passwordEncoder.matches("1234", "hash")).thenReturn(true);
        when(jwtConfig.getJWTToken("chefana", Constants.USER_ROLE)).thenReturn("Bearer token");

        LoginRequest request = new LoginRequest();
        request.setUsername("chefana");
        request.setPassword("1234");

        ResponseEntity<LoginResponse> response = new LoginController(jwtConfig, userDetailsService, passwordEncoder).login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("Bearer token");
        assertThat(response.getBody().getUsername()).isEqualTo("chefana");
        assertThat(response.getBody().getRole()).isEqualTo(Constants.USER_ROLE);
    }

    @Test
    void loginRejectsInvalidPassword() {
        JWTAuthenticationConfig jwtConfig = mock(JWTAuthenticationConfig.class);
        MyUserDetailsService userDetailsService = mock(MyUserDetailsService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(userDetailsService.loadUserByUsername("chefana"))
                .thenReturn(new User("chefana", "chefana@example.com", "hash", Constants.USER_ROLE));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);
        LoginRequest request = new LoginRequest();
        request.setUsername("chefana");
        request.setPassword("bad");

        assertThatThrownBy(() -> new LoginController(jwtConfig, userDetailsService, passwordEncoder).login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    void recipeControllerReturnsSortedAndFilteredPublicRecipes() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        RecipeController controller = new RecipeController(recipes, mock(UserRepository.class), mock(RecipeCommentRepository.class), mock(RecipeShareRepository.class), "http://localhost/recetas/");
        Recipe oldRecipe = recipe(1L, "Sopaipillas", "Chilena", "Chile", "Fácil", List.of("Zapallo"), 1);
        oldRecipe.setCreatedAt(LocalDateTime.of(2026, 1, 1, 8, 0));
        Recipe newRecipe = recipe(2L, "Pizza", "Italiana", "Italia", "Media", List.of("Queso"), 5);
        newRecipe.setCreatedAt(LocalDateTime.of(2026, 2, 1, 8, 0));
        when(recipes.findAll()).thenReturn(List.of(oldRecipe, newRecipe));

        assertThat(controller.getAllRecipes()).extracting(Recipe::getName).containsExactly("Pizza", "Sopaipillas");
        assertThat(controller.searchRecipes("piz", null, null, null, "queso")).containsExactly(newRecipe);
    }

    @Test
    void recipeControllerCreatesAndDeletesRecipes() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        Recipe recipe = recipe(1L, "Cazuela", "Chilena", "Chile", "Media", List.of("Carne"), 0);
        recipe.setCreatedAt(null);
        when(recipes.save(recipe)).thenReturn(recipe);
        when(recipes.existsById(1L)).thenReturn(true);
        RecipeController controller = new RecipeController(recipes, mock(UserRepository.class), mock(RecipeCommentRepository.class), mock(RecipeShareRepository.class), "http://localhost/recetas");

        ResponseEntity<Recipe> createResponse = controller.createRecipe(recipe);
        ResponseEntity<Void> deleteResponse = controller.deleteRecipe(1L);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody().getCreatedAt()).isNotNull();
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(recipes).deleteById(1L);
    }

    @Test
    void recipeControllerAddsPhotoAndVideo() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        Recipe recipe = recipe(1L, "Cazuela", "Chilena", "Chile", "Media", List.of("Carne"), 0);
        when(recipes.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipes.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RecipeController controller = new RecipeController(recipes, mock(UserRepository.class), mock(RecipeCommentRepository.class), mock(RecipeShareRepository.class), "http://localhost/recetas");

        AddPhotoRequest photo = new AddPhotoRequest();
        photo.setPhotoUrl("  https://example.com/foto.jpg  ");
        AddVideoRequest video = new AddVideoRequest();
        video.setVideoUrl("  https://youtube.com/watch?v=1  ");

        controller.addPhoto(1L, photo);
        controller.addVideo(1L, video);

        assertThat(recipe.getPhotos()).contains("https://example.com/foto.jpg");
        assertThat(recipe.getVideos()).contains("https://youtube.com/watch?v=1");
    }

    @Test
    void recipeControllerAddsCommentAndUpdatesPopularity() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        UserRepository users = mock(UserRepository.class);
        RecipeCommentRepository comments = mock(RecipeCommentRepository.class);
        Recipe recipe = recipe(1L, "Cazuela", "Chilena", "Chile", "Media", List.of("Carne"), 1);
        User user = new User("chefana", "chefana@example.com", "hash", Constants.USER_ROLE);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("chefana");
        when(recipes.findById(1L)).thenReturn(Optional.of(recipe));
        when(users.findByUsernameIgnoreCase("chefana")).thenReturn(Optional.of(user));
        when(comments.save(any(RecipeComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipes.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RecipeController controller = new RecipeController(recipes, users, comments, mock(RecipeShareRepository.class), "http://localhost/recetas");
        CreateCommentRequest request = new CreateCommentRequest();
        request.setCommentText("  Muy rica  ");
        request.setRating(5);

        ResponseEntity<RecipeCommentResponse> response = controller.addComment(1L, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCommentText()).isEqualTo("Muy rica");
        assertThat(recipe.getPopularityScore()).isEqualTo(6);
    }

    @Test
    void recipeControllerSharesRecipeWithPlatformUrl() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        UserRepository users = mock(UserRepository.class);
        RecipeShareRepository shares = mock(RecipeShareRepository.class);
        Recipe recipe = recipe(1L, "Pastel de choclo", "Chilena", "Chile", "Media", List.of("Choclo"), 2);
        User user = new User("chefana", "chefana@example.com", "hash", Constants.USER_ROLE);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("chefana");
        when(recipes.findById(1L)).thenReturn(Optional.of(recipe));
        when(users.findByUsernameIgnoreCase("chefana")).thenReturn(Optional.of(user));
        when(shares.save(any(RecipeShare.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipes.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RecipeController controller = new RecipeController(recipes, users, mock(RecipeCommentRepository.class), shares, "http://localhost/recetas/");
        ShareRecipeRequest request = new ShareRecipeRequest();
        request.setPlatform("facebook");

        ResponseEntity<RecipeShareResponse> response = controller.shareRecipe(1L, request, authentication);

        assertThat(response.getBody().getPlatform()).isEqualTo("facebook");
        assertThat(response.getBody().getShareUrl()).contains("facebook.com");
        assertThat(recipe.getPopularityScore()).isEqualTo(4);
    }

    @Test
    void recipeControllerRejectsMissingRecipeOrInvalidUser() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        RecipeController controller = new RecipeController(recipes, mock(UserRepository.class), mock(RecipeCommentRepository.class), mock(RecipeShareRepository.class), "http://localhost/recetas");
        when(recipes.findById(99L)).thenReturn(Optional.empty());
        when(recipes.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> controller.getRecipeById(99L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.deleteRecipe(99L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.addComment(99L, new CreateCommentRequest(), mock(Authentication.class))).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void exceptionHandlerBuildsSafeErrorResponses() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<java.util.Map<String, Object>> status = handler.handleStatus(new ResponseStatusException(HttpStatus.NOT_FOUND, "Receta no encontrada"));
        ResponseEntity<java.util.Map<String, Object>> generic = handler.handleGeneric(new RuntimeException("detalle interno"));

        assertThat(status.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(status.getBody()).containsEntry("message", "Receta no encontrada");
        assertThat(generic.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(generic.getBody()).containsEntry("message", "Error interno del servidor");
    }

    static Recipe recipe(Long id, String name, String cuisine, String country, String difficulty, List<String> ingredients, int popularity) {
        Recipe recipe = ValidationAndModelTest.validRecipe();
        recipe.setId(id);
        recipe.setName(name);
        recipe.setCuisineType(cuisine);
        recipe.setCountryOfOrigin(country);
        recipe.setDifficulty(difficulty);
        recipe.setIngredients(ingredients);
        recipe.setPopularityScore(popularity);
        return recipe;
    }
}
