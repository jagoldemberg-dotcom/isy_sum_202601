package com.duoc.backend;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationAndModelTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void addPhotoRequestAcceptsOnlyHttpsUrls() {
        AddPhotoRequest request = new AddPhotoRequest();
        request.setPhotoUrl("https://example.com/foto.jpg");
        assertThat(validator.validate(request)).isEmpty();

        request.setPhotoUrl("http://example.com/foto.jpg");
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void addVideoRequestAcceptsOnlyAllowedProviders() {
        AddVideoRequest request = new AddVideoRequest();
        request.setVideoUrl("https://www.youtube.com/watch?v=abc");
        assertThat(validator.validate(request)).isEmpty();

        request.setVideoUrl("https://example.com/video");
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void createCommentRequestValidatesCommentAndRatingRange() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setCommentText("Muy buena receta");
        request.setRating(5);
        assertThat(validator.validate(request)).isEmpty();

        request.setCommentText("");
        request.setRating(6);
        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(2);
    }

    @Test
    void loginRequestValidatesRequiredFields() {
        LoginRequest request = new LoginRequest();
        request.setUsername("chefana");
        request.setPassword("password123");
        assertThat(validator.validate(request)).isEmpty();

        request.setUsername(" ");
        request.setPassword(null);
        assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    void shareRequestAllowsOnlyKnownPlatforms() {
        ShareRecipeRequest request = new ShareRecipeRequest();
        request.setPlatform("whatsapp");
        assertThat(validator.validate(request)).isEmpty();

        request.setPlatform("instagram");
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void recipeValidatesRequiredContentAndCopiesCollectionsDefensively() {
        Recipe recipe = validRecipe();
        assertThat(validator.validate(recipe)).isEmpty();

        List<String> ingredients = List.of("Harina", "Queso");
        recipe.setIngredients(ingredients);
        recipe.setPhotos(null);
        recipe.setVideos(null);

        assertThat(recipe.getIngredients()).containsExactly("Harina", "Queso");
        assertThat(recipe.getPhotos()).isEmpty();
        assertThat(recipe.getVideos()).isEmpty();

        recipe.setName("");
        assertThat(validator.validate(recipe)).isNotEmpty();
    }

    @Test
    void userImplementsSpringSecurityDetails() {
        User user = new User("admin", "admin@example.com", "hash", Constants.ADMIN_ROLE);
        user.setId(10L);
        user.setEmail("nuevo@example.com");
        user.setPassword("nuevoHash");
        user.setRole(Constants.USER_ROLE);
        user.setUsername("chef");

        assertThat(user.getId()).isEqualTo(10L);
        assertThat(user.getUsername()).isEqualTo("chef");
        assertThat(user.getEmail()).isEqualTo("nuevo@example.com");
        assertThat(user.getPassword()).isEqualTo("nuevoHash");
        assertThat(user.getAuthorities()).extracting("authority").containsExactly(Constants.USER_ROLE);
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void commentShareAndResponseObjectsExposeData() {
        User user = new User("chefana", "chefana@example.com", "hash", Constants.USER_ROLE);
        Recipe recipe = validRecipe();
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 1, 10, 30);

        RecipeComment comment = new RecipeComment();
        comment.setRecipe(recipe);
        comment.setUser(user);
        comment.setCommentText("Excelente");
        comment.setRating(4);
        comment.setCreatedAt(createdAt);

        RecipeCommentResponse response = RecipeCommentResponse.fromEntity(comment);
        assertThat(response.getUsername()).isEqualTo("chefana");
        assertThat(response.getCommentText()).isEqualTo("Excelente");
        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);

        RecipeShare share = new RecipeShare();
        share.setRecipe(recipe);
        share.setUser(user);
        share.setPlatform("sitio");
        share.setSharedAt(createdAt);
        assertThat(share.getRecipe()).isSameAs(recipe);
        assertThat(share.getUser()).isSameAs(user);
        assertThat(share.getPlatform()).isEqualTo("sitio");
        assertThat(share.getSharedAt()).isEqualTo(createdAt);

        RecipeShareResponse shareResponse = new RecipeShareResponse("ok", "sitio", "http://localhost/recetas/detalle/1");
        assertThat(shareResponse.getMessage()).isEqualTo("ok");
        assertThat(shareResponse.getPlatform()).isEqualTo("sitio");
        assertThat(shareResponse.getShareUrl()).contains("detalle/1");
    }

    static Recipe validRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Pastel de choclo");
        recipe.setCuisineType("Chilena");
        recipe.setCountryOfOrigin("Chile");
        recipe.setDifficulty("Media");
        recipe.setSummary("Receta tradicional");
        recipe.setInstructions("Cocinar el relleno. Cubrir con pastelera.");
        recipe.setCookTimeMinutes(60);
        recipe.setPopularityScore(3);
        recipe.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        recipe.setIngredients(List.of("Choclo", "Carne"));
        recipe.setPhotos(List.of("https://example.com/pastel.jpg"));
        recipe.setVideos(List.of("https://youtube.com/watch?v=abc"));
        return recipe;
    }
}
