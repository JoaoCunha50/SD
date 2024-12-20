package common;

public class TasksRequest {

    public static final int EXIT = 0;
    public static final int PUT = 1;
    public static final int GET = 2;

    public int type;
    public String key;
    public String value;

    public TasksRequest() {
        this.type = -1;
        this.key = null;
        this.value = null;
    }

    public TasksRequest(int type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public byte[] getTaskBytes() {
        int requestType = this.type;

        switch (requestType) {
            case PUT -> {
                String taskPut = this.type + "|" + this.key + "|" + this.value;
                return taskPut.getBytes();
            }
            case GET -> {
                String taskGet = this.type + "|" + this.key;
                return taskGet.getBytes();
            }
            case EXIT -> {
                String taskExit = String.valueOf(this.type);
                return taskExit.getBytes();
            }
            default -> {
                return null;
            }
        }
    }

    public void readTaskBytes(byte[] requestBytes) {
        String requestData = new String(requestBytes);
        String[] parts = requestData.split("\\|");

        int requestType = Integer.parseInt(parts[0]);
        this.type = requestType;

        switch (requestType) {
            case GET:
                this.key = parts[1];
                break;
            case PUT:
                this.key = parts[1];
                this.value = parts[2];
                break;
            case EXIT:
                // Não há dados a ler no caso de EXIT
                break;
            default:
                System.out.println("Task type unrecognised.");
                break;
        }
    }
    @Override
    public String toString() {
        return "TasksRequest [type=" + type + ", key=" + key + ", value=" + value + "]";
    }
}
