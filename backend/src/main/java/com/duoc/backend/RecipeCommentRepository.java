package com.duoc.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeCommentRepository extends JpaRepository<RecipeComment, Long> {

    List<RecipeComment> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);
}
