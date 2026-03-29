package com.duoc.backend;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeRepository recipeRepository;

    public RecipeController(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll().stream()
                .sorted(Comparator.comparing(Recipe::getCreatedAt).reversed())
                .toList();
    }

    @GetMapping("/latest")
    public List<Recipe> latestRecipes() {
        return recipeRepository.findTop3ByOrderByCreatedAtDesc();
    }

    @GetMapping("/popular")
    public List<Recipe> popularRecipes() {
        return recipeRepository.findTop3ByOrderByPopularityScoreDesc();
    }

    @GetMapping("/search")
    public List<Recipe> searchRecipes(@RequestParam(required = false) String name,
                                      @RequestParam(required = false) String cuisineType,
                                      @RequestParam(required = false) String countryOfOrigin,
                                      @RequestParam(required = false) String difficulty,
                                      @RequestParam(required = false) String ingredient) {
        return recipeRepository.findAll().stream()
                .filter(recipe -> contains(recipe.getName(), name))
                .filter(recipe -> contains(recipe.getCuisineType(), cuisineType))
                .filter(recipe -> contains(recipe.getCountryOfOrigin(), countryOfOrigin))
                .filter(recipe -> contains(recipe.getDifficulty(), difficulty))
                .filter(recipe -> ingredientMatches(recipe, ingredient))
                .sorted(Comparator.comparing(Recipe::getCreatedAt).reversed())
                .toList();
    }

    @GetMapping("/{id}")
    public Recipe getRecipeById(@PathVariable Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Receta no encontrada"));
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@Valid @RequestBody Recipe recipe) {
        recipe.setId(null);
        if (recipe.getCreatedAt() == null) {
            recipe.setCreatedAt(java.time.LocalDateTime.now());
        }
        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Receta no encontrada");
        }
        recipeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean contains(String source, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return source != null && source.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private boolean ingredientMatches(Recipe recipe, String ingredient) {
        if (ingredient == null || ingredient.isBlank()) {
            return true;
        }
        String normalized = ingredient.toLowerCase(Locale.ROOT);
        return recipe.getIngredients().stream().anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(normalized));
    }
}
