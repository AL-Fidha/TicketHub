import java.util.List;

public class Administrator extends User {

    public Administrator(String userId, String username, String password) {
        super(userId, username, password, "Administrator");
    }

    @Override
    public void showMenu() {
        System.out.println("1. Manage Movies\n2. View Reports\n3. Manage Bookings");
    }

    public void addMovie(Movie movie, List<Movie> catalog) {
    catalog.add(movie);
    System.out.println("Movie added: " + movie.getTitle());
}


    public void removeMovie(Movie movie) {
        System.out.println("Movie removed: " + movie.getTitle());
    }
}

