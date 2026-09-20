import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Movie> catalog = new ArrayList<>();
        Administrator admin = new Administrator("A1", "admin", "pass");
        Customer customer = new Customer("C1", "user", "123");

        // Admin adds movie
        admin.addMovie(new Movie("M1", "Inception", "Sci-Fi", 148), catalog);

        // Customer browses catalog
        customer.browseCatalog(catalog);

        // Customer books ticket
        Booking booking = new Booking(catalog.get(0));
        customer.bookTicket(booking);
        booking.printTicket();
    }
}
