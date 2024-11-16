package common;

public class AuthRequest {
    public String username;
    public String password;

    public AuthRequest() {
        this.username = null;
        this.password = null;
    }

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

    public int loginAuth(String username, String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            return 0;
        } else
            return 1;
    }

    public byte[] getRequestBytes() {
        String request = this.username + "|" + this.password;  // Adiciona um delimitador
        return request.getBytes();
    }

    public void readRequestBytes(byte[] requestBytes) {
        String requestData = new String(requestBytes);
        String[] parts = requestData.split("\\|");  // Divide pelo delimitador '|'
        
        this.username = parts[0];
        this.password = parts[1];
    }
    
    @Override
    public String toString() {
        return "AuthRequest [username=" + username + ", password=" + password + "]";
    }
}
