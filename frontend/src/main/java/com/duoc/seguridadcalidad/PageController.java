package com.duoc.seguridadcalidad;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;

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
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String ok,
                         @RequestParam(required = false) String error,
                         Model model,
                         Authentication authentication) {
        addUserInfo(model, authentication);

        String token = tokenFrom(authentication);
        RecipeView recipe = recipeApiService.findById(id, token);
        List<CommentView> comments = recipeApiService.getComments(id, token);

        model.addAttribute("recipe", recipe);
        model.addAttribute("comments", comments);
        model.addAttribute("averageRating", averageRating(comments));
        model.addAttribute("commentCount", comments.size());
        model.addAttribute("ok", safeValue(ok));
        model.addAttribute("error", safeValue(error));
        model.addAttribute("commentForm", new AddCommentForm());
        model.addAttribute("photoForm", new AddMediaForm());
        model.addAttribute("videoForm", new AddMediaForm());
        model.addAttribute("shareForm", new ShareForm());
        model.addAttribute("pageTitle", "Detalle receta");
        return "detalle";
    }

    @PostMapping("/detalle/{id}/comentarios")
    public String addComment(@PathVariable Long id,
                             AddCommentForm form,
                             Authentication authentication) {
        try {
            recipeApiService.addComment(id, form, tokenFrom(authentication));
            return "redirect:/detalle/" + id + "?ok=Comentario%20registrado";
        } catch (Exception exception) {
            return "redirect:/detalle/" + id + "?error=No%20se%20pudo%20guardar%20el%20comentario";
        }
    }

    @PostMapping("/detalle/{id}/fotos")
    public String addPhoto(@PathVariable Long id,
                           AddMediaForm form,
                           Authentication authentication) {
        try {
            recipeApiService.addPhoto(id, form, tokenFrom(authentication));
            return "redirect:/detalle/" + id + "?ok=Foto%20agregada";
        } catch (Exception exception) {
            return "redirect:/detalle/" + id + "?error=No%20se%20pudo%20agregar%20la%20foto";
        }
    }

    @PostMapping("/detalle/{id}/videos")
    public String addVideo(@PathVariable Long id,
                           AddMediaForm form,
                           Authentication authentication) {
        try {
            recipeApiService.addVideo(id, form, tokenFrom(authentication));
            return "redirect:/detalle/" + id + "?ok=Video%20agregado";
        } catch (Exception exception) {
            return "redirect:/detalle/" + id + "?error=No%20se%20pudo%20agregar%20el%20video";
        }
    }

    @PostMapping("/detalle/{id}/compartir")
    public String shareRecipe(@PathVariable Long id,
                              ShareForm form,
                              Authentication authentication) {
        try {
            RecipeShareResponseView response = recipeApiService.shareRecipe(id, form, tokenFrom(authentication));
            if (!"sitio".equals(form.getPlatform().toLowerCase(Locale.ROOT))
                    && response.getShareUrl() != null
                    && response.getShareUrl().startsWith("http")) {
                return "redirect:" + response.getShareUrl();
            }
            return "redirect:/detalle/" + id + "?ok=Receta%20compartida%20correctamente";
        } catch (Exception exception) {
            return "redirect:/detalle/" + id + "?error=No%20se%20pudo%20compartir%20la%20receta";
        }
    }

    private void addUserInfo(Model model, Authentication authentication) {
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUser;
        model.addAttribute("authenticated", authenticated);
        model.addAttribute("username", authenticated ? ((AuthenticatedUser) authentication.getPrincipal()).getUsername() : null);
        model.addAttribute("admin", authenticated && ((AuthenticatedUser) authentication.getPrincipal()).isAdmin());
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

    private double averageRating(List<CommentView> comments) {
        if (comments == null || comments.isEmpty()) {
            return 0;
        }
        return comments.stream()
                .mapToInt(CommentView::getRating)
                .average()
                .orElse(0);
    }
}
