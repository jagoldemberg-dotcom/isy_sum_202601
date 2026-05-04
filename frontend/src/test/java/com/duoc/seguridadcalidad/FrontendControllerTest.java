package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FrontendControllerTest {

    @Test
    void loginControllerRedirectsAuthenticatedUsers() {
        LoginController controller = new LoginController();
        Authentication authentication = auth("chefana", "ROLE_USER");

        assertThat(controller.login(authentication)).isEqualTo("redirect:/");
        assertThat(controller.login(null)).isEqualTo("login");
    }

    @Test
    void pageControllerHomeLoadsPublicRecipeSections() {
        RecipeApiService service = mock(RecipeApiService.class);
        when(service.latestRecipes()).thenReturn(List.of(recipe("Última")));
        when(service.popularRecipes()).thenReturn(List.of(recipe("Popular")));
        PageController controller = new PageController(service);
        Model model = new ExtendedModelMap();

        String view = controller.home(model, auth("admin", "ROLE_ADMIN"));

        assertThat(view).isEqualTo("home");
        assertThat(model.asMap()).containsEntry("pageTitle", "Inicio");
        assertThat(model.asMap()).containsEntry("authenticated", true);
        assertThat(model.asMap()).containsEntry("admin", true);
        assertThat((List<?>) model.asMap().get("latestRecipes")).hasSize(1);
        assertThat((List<?>) model.asMap().get("popularRecipes")).hasSize(1);
    }

    @Test
    void pageControllerSearchPreservesFilters() {
        RecipeApiService service = mock(RecipeApiService.class);
        when(service.search("ca", "Chilena", "carne", "Chile", "Media")).thenReturn(List.of(recipe("Cazuela")));
        PageController controller = new PageController(service);
        Model model = new ExtendedModelMap();

        String view = controller.search("ca", "Chilena", "carne", "Chile", "Media", model, null);

        assertThat(view).isEqualTo("buscar");
        assertThat(model.asMap()).containsEntry("name", "ca");
        assertThat(model.asMap()).containsEntry("cuisineType", "Chilena");
        assertThat(model.asMap()).containsEntry("ingredient", "carne");
        assertThat(model.asMap()).containsEntry("countryOfOrigin", "Chile");
        assertThat(model.asMap()).containsEntry("difficulty", "Media");
        assertThat(model.asMap()).containsEntry("authenticated", false);
    }

    @Test
    void pageControllerDetailLoadsRecipeFormsAndRating() {
        RecipeApiService service = mock(RecipeApiService.class);
        when(service.findById(1L, "Bearer token")).thenReturn(recipe("Cazuela"));
        CommentView comment = new CommentView();
        comment.setRating(4);
        when(service.getComments(1L, "Bearer token")).thenReturn(List.of(comment));
        PageController controller = new PageController(service);
        Model model = new ExtendedModelMap();

        String view = controller.detail(1L, "ok", null, model, auth("chefana", "ROLE_USER"));

        assertThat(view).isEqualTo("detalle");
        assertThat(model.asMap()).containsEntry("averageRating", 4.0);
        assertThat(model.asMap()).containsEntry("commentCount", 1);
        assertThat(model.asMap().get("commentForm")).isInstanceOf(AddCommentForm.class);
        assertThat(model.asMap().get("photoForm")).isInstanceOf(AddMediaForm.class);
        assertThat(model.asMap().get("videoForm")).isInstanceOf(AddMediaForm.class);
        assertThat(model.asMap().get("shareForm")).isInstanceOf(ShareForm.class);
    }

    @Test
    void pageControllerPostActionsRedirectOnSuccessAndError() {
        RecipeApiService service = mock(RecipeApiService.class);
        PageController controller = new PageController(service);
        Authentication authentication = auth("chefana", "ROLE_USER");
        AddCommentForm comment = new AddCommentForm();
        AddMediaForm media = new AddMediaForm();

        assertThat(controller.addComment(1L, comment, authentication)).contains("ok=Comentario");
        assertThat(controller.addPhoto(1L, media, authentication)).contains("ok=Foto");
        assertThat(controller.addVideo(1L, media, authentication)).contains("ok=Video");

        doThrow(new RuntimeException("fail")).when(service).addComment(eq(2L), any(AddCommentForm.class), eq("Bearer token"));
        doThrow(new RuntimeException("fail")).when(service).addPhoto(eq(2L), any(AddMediaForm.class), eq("Bearer token"));
        doThrow(new RuntimeException("fail")).when(service).addVideo(eq(2L), any(AddMediaForm.class), eq("Bearer token"));

        assertThat(controller.addComment(2L, comment, authentication)).contains("error=No%20se%20pudo");
        assertThat(controller.addPhoto(2L, media, authentication)).contains("error=No%20se%20pudo");
        assertThat(controller.addVideo(2L, media, authentication)).contains("error=No%20se%20pudo");
    }

    @Test
    void pageControllerShareRedirectsToExternalPlatformWhenProvided() {
        RecipeApiService service = mock(RecipeApiService.class);
        RecipeShareResponseView response = new RecipeShareResponseView();
        response.setShareUrl("https://wa.me/?text=receta");
        when(service.shareRecipe(eq(1L), any(ShareForm.class), eq("Bearer token"))).thenReturn(response);
        PageController controller = new PageController(service);
        ShareForm form = new ShareForm();
        form.setPlatform("whatsapp");

        String redirect = controller.shareRecipe(1L, form, auth("chefana", "ROLE_USER"));

        assertThat(redirect).isEqualTo("redirect:https://wa.me/?text=receta");
        verify(service).shareRecipe(eq(1L), eq(form), eq("Bearer token"));
    }

    @Test
    void pageControllerShareFallsBackToDetailWhenSiteOrError() {
        RecipeApiService service = mock(RecipeApiService.class);
        RecipeShareResponseView response = new RecipeShareResponseView();
        response.setShareUrl("http://localhost/recetas/detalle/1");
        when(service.shareRecipe(eq(1L), any(ShareForm.class), eq("Bearer token"))).thenReturn(response);
        when(service.shareRecipe(eq(2L), any(ShareForm.class), eq("Bearer token"))).thenThrow(new RuntimeException("fail"));
        PageController controller = new PageController(service);
        ShareForm form = new ShareForm();

        assertThat(controller.shareRecipe(1L, form, auth("chefana", "ROLE_USER"))).contains("ok=Receta");
        assertThat(controller.shareRecipe(2L, form, auth("chefana", "ROLE_USER"))).contains("error=No%20se%20pudo");
    }

    private Authentication auth(String username, String role) {
        AuthenticatedUser principal = new AuthenticatedUser(username, "Bearer token", role);
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }

    private RecipeView recipe(String name) {
        RecipeView recipe = new RecipeView();
        recipe.setId(1L);
        recipe.setName(name);
        return recipe;
    }
}
