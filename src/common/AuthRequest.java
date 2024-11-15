package common;

public class AuthRequest {
    public String username;
    public String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    int loginAuth(String username, String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            return 0;
        } else
            return 1;
    }

    @Override
    public String toString() {
        return "AuthRequest [username=" + username + ", password=" + password + "]";
    }
}
