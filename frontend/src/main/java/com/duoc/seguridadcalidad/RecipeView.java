package com.duoc.seguridadcalidad;

import java.util.List;

public class RecipeView {

    private final Long id;
    private final String name;
    private final String summary;
    private final String cuisineType;
    private final String countryOfOrigin;
    private final String difficulty;
    private final Integer cookTimeMinutes;
    private final List<String> ingredients;
    private final List<String> instructions;
    private final String imageUrl;
    private final int popularityScore;
    private final int recentOrder;

    public RecipeView(Long id,
                      String name,
                      String summary,
                      String cuisineType,
                      String countryOfOrigin,
                      String difficulty,
                      Integer cookTimeMinutes,
                      List<String> ingredients,
                      List<String> instructions,
                      String imageUrl,
                      int popularityScore,
                      int recentOrder) {
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.cuisineType = cuisineType;
        this.countryOfOrigin = countryOfOrigin;
        this.difficulty = difficulty;
        this.cookTimeMinutes = cookTimeMinutes;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageUrl = imageUrl;
        this.popularityScore = popularityScore;
        this.recentOrder = recentOrder;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Integer getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPopularityScore() {
        return popularityScore;
    }

    public int getRecentOrder() {
        return recentOrder;
    }
}
