package common;

public class AuthRequest {
    public int type;
    public String username;
    public String password;

    public static final int REGISTER = 0;
    public static final int LOGIN = 1;

    public AuthRequest() {
        this.type = -1;
        this.username = null;
        this.password = null;
    }

    public AuthRequest(int type, String username, String password) {
        this.type = type;
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

    public int getType() {
        return type;
    }

    public void setType(int Type) {
        this.type = Type;
    }

    public byte[] getRequestBytes() {
        String request = this.type + "|" + this.username + "|" + this.password;  // Adiciona um delimitador
        return request.getBytes();
    }

    public void readRequestBytes(byte[] requestBytes) {
        String requestData = new String(requestBytes);
        String[] parts = requestData.split("\\|");  // Divide pelo delimitador '|'
        
        this.type = Integer.parseInt(parts[0]);
        this.username = parts[1];
        this.password = parts[2];
    }
    
    @Override
    public String toString() {
        return "AuthRequest [username=" + username + ", password=" + password + "]";
    }
}
