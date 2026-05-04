package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FormsAndViewsTest {

    @Test
    void addCommentFormDefaultsRatingToFiveWhenMissing() {
        AddCommentForm form = new AddCommentForm();
        form.setCommentText("Me gustó");

        assertThat(form.getCommentText()).isEqualTo("Me gustó");
        assertThat(form.getRating()).isEqualTo(5);

        form.setRating(3);
        assertThat(form.getRating()).isEqualTo(3);
    }

    @Test
    void addMediaAndShareFormsExposeSafeDefaults() {
        AddMediaForm mediaForm = new AddMediaForm();
        mediaForm.setUrl("https://example.com/foto.jpg");
        assertThat(mediaForm.getUrl()).isEqualTo("https://example.com/foto.jpg");

        ShareForm shareForm = new ShareForm();
        assertThat(shareForm.getPlatform()).isEqualTo("sitio");
        shareForm.setPlatform(" ");
        assertThat(shareForm.getPlatform()).isEqualTo("sitio");
        shareForm.setPlatform("whatsapp");
        assertThat(shareForm.getPlatform()).isEqualTo("whatsapp");
    }

    @Test
    void authenticatedUserReportsAdminRoleAndJwt() {
        AuthenticatedUser admin = new AuthenticatedUser("admin", "Bearer token", "ROLE_ADMIN");
        AuthenticatedUser user = new AuthenticatedUser("chefana", "Bearer token2", "ROLE_USER");

        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getJwtToken()).isEqualTo("Bearer token");
        assertThat(admin.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(admin.isAdmin()).isTrue();
        assertThat(user.isAdmin()).isFalse();
    }

    @Test
    void commentViewFormatsDateAndStarsSafely() {
        CommentView comment = new CommentView();
        comment.setId(1L);
        comment.setUsername("chefana");
        comment.setCommentText("Excelente");
        comment.setRating(6);
        comment.setCreatedAt(LocalDateTime.of(2026, 5, 1, 13, 45));

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getUsername()).isEqualTo("chefana");
        assertThat(comment.getCommentText()).isEqualTo("Excelente");
        assertThat(comment.getFormattedCreatedAt()).isEqualTo("01-05-2026 13:45");
        assertThat(comment.getStars()).isEqualTo("★★★★★");

        comment.setRating(null);
        comment.setCreatedAt(null);
        assertThat(comment.getRating()).isZero();
        assertThat(comment.getFormattedCreatedAt()).isEmpty();
        assertThat(comment.getStars()).isEqualTo("☆☆☆☆☆");
    }

    @Test
    void loginRequestAndResponseExposeFields() {
        FrontendLoginRequest request = new FrontendLoginRequest("chefana", "1234");
        assertThat(request.getUsername()).isEqualTo("chefana");
        assertThat(request.getPassword()).isEqualTo("1234");

        request.setUsername("admin");
        request.setPassword("password");
        assertThat(request.getUsername()).isEqualTo("admin");
        assertThat(request.getPassword()).isEqualTo("password");

        FrontendLoginResponse response = new FrontendLoginResponse();
        response.setToken("Bearer token");
        response.setUsername("chefana");
        response.setRole("ROLE_USER");
        assertThat(response.getToken()).isEqualTo("Bearer token");
        assertThat(response.getUsername()).isEqualTo("chefana");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void recipeViewUsesDefaultCollectionsImageAndInstructionSteps() {
        RecipeView recipe = new RecipeView();
        recipe.setId(1L);
        recipe.setName("Cazuela");
        recipe.setSummary("Tradicional");
        recipe.setCuisineType("Chilena");
        recipe.setCountryOfOrigin("Chile");
        recipe.setDifficulty("Media");
        recipe.setCookTimeMinutes(70);
        recipe.setPopularityScore(10);
        recipe.setInstructions("Cortar verduras. Hervir con carne");

        assertThat(recipe.getId()).isEqualTo(1L);
        assertThat(recipe.getName()).isEqualTo("Cazuela");
        assertThat(recipe.getSummary()).isEqualTo("Tradicional");
        assertThat(recipe.getCuisineType()).isEqualTo("Chilena");
        assertThat(recipe.getCountryOfOrigin()).isEqualTo("Chile");
        assertThat(recipe.getDifficulty()).isEqualTo("Media");
        assertThat(recipe.getCookTimeMinutes()).isEqualTo(70);
        assertThat(recipe.getPopularityScore()).isEqualTo(10);
        assertThat(recipe.getIngredients()).isEmpty();
        assertThat(recipe.getPhotos()).isEmpty();
        assertThat(recipe.getVideos()).isEmpty();
        assertThat(recipe.getImageUrl()).contains("images.unsplash.com");
        assertThat(recipe.getInstructionSteps()).containsExactly("Cortar verduras.", "Hervir con carne.");

        recipe.setIngredients(List.of("Carne"));
        recipe.setPhotos(List.of("https://example.com/cazuela.jpg"));
        recipe.setVideos(List.of("https://youtube.com/watch?v=1"));
        assertThat(recipe.getIngredients()).containsExactly("Carne");
        assertThat(recipe.getImageUrl()).isEqualTo("https://example.com/cazuela.jpg");
        assertThat(recipe.getVideos()).containsExactly("https://youtube.com/watch?v=1");

        recipe.setInstructions(" ");
        assertThat(recipe.getInstructionSteps()).isEmpty();
    }

    @Test
    void recipeShareResponseViewExposesFields() {
        RecipeShareResponseView response = new RecipeShareResponseView();
        response.setMessage("ok");
        response.setPlatform("sitio");
        response.setShareUrl("http://localhost/recetas/detalle/1");

        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getPlatform()).isEqualTo("sitio");
        assertThat(response.getShareUrl()).contains("detalle/1");
    }
}
