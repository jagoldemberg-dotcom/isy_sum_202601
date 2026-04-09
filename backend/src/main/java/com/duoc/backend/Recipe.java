package com.duoc.backend;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank(message = "El tipo de cocina es obligatorio")
    @Size(max = 60, message = "El tipo de cocina no puede superar 60 caracteres")
    @Column(nullable = false, length = 60)
    private String cuisineType;

    @NotBlank(message = "El país de origen es obligatorio")
    @Size(max = 60, message = "El país de origen no puede superar 60 caracteres")
    @Column(nullable = false, length = 60)
    private String countryOfOrigin;

    @NotBlank(message = "La dificultad es obligatoria")
    @Size(max = 30, message = "La dificultad no puede superar 30 caracteres")
    @Column(nullable = false, length = 30)
    private String difficulty;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    @Column(nullable = false, length = 255)
    private String summary;

    @NotBlank(message = "Las instrucciones son obligatorias")
    @Size(max = 4000, message = "Las instrucciones no pueden superar 4000 caracteres")
    @Column(nullable = false, length = 4000)
    private String instructions;

    @NotNull(message = "El tiempo de cocción es obligatorio")
    @Min(value = 1, message = "El tiempo de cocción debe ser mayor que cero")
    @Max(value = 600, message = "El tiempo de cocción es demasiado alto")
    @Column(nullable = false)
    private Integer cookTimeMinutes;

    @Column(nullable = false)
    private Integer popularityScore = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "ingredient", nullable = false, length = 120)
    @NotEmpty(message = "Debe informar al menos un ingrediente")
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_photos", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "photo_url", nullable = false, length = 500)
    private List<String> photos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_videos", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "video_url", nullable = false, length = 500)
    private List<String> videos = new ArrayList<>();

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients == null ? new ArrayList<>() : new ArrayList<>(ingredients);
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos == null ? new ArrayList<>() : new ArrayList<>(photos);
    }

    public List<String> getVideos() {
        return videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos == null ? new ArrayList<>() : new ArrayList<>(videos);
    }
}
