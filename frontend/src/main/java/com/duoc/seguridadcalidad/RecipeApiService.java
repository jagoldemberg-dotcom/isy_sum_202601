package com.duoc.seguridadcalidad;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class RecipeApiService {

    private final RestClient backendRestClient;

    public RecipeApiService(RestClient backendRestClient) {
        this.backendRestClient = backendRestClient;
    }

    public List<RecipeView> latestRecipes() {
        return readPublicList("/recipes/latest");
    }

    public List<RecipeView> popularRecipes() {
        return readPublicList("/recipes/popular");
    }

    public List<RecipeView> search(String name,
                                   String cuisineType,
                                   String ingredient,
                                   String countryOfOrigin,
                                   String difficulty) {
        try {
            return toRecipeList(backendRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/recipes/search")
                            .queryParamIfPresent("name", optionalValue(name))
                            .queryParamIfPresent("cuisineType", optionalValue(cuisineType))
                            .queryParamIfPresent("ingredient", optionalValue(ingredient))
                            .queryParamIfPresent("countryOfOrigin", optionalValue(countryOfOrigin))
                            .queryParamIfPresent("difficulty", optionalValue(difficulty))
                            .build())
                    .retrieve()
                    .body(RecipeView[].class));
        } catch (RestClientException exception) {
            return List.of();
        }
    }

    public RecipeView findById(Long id, String token) {
        try {
            RecipeView recipe = backendRestClient.get()
                    .uri("/recipes/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ResponseStatusException(NOT_FOUND, "Receta no encontrada o sin acceso");
                    })
                    .body(RecipeView.class);

            if (recipe == null) {
                throw new ResponseStatusException(NOT_FOUND, "Receta no encontrada");
            }
            return recipe;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "No fue posible consultar el backend", exception);
        }
    }

    public List<CommentView> getComments(Long recipeId, String token) {
        try {
            return toCommentList(backendRestClient.get()
                    .uri("/recipes/{id}/comments", recipeId)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .body(CommentView[].class));
        } catch (RestClientException exception) {
            return List.of();
        }
    }

    public void addComment(Long recipeId, AddCommentForm form, String token) {
        executeSecuredPost(
                "/recipes/{id}/comments",
                recipeId,
                token,
                """
                {
                  "commentText": "%s",
                  "rating": %s
                }
                """.formatted(escapeJson(form.getCommentText()), form.getRating())
        );
    }

    public void addPhoto(Long recipeId, AddMediaForm form, String token) {
        executeSecuredPost(
                "/recipes/{id}/photos",
                recipeId,
                token,
                """
                {
                  "photoUrl": "%s"
                }
                """.formatted(escapeJson(form.getUrl()))
        );
    }

    public void addVideo(Long recipeId, AddMediaForm form, String token) {
        executeSecuredPost(
                "/recipes/{id}/videos",
                recipeId,
                token,
                """
                {
                  "videoUrl": "%s"
                }
                """.formatted(escapeJson(form.getUrl()))
        );
    }

    public RecipeShareResponseView shareRecipe(Long recipeId, ShareForm form, String token) {
        try {
            RecipeShareResponseView response = backendRestClient.post()
                    .uri("/recipes/{id}/shares", recipeId)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .body("""
                          {
                            "platform": "%s"
                          }
                          """.formatted(escapeJson(form.getPlatform())))
                    .retrieve()
                    .body(RecipeShareResponseView.class);

            if (response == null) {
                throw new ResponseStatusException(BAD_GATEWAY, "No fue posible compartir la receta");
            }
            return response;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "No fue posible compartir la receta", exception);
        }
    }

    private void executeSecuredPost(String path, Long recipeId, String token, String body) {
        try {
            backendRestClient.post()
                    .uri(path, recipeId)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "No fue posible registrar la acción", exception);
        }
    }

    private List<RecipeView> readPublicList(String path) {
        try {
            return toRecipeList(backendRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(RecipeView[].class));
        } catch (RestClientException exception) {
            return List.of();
        }
    }

    private List<RecipeView> toRecipeList(RecipeView[] response) {
        return response == null ? List.of() : Arrays.asList(response);
    }

    private List<CommentView> toCommentList(CommentView[] response) {
        return response == null ? List.of() : Arrays.asList(response);
    }

    private java.util.Optional<String> optionalValue(String value) {
        return value == null || value.isBlank()
                ? java.util.Optional.empty()
                : java.util.Optional.of(value);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ");
    }
}
