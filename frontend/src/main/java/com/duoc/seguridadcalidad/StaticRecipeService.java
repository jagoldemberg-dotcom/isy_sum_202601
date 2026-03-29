package com.duoc.seguridadcalidad;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class StaticRecipeService {

    private final List<RecipeView> recipes = List.of(
            new RecipeView(
                    1L,
                    "Pastel de choclo",
                    "Receta tradicional chilena con pino de carne, pollo, aceitunas y una cubierta cremosa de choclo.",
                    "Chilena",
                    "Chile",
                    "Media",
                    55,
                    List.of("choclo", "carne molida", "pollo", "cebolla", "aceitunas", "huevo"),
                    List.of(
                            "Sofríe la cebolla y prepara el pino con carne molida, comino y una pizca de ají de color.",
                            "Cocina el pollo aparte y desmenúzalo en trozos medianos.",
                            "Procesa el choclo cocido con albahaca hasta obtener una pasta espesa y cremosa.",
                            "Arma la fuente con pino, pollo, aceitunas y huevo duro. Cubre con la mezcla de choclo.",
                            "Hornea hasta dorar la superficie y sirve caliente."
                    ),
                    "https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80",
                    96,
                    6
            ),
            new RecipeView(
                    2L,
                    "Ceviche peruano",
                    "Preparación fresca de pescado marinado con limón, cebolla morada y cilantro.",
                    "Peruana",
                    "Perú",
                    "Fácil",
                    20,
                    List.of("pescado blanco", "limón", "cebolla morada", "cilantro", "ají", "sal"),
                    List.of(
                            "Corta el pescado en cubos parejos y mantenlo refrigerado.",
                            "Mezcla con jugo de limón recién exprimido, sal y ají al gusto.",
                            "Agrega cebolla morada en pluma y cilantro picado fino.",
                            "Deja reposar unos minutos y sirve inmediatamente."
                    ),
                    "https://images.unsplash.com/photo-1619894991209-9f9694be045a?auto=format&fit=crop&w=900&q=80",
                    92,
                    5
            ),
            new RecipeView(
                    3L,
                    "Paella mediterránea",
                    "Arroz especiado con mariscos, verduras y un toque de azafrán.",
                    "Española",
                    "España",
                    "Difícil",
                    70,
                    List.of("arroz", "azafrán", "caldo", "camarones", "choritos", "pimentón"),
                    List.of(
                            "Sofríe ajo, cebolla y pimentón hasta que estén tiernos.",
                            "Añade el arroz y el azafrán para nacararlo unos minutos.",
                            "Incorpora el caldo caliente y cocina sin revolver constantemente.",
                            "Agrega los mariscos cuando el arroz esté a media cocción.",
                            "Descansa la paella cinco minutos antes de servir."
                    ),
                    "https://images.unsplash.com/photo-1515443961218-a51367888e4b?auto=format&fit=crop&w=900&q=80",
                    88,
                    4
            ),
            new RecipeView(
                    4L,
                    "Ramen casero",
                    "Caldo reconfortante con fideos, huevo marinado y vegetales.",
                    "Japonesa",
                    "Japón",
                    "Media",
                    65,
                    List.of("fideos ramen", "caldo", "huevo", "cebollín", "setas", "salsa de soya"),
                    List.of(
                            "Prepara un caldo aromático con ajo, jengibre y soya.",
                            "Cuece los fideos aparte para mantener la textura adecuada.",
                            "Agrega setas y verduras al caldo unos minutos antes de servir.",
                            "Sirve con huevo partido a la mitad y cebollín fresco."
                    ),
                    "https://images.unsplash.com/photo-1557872943-16a5ac26437e?auto=format&fit=crop&w=900&q=80",
                    84,
                    3
            ),
            new RecipeView(
                    5L,
                    "Tacos de pollo",
                    "Tacos rápidos con pollo sazonado, verduras frescas y salsa cremosa.",
                    "Mexicana",
                    "México",
                    "Fácil",
                    30,
                    List.of("tortillas", "pollo", "palta", "tomate", "lechuga", "yogur"),
                    List.of(
                            "Cocina el pollo con comino, pimentón y ajo hasta dorar.",
                            "Calienta las tortillas para que queden flexibles.",
                            "Rellena con pollo, tomate, lechuga y palta laminada.",
                            "Termina con salsa de yogur y unas gotas de limón."
                    ),
                    "https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?auto=format&fit=crop&w=900&q=80",
                    90,
                    2
            ),
            new RecipeView(
                    6L,
                    "Risotto de champiñones",
                    "Arroz cremoso cocinado lentamente con caldo y champiñones salteados.",
                    "Italiana",
                    "Italia",
                    "Media",
                    45,
                    List.of("arroz arborio", "champiñones", "caldo", "cebolla", "parmesano", "mantequilla"),
                    List.of(
                            "Saltea la cebolla y los champiñones hasta que tomen color.",
                            "Agrega el arroz y cocina un minuto antes de incorporar el caldo.",
                            "Añade el caldo de a poco, revolviendo para liberar el almidón.",
                            "Finaliza con mantequilla y parmesano para obtener una textura cremosa."
                    ),
                    "https://images.unsplash.com/photo-1476124369491-e7addf5db371?auto=format&fit=crop&w=900&q=80",
                    86,
                    1
            )
    );

    public List<RecipeView> findAll() {
        return recipes;
    }

    public List<RecipeView> latestRecipes() {
        return recipes.stream()
                .sorted(Comparator.comparingInt(RecipeView::getRecentOrder).reversed())
                .limit(3)
                .toList();
    }

    public List<RecipeView> popularRecipes() {
        return recipes.stream()
                .sorted(Comparator.comparingInt(RecipeView::getPopularityScore).reversed())
                .limit(3)
                .toList();
    }

    public Optional<RecipeView> findById(Long id) {
        return recipes.stream().filter(recipe -> recipe.getId().equals(id)).findFirst();
    }

    public List<RecipeView> search(String name,
                                   String cuisineType,
                                   String ingredient,
                                   String countryOfOrigin,
                                   String difficulty) {
        return recipes.stream()
                .filter(recipe -> contains(recipe.getName(), name))
                .filter(recipe -> contains(recipe.getCuisineType(), cuisineType))
                .filter(recipe -> contains(recipe.getCountryOfOrigin(), countryOfOrigin))
                .filter(recipe -> contains(recipe.getDifficulty(), difficulty))
                .filter(recipe -> ingredientMatches(recipe, ingredient))
                .toList();
    }

    private boolean contains(String source, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return source != null && source.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private boolean ingredientMatches(RecipeView recipe, String ingredient) {
        if (ingredient == null || ingredient.isBlank()) {
            return true;
        }
        String normalized = ingredient.toLowerCase(Locale.ROOT);
        return recipe.getIngredients().stream()
                .anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(normalized));
    }
}
