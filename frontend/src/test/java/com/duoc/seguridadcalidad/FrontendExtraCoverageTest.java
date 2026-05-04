package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FrontendExtraCoverageTest {

    @Test
    void authenticationProviderRejectsIncompleteBackendResponseAndUnsupportedTypes() {
        RestClientFixture fixture = restClientFixture();
        fixture.server.expect(once(), requestTo("http://backend/login"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        BackendAuthenticationProvider provider = new BackendAuthenticationProvider(fixture.client);

        assertThatThrownBy(() -> provider.authenticate(new UsernamePasswordAuthenticationToken("chefana", null)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("No fue posible validar");
        assertThat(provider.supports(TestingAuthenticationToken.class)).isFalse();
        fixture.server.verify();
    }

    @Test
    void recipeApiServiceReturnsEmptyListsWhenBackendSendsNullOrFails() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/latest"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));
        fixture.server.expect(once(), requestTo("http://backend/recipes/popular"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        fixture.server.expect(once(), requestTo("http://backend/recipes/7/comments"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));
        fixture.server.expect(once(), requestTo("http://backend/recipes/8/comments"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThat(service.latestRecipes()).isEmpty();
        assertThat(service.popularRecipes()).isEmpty();
        assertThat(service.getComments(7L, "Bearer token")).isEmpty();
        assertThat(service.getComments(8L, "Bearer token")).isEmpty();
        fixture.server.verify();
    }

    @Test
    void recipeApiServiceSearchSendsEveryFilterWhenPresent() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo(allOf(
                        containsString("http://backend/recipes/search"),
                        containsString("name=ca"),
                        containsString("cuisineType=Chilena"),
                        containsString("ingredient=pollo"),
                        containsString("countryOfOrigin=Chile"),
                        containsString("difficulty=Facil")
                )))
                .andRespond(withSuccess("[{\"id\":4,\"name\":\"Cazuela de pollo\"}]", MediaType.APPLICATION_JSON));

        assertThat(service.search("ca", "Chilena", "pollo", "Chile", "Facil"))
                .extracting(RecipeView::getName)
                .containsExactly("Cazuela de pollo");
        fixture.server.verify();
    }

    @Test
    void recipeApiServiceFindMapsNullAndTransportErrors() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/50"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));
        fixture.server.expect(once(), requestTo("http://backend/recipes/51"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> service.findById(50L, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Receta no encontrada");
        assertThatThrownBy(() -> service.findById(51L, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No fue posible consultar");
        fixture.server.verify();
    }

    @Test
    void recipeApiServiceEscapesNullAndSpecialCharactersInSecuredBodies() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/3/comments"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(allOf(containsString("\"commentText\": \"\""), containsString("\"rating\": 5"))))
                .andRespond(withSuccess());
        fixture.server.expect(once(), requestTo("http://backend/recipes/3/videos"))
                .andExpect(content().string(containsString("https://videos.example.com/linea ruta")))
                .andRespond(withSuccess());

        AddCommentForm comment = new AddCommentForm();
        AddMediaForm video = new AddMediaForm();
        video.setUrl("https://videos.example.com/linea\nruta");

        service.addComment(3L, comment, "Bearer token");
        service.addVideo(3L, video, "Bearer token");
        fixture.server.verify();
    }

    @Test
    void recipeApiServiceShareRejectsNullBackendBody() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/4/shares"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.shareRecipe(4L, new ShareForm(), "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No fue posible compartir");
        fixture.server.verify();
    }

    @Test
    void pageControllerCoversAnonymousBranchesEmptyRatingsAndShareFallbacks() {
        RecipeApiService service = mock(RecipeApiService.class);
        PageController controller = new PageController(service);
        Model model = new ExtendedModelMap();
        when(service.findById(1L, "Bearer token")).thenReturn(recipe("Charquicán"));
        when(service.getComments(1L, "Bearer token")).thenReturn(List.of());

        String detailView = controller.detail(1L, null, "", model, auth("chefana", "ROLE_USER"));

        assertThat(detailView).isEqualTo("detalle");
        assertThat(model.asMap()).containsEntry("averageRating", 0.0);
        assertThat(model.asMap()).containsEntry("commentCount", 0);
        assertThat(model.asMap()).containsEntry("admin", false);
        assertThat(model.asMap()).containsEntry("ok", "");
        assertThat(model.asMap()).containsEntry("error", "");

        assertThat(controller.addComment(2L, new AddCommentForm(), null)).contains("error=No%20se%20pudo");
        assertThat(controller.addPhoto(2L, new AddMediaForm(), null)).contains("error=No%20se%20pudo");
        assertThat(controller.addVideo(2L, new AddMediaForm(), null)).contains("error=No%20se%20pudo");

        RecipeShareResponseView nullUrlResponse = new RecipeShareResponseView();
        RecipeShareResponseView nonHttpResponse = new RecipeShareResponseView();
        nonHttpResponse.setShareUrl("recetas/detalle/3");
        ShareForm externalForm = new ShareForm();
        externalForm.setPlatform("x");
        when(service.shareRecipe(eq(3L), any(ShareForm.class), eq("Bearer token"))).thenReturn(nullUrlResponse);
        when(service.shareRecipe(eq(4L), any(ShareForm.class), eq("Bearer token"))).thenReturn(nonHttpResponse);

        assertThat(controller.shareRecipe(3L, externalForm, auth("chefana", "ROLE_USER"))).contains("ok=Receta");
        assertThat(controller.shareRecipe(4L, externalForm, auth("chefana", "ROLE_USER"))).contains("ok=Receta");
    }

    @Test
    void loginControllerShowsLoginForAuthenticatedNonApplicationPrincipal() {
        LoginController controller = new LoginController();
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated("usuario", "N/A", List.of());

        assertThat(controller.login(authentication)).isEqualTo("login");
    }

    @Test
    void viewHelpersCoverBoundaryValues() {
        ShareForm shareForm = new ShareForm();
        shareForm.setPlatform(null);
        assertThat(shareForm.getPlatform()).isEqualTo("sitio");

        CommentView negative = new CommentView();
        negative.setRating(-2);
        assertThat(negative.getStars()).isEqualTo("☆☆☆☆☆");

        RecipeView recipe = new RecipeView();
        recipe.setInstructions(null);
        assertThat(recipe.getInstructionSteps()).isEmpty();
        recipe.setInstructions("Servir caliente.");
        assertThat(recipe.getInstructionSteps()).containsExactly("Servir caliente.");
    }

    private RestClientFixture restClientFixture() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://backend")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new RestClientFixture(builder.build(), server);
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

    private record RestClientFixture(RestClient client, MockRestServiceServer server) {
    }
}
