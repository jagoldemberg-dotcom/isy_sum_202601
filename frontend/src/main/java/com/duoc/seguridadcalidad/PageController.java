package com.duoc.seguridadcalidad;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class PageController {

    private final StaticRecipeService staticRecipeService;

    public PageController(StaticRecipeService staticRecipeService) {
        this.staticRecipeService = staticRecipeService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        addUserInfo(model, authentication);
        model.addAttribute("latestRecipes", staticRecipeService.latestRecipes());
        model.addAttribute("popularRecipes", staticRecipeService.popularRecipes());
        model.addAttribute("pageTitle", "Inicio");
        return "home";
    }

    @GetMapping("/buscar")
    public String search(@RequestParam(required = false) String name,
                         @RequestParam(required = false) String cuisineType,
                         @RequestParam(required = false) String ingredient,
                         @RequestParam(required = false) String countryOfOrigin,
                         @RequestParam(required = false) String difficulty,
                         Model model,
                         Authentication authentication) {
        addUserInfo(model, authentication);
        model.addAttribute("recipes", staticRecipeService.search(name, cuisineType, ingredient, countryOfOrigin, difficulty));
        model.addAttribute("name", safeValue(name));
        model.addAttribute("cuisineType", safeValue(cuisineType));
        model.addAttribute("ingredient", safeValue(ingredient));
        model.addAttribute("countryOfOrigin", safeValue(countryOfOrigin));
        model.addAttribute("difficulty", safeValue(difficulty));
        model.addAttribute("pageTitle", "Buscar recetas");
        return "buscar";
    }

    @GetMapping("/detalle/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        RecipeView recipe = staticRecipeService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Receta no encontrada"));
        addUserInfo(model, authentication);
        model.addAttribute("recipe", recipe);
        model.addAttribute("pageTitle", recipe.getName());
        return "detalle";
    }

    private void addUserInfo(Model model, Authentication authentication) {
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());
        model.addAttribute("authenticated", authenticated);
        model.addAttribute("username", authenticated ? authentication.getName() : null);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
