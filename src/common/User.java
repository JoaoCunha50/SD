package common;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class User implements Serializable {
    public int id;
    public String username;
    public String password;

    public User() {
        this.username = null;
        this.password = null;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int registerAuth(ConcurrentHashMap<String, User> users) {
        User user = users.get(this.getUsername());
        if (user == null) {
            users.put(this.getUsername(), this);
            System.out.println("Register was successfull, notifying client ...");
            return 1;
        } else
            System.out.println("There is already a user with that username, notifying client ...");
        return 0;
    }

    public int loginAuth(ConcurrentHashMap<String, User> users) {
        User user = users.get(this.getUsername());
        if (user != null) {
            if (user.getPassword().equals(this.getPassword())) {
                System.out.println("Login was successfull, notifying client ...");
                return 1;
            }
            System.out.println("Pssword is invalid, notifying client ...");
            return -1;
        }
        System.out.println("There is no user with such credentials, notifying client ...");
        return 0;
    }
}