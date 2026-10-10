package com.tickethub.repository;

import com.tickethub.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
       boolean existsByShow_Id(Long showId);


    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.show s JOIN FETCH s.movie " +
           "JOIN FETCH s.screen sc JOIN FETCH sc.theatre " +
           "WHERE b.user.id = :userId ORDER BY b.bookedAt DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);

    Optional<Booking> findByBookingRef(String bookingRef);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.show s JOIN FETCH s.movie " +
           "JOIN FETCH s.screen sc JOIN FETCH sc.theatre " +
           "ORDER BY b.bookedAt DESC")
    List<Booking> findAllWithDetails();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = 'Confirmed'")
    Double getTotalRevenue();

    long countByStatus(Booking.Status status);
}
