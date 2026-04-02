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
            return toList(backendRestClient.get()
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

    private List<RecipeView> readPublicList(String path) {
        try {
            return toList(backendRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(RecipeView[].class));
        } catch (RestClientException exception) {
            return List.of();
        }
    }

    private List<RecipeView> toList(RecipeView[] response) {
        return response == null ? List.of() : Arrays.asList(response);
    }

    private java.util.Optional<String> optionalValue(String value) {
        return value == null || value.isBlank()
                ? java.util.Optional.empty()
                : java.util.Optional.of(value);
    }
}