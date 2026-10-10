package com.tickethub.repository;

import com.tickethub.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    java.util.Optional<Movie> findFirstByTmdbId(Long tmdbId);


    List<Movie> findByLanguage(Movie.Language language);

    List<Movie> findByStatus(Movie.Status status);

    @Query("SELECT m FROM Movie m WHERE " +
           "(:language IS NULL OR m.language = :language) AND " +
           "(:genre    IS NULL OR m.genre LIKE %:genre%) AND " +
           "(:search   IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%',:search,'%')))")
    List<Movie> findFiltered(
        @Param("language") Movie.Language language,
        @Param("genre")    String genre,
        @Param("search")   String search
    );
}
