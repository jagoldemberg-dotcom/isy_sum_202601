package com.duoc.seguridadcalidad;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    private final RecipeApiService recipeApiService;

    public PageController(RecipeApiService recipeApiService) {
        this.recipeApiService = recipeApiService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        addUserInfo(model, authentication);
        model.addAttribute("latestRecipes", recipeApiService.latestRecipes());
        model.addAttribute("popularRecipes", recipeApiService.popularRecipes());
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
        model.addAttribute("recipes", recipeApiService.search(name, cuisineType, ingredient, countryOfOrigin, difficulty));
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
        addUserInfo(model, authentication);
        model.addAttribute("recipe", recipeApiService.findById(id, tokenFrom(authentication)));
        model.addAttribute("pageTitle", "Detalle receta");
        return "detalle";
    }

    private void addUserInfo(Model model, Authentication authentication) {
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUser;
        model.addAttribute("authenticated", authenticated);
        model.addAttribute("username", authenticated ? ((AuthenticatedUser) authentication.getPrincipal()).getUsername() : null);
    }

    private String tokenFrom(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("No existe una sesión autenticada para consultar el detalle");
        }
        return user.getJwtToken();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
