package common;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a user in the system. The class includes the user's credentials
 * (username and password), as well as methods for user authentication, both for
 * registration and login processes. The class implements {@link Serializable}
 * to allow the object to be serialized for network transmission or storage.
 */
public class User implements Serializable {

    /**
     * The user's unique identifier (ID).
     */
    public int id;

    /**
     * The username associated with the user.
     */
    public String username;

    /**
     * The password associated with the user.
     */
    public String password;

    /**
     * Default constructor that initializes the username and password as null.
     * The ID is not initialized, as it is typically set later.
     */
    public User() {
        this.username = null;
        this.password = null;
    }

    /**
     * Constructs a user object with the specified username and password.
     *
     * @param username The username for the user.
     * @param password The password for the user.
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username of the user.
     *
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The username to set for the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password of the user.
     *
     * @return The password of the user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user.
     *
     * @param password The password to set for the user.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Registers the user in the provided users map (a
     * {@link ConcurrentHashMap}). If the username does not already exist in the
     * map, the user will be added. A success message is displayed if the
     * registration is successful; otherwise, a message indicating that the
     * username already exists is displayed.
     *
     * @param users The map of users where usernames are the keys, and
     * {@link User} objects are the values.
     * @return 1 if the registration was successful, 0 if the username already
     * exists.
     */
    public int registerAuth(ConcurrentHashMap<String, User> users) {
        User user = users.get(this.getUsername());
        if (user == null) {
            users.put(this.getUsername(), this);
            System.out.println("Register was successful, notifying client ...");
            return 1;
        } else {
            System.out.println("There is already a user with that username, notifying client ...");
        }
        return 0;
    }

    /**
     * Logs the user in by checking if the username and password match an
     * existing user in the provided map. If the user is found and the password
     * matches, the login is successful. If the password does not match, an
     * error message is displayed. If the username does not exist, an error
     * message is displayed.
     *
     * @param users The map of users where usernames are the keys, and
     * {@link User} objects are the values.
     * @return 1 if the login was successful, 0 if the username does not exist,
     * -1 if the password is incorrect.
     */
    public int loginAuth(ConcurrentHashMap<String, User> users) {
        User user = users.get(this.getUsername());
        if (user != null) {
            if (user.getPassword().equals(this.getPassword())) {
                System.out.println("Login was successful, notifying client ...");
                return 1;
            }
            System.out.println("Password is invalid, notifying client ...");
            return -1;
        }
        System.out.println("There is no user with such credentials, notifying client ...");
        return 0;
    }
}
