package com.tickethub.repository;

import com.tickethub.model.BookingSeat;
import com.tickethub.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    @Query("SELECT bs.seatLabel FROM BookingSeat bs " +
           "JOIN bs.booking b " +
           "WHERE bs.show.id = :showId AND b.status != com.tickethub.model.Booking$Status.Cancelled")
    List<String> findBookedSeatsByShowId(@Param("showId") Long showId);

    @Query("SELECT COUNT(bs) FROM BookingSeat bs " +
           "JOIN bs.booking b " +
           "WHERE bs.show.id = :showId AND b.status != com.tickethub.model.Booking$Status.Cancelled")
    long countBookedSeatsByShowId(@Param("showId") Long showId);
}
