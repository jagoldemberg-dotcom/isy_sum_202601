package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BackendClientAndServiceTest {

    @Test
    void backendClientConfigCreatesRestClient() {
        RestClient client = new BackendClientConfig().backendRestClient("http://backend:8080");
        assertThat(client).isNotNull();
    }

    @Test
    void authenticationProviderAuthenticatesAgainstBackend() {
        RestClientFixture fixture = restClientFixture();
        fixture.server.expect(once(), requestTo("http://backend/login"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"token":"Bearer token","username":"chefana","role":"ROLE_USER"}
                        """, MediaType.APPLICATION_JSON));
        BackendAuthenticationProvider provider = new BackendAuthenticationProvider(fixture.client);

        Authentication authentication = provider.authenticate(new UsernamePasswordAuthenticationToken("chefana", "1234"));

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedUser.class);
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        assertThat(principal.getUsername()).isEqualTo("chefana");
        assertThat(principal.getJwtToken()).isEqualTo("Bearer token");
        assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
        fixture.server.verify();
    }

    @Test
    void authenticationProviderRejectsMissingOrFailedBackendResponse() {
        RestClientFixture fixture = restClientFixture();
        fixture.server.expect(once(), requestTo("http://backend/login"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        BackendAuthenticationProvider provider = new BackendAuthenticationProvider(fixture.client);

        assertThatThrownBy(() -> provider.authenticate(new UsernamePasswordAuthenticationToken("chefana", "bad")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void recipeApiServiceReadsPublicListsAndSearch() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/latest"))
                .andRespond(withSuccess("[{\"id\":1,\"name\":\"Cazuela\"}]", MediaType.APPLICATION_JSON));
        fixture.server.expect(once(), requestTo("http://backend/recipes/popular"))
                .andRespond(withSuccess("[{\"id\":2,\"name\":\"Sopaipillas\"}]", MediaType.APPLICATION_JSON));
        fixture.server.expect(once(), requestTo(containsString("http://backend/recipes/search")))
                .andExpect(requestTo(containsString("name=ca")))
                .andRespond(withSuccess("[{\"id\":3,\"name\":\"Carbonada\"}]", MediaType.APPLICATION_JSON));

        assertThat(service.latestRecipes()).extracting(RecipeView::getName).containsExactly("Cazuela");
        assertThat(service.popularRecipes()).extracting(RecipeView::getName).containsExactly("Sopaipillas");
        assertThat(service.search("ca", "", null, null, null)).extracting(RecipeView::getName).containsExactly("Carbonada");
        fixture.server.verify();
    }

    @Test
    void recipeApiServiceReturnsEmptyListsOnPublicBackendFailure() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/latest"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        fixture.server.expect(once(), requestTo(containsString("http://backend/recipes/search")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThat(service.latestRecipes()).isEmpty();
        assertThat(service.search(null, null, null, null, null)).isEmpty();
    }

    @Test
    void recipeApiServiceFindsRecipeAndCommentsWithToken() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/1"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Cazuela\"}", MediaType.APPLICATION_JSON));
        fixture.server.expect(once(), requestTo("http://backend/recipes/1/comments"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("[{\"id\":10,\"username\":\"chefana\",\"commentText\":\"ok\",\"rating\":5}]", MediaType.APPLICATION_JSON));

        assertThat(service.findById(1L, "Bearer token").getName()).isEqualTo("Cazuela");
        assertThat(service.getComments(1L, "Bearer token")).extracting(CommentView::getUsername).containsExactly("chefana");
        fixture.server.verify();
    }

    @Test
    void recipeApiServiceMapsFindErrorsToStatusExceptions() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> service.findById(99L, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Receta no encontrada");
    }

    @Test
    void recipeApiServiceExecutesSecuredPostActions() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/1/comments"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
        fixture.server.expect(once(), requestTo("http://backend/recipes/1/photos"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess());
        fixture.server.expect(once(), requestTo("http://backend/recipes/1/videos"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess());

        AddCommentForm comment = new AddCommentForm();
        comment.setCommentText("Texto con \"comillas\" y salto\nlinea");
        comment.setRating(4);
        AddMediaForm media = new AddMediaForm();
        media.setUrl("https://example.com/recurso.jpg");
        service.addComment(1L, comment, "Bearer token");
        service.addPhoto(1L, media, "Bearer token");
        service.addVideo(1L, media, "Bearer token");
        fixture.server.verify();
    }

    @Test
    void recipeApiServiceThrowsWhenSecuredPostFails() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/1/photos"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
        AddMediaForm media = new AddMediaForm();
        media.setUrl("invalid");

        assertThatThrownBy(() -> service.addPhoto(1L, media, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No fue posible registrar");
    }

    @Test
    void recipeApiServiceSharesRecipesAndRejectsNullResponse() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/1/shares"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("{\"message\":\"ok\",\"platform\":\"sitio\",\"shareUrl\":\"http://localhost/recetas/detalle/1\"}", MediaType.APPLICATION_JSON));
        ShareForm form = new ShareForm();

        RecipeShareResponseView response = service.shareRecipe(1L, form, "Bearer token");

        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getShareUrl()).contains("detalle/1");
        fixture.server.verify();
    }

    @Test
    void recipeApiServiceThrowsWhenShareFails() {
        RestClientFixture fixture = restClientFixture();
        RecipeApiService service = new RecipeApiService(fixture.client);
        fixture.server.expect(once(), requestTo("http://backend/recipes/1/shares"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> service.shareRecipe(1L, new ShareForm(), "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No fue posible compartir");
    }

    private RestClientFixture restClientFixture() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://backend")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new RestClientFixture(builder.build(), server);
    }

    private record RestClientFixture(RestClient client, MockRestServiceServer server) {
    }
}
