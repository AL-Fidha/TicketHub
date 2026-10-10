package com.tickethub.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seat_label"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Show show;

    @Column(name = "seat_label", nullable = false, length = 10)
    private String seatLabel;   // e.g. A1, B5, H10

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 10)
    private SeatType seatType;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    public enum SeatType { standard, premium }
}
