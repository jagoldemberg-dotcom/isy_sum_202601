package com.duoc.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository,
                      RecipeRepository recipeRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedRecipes();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(new User("chefana", "chefana@duoc.cl", passwordEncoder.encode("Receta123!"), Constants.USER_ROLE));
        userRepository.save(new User("martinchef", "martinchef@duoc.cl", passwordEncoder.encode("Cocina123!"), Constants.USER_ROLE));
        userRepository.save(new User("adminrecetas", "admin@duoc.cl", passwordEncoder.encode("Admin123!"), Constants.ADMIN_ROLE));
    }

    private void seedRecipes() {
        if (recipeRepository.count() > 0) {
            return;
        }

        recipeRepository.save(buildRecipe(
                "Pastel de choclo",
                "Chilena",
                "Chile",
                "Media",
                "Receta tradicional chilena con pino, pollo y cobertura de choclo.",
                "Prepara el pino. Cocina el pollo. Procesa el choclo. Arma la fuente y hornea hasta dorar.",
                55,
                96,
                LocalDateTime.now().minusDays(1),
                List.of("choclo", "carne molida", "pollo", "cebolla", "aceitunas", "huevo"),
                List.of("https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80")
        ));
        recipeRepository.save(buildRecipe(
                "Ceviche peruano",
                "Peruana",
                "Perú",
                "Fácil",
                "Preparación fresca de pescado marinado con limón y cebolla.",
                "Corta el pescado. Mezcla con limón, cebolla morada, cilantro y ají. Sirve de inmediato.",
                20,
                92,
                LocalDateTime.now().minusDays(2),
                List.of("pescado blanco", "limón", "cebolla morada", "cilantro", "ají"),
                List.of("https://images.unsplash.com/photo-1619894991209-9f9694be045a?auto=format&fit=crop&w=900&q=80")
        ));
        recipeRepository.save(buildRecipe(
                "Tacos de pollo",
                "Mexicana",
                "México",
                "Fácil",
                "Tacos rápidos con verduras frescas y salsa cremosa.",
                "Cocina el pollo, calienta tortillas, rellena y termina con salsa de yogur.",
                30,
                90,
                LocalDateTime.now().minusDays(3),
                List.of("tortillas", "pollo", "palta", "tomate", "lechuga", "yogur"),
                List.of("https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?auto=format&fit=crop&w=900&q=80")
        ));
        recipeRepository.save(buildRecipe(
                "Paella mediterránea",
                "Española",
                "España",
                "Difícil",
                "Arroz especiado con mariscos y verduras.",
                "Sofríe verduras, incorpora arroz y azafrán, añade caldo y termina con mariscos.",
                70,
                88,
                LocalDateTime.now().minusDays(4),
                List.of("arroz", "azafrán", "caldo", "camarones", "choritos", "pimentón"),
                List.of("https://images.unsplash.com/photo-1515443961218-a51367888e4b?auto=format&fit=crop&w=900&q=80")
        ));
    }

    private Recipe buildRecipe(String name,
                               String cuisineType,
                               String countryOfOrigin,
                               String difficulty,
                               String summary,
                               String instructions,
                               int cookTime,
                               int popularity,
                               LocalDateTime createdAt,
                               List<String> ingredients,
                               List<String> photos) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setCuisineType(cuisineType);
        recipe.setCountryOfOrigin(countryOfOrigin);
        recipe.setDifficulty(difficulty);
        recipe.setSummary(summary);
        recipe.setInstructions(instructions);
        recipe.setCookTimeMinutes(cookTime);
        recipe.setPopularityScore(popularity);
        recipe.setCreatedAt(createdAt);
        recipe.setIngredients(ingredients);
        recipe.setPhotos(photos);
        return recipe;
    }
}
