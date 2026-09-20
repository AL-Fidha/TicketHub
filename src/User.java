public abstract class User {
    protected String userId;
    protected String username;
    protected String password;
    protected String role;

    public User(String userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public boolean login(String inputUser, String inputPass) {
        return username.equals(inputUser) && password.equals(inputPass);
    }

    public abstract void showMenu();
}

