package com.duoc.seguridadcalidad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipeView {

    private Long id;
    private String name;
    private String summary;
    private String cuisineType;
    private String countryOfOrigin;
    private String difficulty;
    private Integer cookTimeMinutes;
    private Integer popularityScore;
    private List<String> ingredients;
    private List<String> photos;
    private List<String> videos;
    private String instructions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public void setCookTimeMinutes(Integer cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public Integer getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(Integer popularityScore) {
        this.popularityScore = popularityScore;
    }

    public List<String> getIngredients() {
        return ingredients == null ? Collections.emptyList() : ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getPhotos() {
        return photos == null ? Collections.emptyList() : photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public List<String> getVideos() {
        return videos == null ? Collections.emptyList() : videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getImageUrl() {
        return getPhotos().isEmpty()
                ? "https://images.unsplash.com/photo-1495521821757-a1efb6729352?auto=format&fit=crop&w=900&q=80"
                : getPhotos().getFirst();
    }

    public List<String> getInstructionSteps() {
        if (instructions == null || instructions.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(instructions.split("\\.\\s+"))
                .map(String::trim)
                .filter(step -> !step.isBlank())
                .map(step -> step.endsWith(".") ? step : step + ".")
                .toList();
    }
}
