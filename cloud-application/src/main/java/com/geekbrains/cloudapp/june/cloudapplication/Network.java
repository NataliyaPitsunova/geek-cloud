package com.geekbrains.cloudapp.june.cloudapplication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private final int port;

    private DataInputStream is;
    private DataOutputStream os;

    public Network(int port) throws IOException {
        this.port = port;
        Socket socket = new Socket("localhost", port);
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
    }

    public String readString() throws IOException {
        return is.readUTF();
    }

    public int getInt() throws IOException {
        return is.readInt();
    }

/*    public void writeCommand(String command) throws IOException {
        os.writeUTF(command);
        os.flush();
    }*/

    public DataInputStream getIs() {
        return is;
    }

    public DataOutputStream getOs() {
        return os;
    }
}
