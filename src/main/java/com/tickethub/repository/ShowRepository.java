package com.tickethub.repository;

import com.tickethub.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

        @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theatre " +
            "ORDER BY s.showDate DESC, s.showTime")
        List<Show> findAllWithDetails();

        boolean existsByScreen_IdAndShowDateAndShowTime(Long screenId, LocalDate showDate, java.time.LocalTime showTime);

    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.movie m " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theatre t " +
           "WHERE m.id = :movieId " +
           "AND (:showDate IS NULL OR s.showDate = :showDate) " +
           "AND (:theatreId IS NULL OR t.id = :theatreId) " +
           "AND s.showDate >= CURRENT_DATE " +
           "ORDER BY s.showDate, t.city, s.showTime")
    List<Show> findByMovieFiltered(
        @Param("movieId")   Long movieId,
        @Param("showDate")  LocalDate showDate,
        @Param("theatreId") Long theatreId
    );
}
