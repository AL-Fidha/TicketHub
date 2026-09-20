import java.util.UUID;

public class Booking {
    private String bookingId;
    private Movie movie;

    public Booking(Movie movie) {
        this.bookingId = UUID.randomUUID().toString();
        this.movie = movie;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void printTicket() {
        System.out.println("Ticket ID: " + bookingId);
        System.out.println("Movie: " + movie.getTitle());
    }
}
