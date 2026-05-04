package com.duoc.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackendExtraCoverageTest {

    private static final String SECRET = "SeguridadCalidadJWTSecretKey2026SeguridadCalidadJWT!";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recipeControllerReadsLatestPopularAndCommentsFromRepositories() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        RecipeCommentRepository comments = mock(RecipeCommentRepository.class);
        Recipe recipe = ServiceAndControllerTest.recipe(1L, "Empanadas", "Chilena", "Chile", "Media", List.of("Pino"), 7);
        User user = new User("chefana", "chefana@example.com", "hash", Constants.USER_ROLE);

        RecipeComment comment = new RecipeComment();
        comment.setRecipe(recipe);
        comment.setUser(user);
        comment.setCommentText("Quedó muy buena");
        comment.setRating(5);
        comment.setCreatedAt(LocalDateTime.of(2026, 5, 1, 18, 0));

        when(recipes.findTop3ByOrderByCreatedAtDesc()).thenReturn(List.of(recipe));
        when(recipes.findTop3ByOrderByPopularityScoreDesc()).thenReturn(List.of(recipe));
        when(recipes.existsById(1L)).thenReturn(true);
        when(comments.findByRecipeIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(comment));

        RecipeController controller = new RecipeController(recipes, mock(UserRepository.class), comments, mock(RecipeShareRepository.class), "http://localhost/recetas/");

        assertThat(controller.latestRecipes()).containsExactly(recipe);
        assertThat(controller.popularRecipes()).containsExactly(recipe);

        List<RecipeCommentResponse> responses = controller.getComments(1L);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isNull();
        assertThat(responses.get(0).getUsername()).isEqualTo("chefana");
        verify(comments).findByRecipeIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void recipeControllerSearchCoversEveryFilterAndMissingValues() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        Recipe matchingRecipe = ServiceAndControllerTest.recipe(1L, "Pastel de choclo", "Chilena", "Chile", "Media", List.of("Choclo", "Carne"), 3);
        matchingRecipe.setCreatedAt(LocalDateTime.of(2026, 5, 2, 10, 0));

        Recipe recipeWithNullCuisine = ServiceAndControllerTest.recipe(2L, "Tarta", null, "Argentina", "Fácil", List.of("Harina"), 1);
        recipeWithNullCuisine.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(recipes.findAll()).thenReturn(List.of(matchingRecipe, recipeWithNullCuisine));
        RecipeController controller = new RecipeController(recipes, mock(UserRepository.class), mock(RecipeCommentRepository.class), mock(RecipeShareRepository.class), "http://localhost/recetas");

        assertThat(controller.searchRecipes("pastel", "chil", "chi", "med", "choc")).containsExactly(matchingRecipe);
        assertThat(controller.searchRecipes(null, "vegana", null, null, null)).isEmpty();
        assertThat(controller.searchRecipes(null, null, null, null, "ingrediente inexistente")).isEmpty();
    }

    @Test
    void recipeControllerSharesRecipeInEverySupportedPlatform() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        UserRepository users = mock(UserRepository.class);
        RecipeShareRepository shares = mock(RecipeShareRepository.class);
        Recipe recipe = ServiceAndControllerTest.recipe(10L, "Caldillo", "Chilena", "Chile", "Fácil", List.of("Pescado"), 0);
        User user = new User("chefana", "chefana@example.com", "hash", Constants.USER_ROLE);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("chefana");
        when(recipes.findById(10L)).thenReturn(Optional.of(recipe));
        when(users.findByUsernameIgnoreCase("chefana")).thenReturn(Optional.of(user));
        when(shares.save(any(RecipeShare.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipes.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeController controller = new RecipeController(recipes, users, mock(RecipeCommentRepository.class), shares, "http://localhost/recetas/");

        ShareRecipeRequest whatsapp = new ShareRecipeRequest();
        whatsapp.setPlatform("whatsapp");
        ShareRecipeRequest x = new ShareRecipeRequest();
        x.setPlatform("x");
        ShareRecipeRequest sitio = new ShareRecipeRequest();
        sitio.setPlatform("sitio");

        assertThat(controller.shareRecipe(10L, whatsapp, authentication).getBody().getShareUrl()).contains("wa.me");
        assertThat(controller.shareRecipe(10L, x, authentication).getBody().getShareUrl()).contains("twitter.com/intent/tweet");
        assertThat(controller.shareRecipe(10L, sitio, authentication).getBody().getShareUrl()).isEqualTo("http://localhost/recetas/detalle/10");
        assertThat(recipe.getPopularityScore()).isEqualTo(6);
    }

    @Test
    void recipeControllerRejectsMissingBlankOrUnknownAuthenticatedUser() {
        RecipeRepository recipes = mock(RecipeRepository.class);
        UserRepository users = mock(UserRepository.class);
        Recipe recipe = ServiceAndControllerTest.recipe(1L, "Cazuela", "Chilena", "Chile", "Media", List.of("Carne"), 0);
        when(recipes.findById(1L)).thenReturn(Optional.of(recipe));

        RecipeController controller = new RecipeController(recipes, users, mock(RecipeCommentRepository.class), mock(RecipeShareRepository.class), "http://localhost/recetas");
        CreateCommentRequest request = new CreateCommentRequest();
        request.setCommentText("Comentario válido");
        request.setRating(4);

        Authentication blankAuthentication = mock(Authentication.class);
        when(blankAuthentication.getName()).thenReturn(" ");
        Authentication unknownAuthentication = mock(Authentication.class);
        when(unknownAuthentication.getName()).thenReturn("desconocido");
        when(users.findByUsernameIgnoreCase("desconocido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.addComment(1L, request, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Debe iniciar sesión");
        assertThatThrownBy(() -> controller.addComment(1L, request, blankAuthentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Debe iniciar sesión");
        assertThatThrownBy(() -> controller.addComment(1L, request, unknownAuthentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuario autenticado no válido");
    }

    @Test
    void authorizationFilterContinuesWhenHeaderIsMissingOrIsNotBearer() throws ServletException, IOException {
        JWTAuthorizationFilter filter = new JWTAuthorizationFilter(new JWTAuthenticationConfig(SECRET, 60000));

        MockHttpServletRequest missingHeader = new MockHttpServletRequest("POST", "/recipes/1/comments");
        missingHeader.setServletPath("/recipes/1/comments");
        MockHttpServletResponse missingResponse = new MockHttpServletResponse();
        filter.doFilter(missingHeader, missingResponse, new MockFilterChain());

        MockHttpServletRequest wrongHeader = new MockHttpServletRequest("POST", "/recipes/1/comments");
        wrongHeader.setServletPath("/recipes/1/comments");
        wrongHeader.addHeader(Constants.AUTHORIZATION_HEADER, "Token abc");
        MockHttpServletResponse wrongResponse = new MockHttpServletResponse();
        filter.doFilter(wrongHeader, wrongResponse, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(missingResponse.getStatus()).isEqualTo(200);
        assertThat(wrongResponse.getStatus()).isEqualTo(200);
    }

    @Test
    void loginConvertsMissingUserAndIllegalArgumentIntoUnauthorized() {
        JWTAuthenticationConfig jwtConfig = mock(JWTAuthenticationConfig.class);
        MyUserDetailsService userDetailsService = mock(MyUserDetailsService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        LoginController controller = new LoginController(jwtConfig, userDetailsService, passwordEncoder);

        LoginRequest request = new LoginRequest();
        request.setUsername("nadie");
        request.setPassword("clave");
        when(userDetailsService.loadUserByUsername("nadie")).thenThrow(new UsernameNotFoundException("no existe"));
        assertThatThrownBy(() -> controller.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Credenciales inválidas");

        LoginRequest badRequest = new LoginRequest();
        badRequest.setUsername("ilegal");
        badRequest.setPassword("clave");
        when(userDetailsService.loadUserByUsername("ilegal")).thenThrow(new IllegalArgumentException("dato inválido"));
        assertThatThrownBy(() -> controller.login(badRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    void apiExceptionHandlerReportsValidationErrorsByField() throws Exception {
        LoginRequest request = new LoginRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "loginRequest");
        bindingResult.addError(new FieldError("loginRequest", "username", "El usuario es obligatorio"));
        bindingResult.addError(new FieldError("loginRequest", "password", "La contraseña es obligatoria"));

        Method method = BackendExtraCoverageTest.class.getDeclaredMethod("dummyLogin", LoginRequest.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = new ApiExceptionHandler().handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("message", "La solicitud contiene datos inválidos");
        assertThat((Map<String, String>) response.getBody().get("errors"))
                .containsEntry("username", "El usuario es obligatorio")
                .containsEntry("password", "La contraseña es obligatoria");
    }

    @Test
    void modelObjectsExposeRemainingSimpleGetters() {
        Recipe recipe = ValidationAndModelTest.validRecipe();
        assertThat(recipe.getSummary()).isEqualTo("Receta tradicional");
        assertThat(recipe.getInstructions()).contains("Cocinar");
        assertThat(recipe.getCookTimeMinutes()).isEqualTo(60);
        assertThat(recipe.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 0));
        assertThat(recipe.getPhotos()).containsExactly("https://example.com/pastel.jpg");
        assertThat(recipe.getVideos()).containsExactly("https://youtube.com/watch?v=abc");

        RecipeComment comment = new RecipeComment();
        assertThat(comment.getId()).isNull();
        assertThat(comment.getCreatedAt()).isNotNull();

        RecipeShare share = new RecipeShare();
        assertThat(share.getId()).isNull();
        assertThat(share.getSharedAt()).isNotNull();
    }

    @SuppressWarnings("unused")
    private static void dummyLogin(@Valid LoginRequest request) {
    }
}
