package com.duoc.backend;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final RecipeCommentRepository recipeCommentRepository;
    private final RecipeShareRepository recipeShareRepository;
    private final String publicBaseUrl;

    public RecipeController(RecipeRepository recipeRepository,
                            UserRepository userRepository,
                            RecipeCommentRepository recipeCommentRepository,
                            RecipeShareRepository recipeShareRepository,
                            @Value("${app.public-base-url}") String publicBaseUrl) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.recipeCommentRepository = recipeCommentRepository;
        this.recipeShareRepository = recipeShareRepository;
        this.publicBaseUrl = publicBaseUrl;
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
        return findRecipe(id);
    }

    @GetMapping("/{id}/comments")
    public List<RecipeCommentResponse> getComments(@PathVariable Long id) {
        ensureRecipeExists(id);
        return recipeCommentRepository.findByRecipeIdOrderByCreatedAtDesc(id).stream()
                .map(RecipeCommentResponse::fromEntity)
                .toList();
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@Valid @RequestBody Recipe recipe) {
        recipe.setId(null);
        if (recipe.getCreatedAt() == null) {
            recipe.setCreatedAt(LocalDateTime.now());
        }
        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<Recipe> addPhoto(@PathVariable Long id,
                                           @Valid @RequestBody AddPhotoRequest request) {
        Recipe recipe = findRecipe(id);
        recipe.getPhotos().add(request.getPhotoUrl().trim());
        return ResponseEntity.ok(recipeRepository.save(recipe));
    }

    @PostMapping("/{id}/videos")
    public ResponseEntity<Recipe> addVideo(@PathVariable Long id,
                                           @Valid @RequestBody AddVideoRequest request) {
        Recipe recipe = findRecipe(id);
        recipe.getVideos().add(request.getVideoUrl().trim());
        return ResponseEntity.ok(recipeRepository.save(recipe));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<RecipeCommentResponse> addComment(@PathVariable Long id,
                                                            @Valid @RequestBody CreateCommentRequest request,
                                                            Authentication authentication) {
        Recipe recipe = findRecipe(id);
        User user = findCurrentUser(authentication);

        RecipeComment comment = new RecipeComment();
        comment.setRecipe(recipe);
        comment.setUser(user);
        comment.setCommentText(request.getCommentText().trim());
        comment.setRating(request.getRating());
        comment.setCreatedAt(LocalDateTime.now());

        RecipeComment savedComment = recipeCommentRepository.save(comment);
        recipe.setPopularityScore(recipe.getPopularityScore() + request.getRating());
        recipeRepository.save(recipe);

        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeCommentResponse.fromEntity(savedComment));
    }

    @PostMapping("/{id}/shares")
    public ResponseEntity<RecipeShareResponse> shareRecipe(@PathVariable Long id,
                                                           @Valid @RequestBody ShareRecipeRequest request,
                                                           Authentication authentication) {
        Recipe recipe = findRecipe(id);
        User user = findCurrentUser(authentication);

        RecipeShare share = new RecipeShare();
        share.setRecipe(recipe);
        share.setUser(user);
        share.setPlatform(request.getPlatform().trim().toLowerCase(Locale.ROOT));
        share.setSharedAt(LocalDateTime.now());
        recipeShareRepository.save(share);

        recipe.setPopularityScore(recipe.getPopularityScore() + 2);
        recipeRepository.save(recipe);

        String shareUrl = buildShareUrl(share.getPlatform(), recipe);
        return ResponseEntity.ok(new RecipeShareResponse(
                "La receta fue compartida correctamente",
                share.getPlatform(),
                shareUrl
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Receta no encontrada");
        }
        recipeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Recipe findRecipe(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Receta no encontrada"));
    }

    private void ensureRecipeExists(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Receta no encontrada");
        }
    }

    private User findCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Debe iniciar sesión");
        }

        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Usuario autenticado no válido"));
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

    private String buildShareUrl(String platform, Recipe recipe) {
        String detailUrl = normalizedBaseUrl() + "/detalle/" + recipe.getId();
        String encodedUrl = URLEncoder.encode(detailUrl, StandardCharsets.UTF_8);
        String encodedText = URLEncoder.encode("Mira esta receta: " + recipe.getName(), StandardCharsets.UTF_8);

        return switch (platform) {
            case "facebook" -> "https://www.facebook.com/sharer/sharer.php?u=" + encodedUrl;
            case "whatsapp" -> "https://wa.me/?text=" + encodedText + "%20" + encodedUrl;
            case "x" -> "https://twitter.com/intent/tweet?text=" + encodedText + "&url=" + encodedUrl;
            default -> detailUrl;
        };
    }

    private String normalizedBaseUrl() {
        return publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
    }
}
