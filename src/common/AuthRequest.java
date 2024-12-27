package common;

/**
 * Represents an authentication request that contains a user's credentials and
 * type of authentication request. It can be used for both user registration and
 * login processes. This class encapsulates the type of request (either login or
 * registration), username, and password. The data can be serialized into bytes
 * for transmission and deserialized from bytes upon receipt.
 */
public class AuthRequest {

    /**
     * The type of request: 0 for registration, 1 for login.
     */
    public int type;

    /**
     * The username associated with the authentication request.
     */
    public String username;

    /**
     * The password associated with the authentication request.
     */
    public String password;

    /**
     * Constant representing a registration request type.
     */
    public static final int REGISTER = 0;

    /**
     * Constant representing a login request type.
     */
    public static final int LOGIN = 1;

    /**
     * Default constructor which initializes the type as -1, and both username
     * and password as null.
     */
    public AuthRequest() {
        this.type = -1;
        this.username = null;
        this.password = null;
    }

    /**
     * Constructs an authentication request with specified type, username, and
     * password.
     *
     * @param type The type of request: either {@link #REGISTER} or
     * {@link #LOGIN}.
     * @param username The username for the authentication request.
     * @param password The password for the authentication request.
     */
    public AuthRequest(int type, String username, String password) {
        this.type = type;
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username associated with the authentication request.
     *
     * @return The username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username for the authentication request.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password associated with the authentication request.
     *
     * @return The password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password for the authentication request.
     *
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the type of the authentication request. The type indicates whether
     * the request is for login or registration.
     *
     * @return The request type: {@link #REGISTER} for registration,
     * {@link #LOGIN} for login.
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type of the authentication request.
     *
     * @param Type The type of the request: {@link #REGISTER} for registration,
     * {@link #LOGIN} for login.
     */
    public void setType(int Type) {
        this.type = Type;
    }

    /**
     * Serializes the authentication request into a byte array for transmission.
     * The byte array contains the request type, username, and password,
     * separated by a delimiter ('|').
     *
     * @return The byte array representing the serialized authentication
     * request.
     */
    public byte[] getRequestBytes() {
        String request = this.type + "|" + this.username + "|" + this.password;  // Adds a delimiter
        return request.getBytes();
    }

    /**
     * Deserializes a byte array back into the authentication request object.
     * The byte array should contain the request type, username, and password,
     * separated by a delimiter ('|').
     *
     * @param requestBytes The byte array to deserialize.
     */
    public void readRequestBytes(byte[] requestBytes) {
        String requestData = new String(requestBytes);
        String[] parts = requestData.split("\\|");  // Split by the '|' delimiter

        this.type = Integer.parseInt(parts[0]);
        this.username = parts[1];
        this.password = parts[2];
    }

    /**
     * Provides a string representation of the authentication request, including
     * the username and password. The password is not masked in this
     * representation.
     *
     * @return A string representing the authentication request.
     */
    @Override
    public String toString() {
        return "AuthRequest [username=" + username + ", password=" + password + "]";
    }
}
