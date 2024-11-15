package common;

public class AuthResponse {
    public int status;

    public AuthResponse(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AuthResponse [status=" + status + "]";
    }

}
