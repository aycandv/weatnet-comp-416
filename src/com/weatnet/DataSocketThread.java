package com.weatnet;

import javax.imageio.ImageIO;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DataSocketThread {
    Socket socket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    String request;
    String location;
    String type;
    int hashValue;

    public DataSocketThread(Socket s, String request) throws IOException {
        socket = s;
        dataInputStream = new DataInputStream(s.getInputStream());
        dataOutputStream = new DataOutputStream(s.getOutputStream());
        this.request = request;
        location = request.split(" ")[2];
        type = request.split(" ")[3];
    }

    public int getHashValue() {
        return hashValue;
    }

    public void setHashValue(int hashValue) {
        this.hashValue = hashValue;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void run() {
        System.out.println("Request reeceived:" + getRequest());
        Api owmAPI = new Api(getLocation(), getType());
        owmAPI.fetchData();

        try {
            if (!getType().equals("clouds") && !getType().equals("pressure") && !getType().equals("wind") && !getType().equals("temp")) {
                getDataOutputStream().writeUTF(owmAPI.getMessage());
                setHashValue(owmAPI.getMessage().hashCode());
            } else {
                //getDataOutputStream().writeUTF("Sending image...");
                ImageIO.write(owmAPI.getBufferedImage(), "png", socket.getOutputStream());
                setHashValue(owmAPI.getBufferedImage().hashCode());
            }

            //System.out.println(getHashValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
