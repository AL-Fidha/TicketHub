package com.tickethub.repository;

import com.tickethub.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    List<Theatre> findByCity(String city);

    @Query("SELECT DISTINCT t.city FROM Theatre t ORDER BY t.city")
    List<String> findAllCities();
}
