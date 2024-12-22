package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TasksRequest {

    public static final int EXIT = 0;
    public static final int PUT = 1;
    public static final int GET = 2;
    public static final int MULTIPUT = 3;
    public static final int MULTIGET = 4;

    public int type;
    public String key;
    public String value;
    public int N;
    HashMap<String, String> pairs;
    ArrayList<String> keys;

    public TasksRequest() {
        this.type = -1;
        this.key = null;
        this.value = null;
        this.N = 0;
        this.pairs = null;
        this.keys = null;
    }

    public TasksRequest(int type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.N = 0;
        this.pairs = null;
        this.keys = null;
    }

    public TasksRequest(int type, String key, String value, int N, HashMap<String,String> pairs, ArrayList<String> keys) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.N = N;
        this.pairs = pairs;
        this.keys = keys;
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

    public int getN() {
        return N;
    }

    public void setN(int N) {
        this.N = N;
    }

    public HashMap<String,String> getPairs() {
        return pairs;
    }

    public void setPairs(HashMap<String,String> pairs) {
        this.pairs = pairs;
    }

    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
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
            case MULTIPUT -> {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : this.pairs.entrySet()) {
                    sb.append(entry.getKey()).append("|").append(entry.getValue()).append("|");
                }
                
                String taskMultiPut = this.type + "|" + this.N + "|" + sb.toString();
                return taskMultiPut.getBytes();
            }
            case MULTIGET -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < this.keys.size(); i++) {
                    sb.append("|").append(this.keys.get(i));
                }
                
                String taskMultiGet = this.type + "|" + this.N + sb.toString();
                return taskMultiGet.getBytes();
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
            case MULTIPUT:
                this.N = Integer.parseInt(parts[1]);
                int j = 2;
                HashMap<String,String> pairs = new HashMap<>();
                for(int i = 0; i<N; i++){
                    pairs.put(parts[j], parts[j+1]);
                    j = j+2;
                }
                this.pairs = pairs;
                break;
            case MULTIGET:
                this.N = Integer.parseInt(parts[1]);
                ArrayList<String> keys = new ArrayList<>();
                for(int i = 0; i<N; i++){
                    keys.add(parts[i+2]);
                }
                this.keys = keys;
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

    public String multiPutToString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : this.pairs.entrySet()) {
            sb.append(", [key=").append(entry.getKey()).append(", value=").append(entry.getValue()).append("]");
        }
        return "TasksRequest [type=" + type + sb.toString() + "]";
    }
}
