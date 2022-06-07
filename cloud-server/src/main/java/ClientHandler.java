import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;



public class ClientHandler implements Runnable {
    private final String serverDir = "server_files";

    private DataInputStream is;
    private DataOutputStream os;

    public ClientHandler(@NotNull Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        System.out.println("Client accepted");
        sendList(serverDir);
    }

    private void sendList(String dir) throws IOException {
        os.writeUTF("#list");
        List<String> files = getFiles(serverDir);
        os.writeInt(files.size());
        for (String file : files) {
            os.writeUTF(file);
        }
        os.flush();
    }

    public List<String> getFiles(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] buf = new byte[256];
                String command = is.readUTF();
                System.out.println("received: " + command);
                if (command.equals("#file")) {              //это команда приходит если нажата кнопка upload
                    String fileName = is.readUTF();
                    long len = is.readLong();//понадобиться
                    File file = Path.of(serverDir).resolve(fileName).toFile();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        int count;
                        if ((count = is.read(buf)) != -1) {
                            fos.write(buf, 0, count);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sendList(serverDir);
                }
                if (command.equals("#received")) {      //если была нажата кнопка download(file from srv)
                    String fileName = is.readUTF();
                    os.writeUTF("#received");       //отправляем на клиента обратно сообщение
                    File toSend = Path.of(serverDir).resolve(fileName).toFile();
                    os.writeLong(toSend.length());
                    try (FileInputStream fis = new FileInputStream(toSend)) {
                        while (fis.available() > 0) {
                            int read = fis.read(buf);
                            os.write(buf, 0, read);         //пишем в поток
                        }
                    }
                    os.flush();
                }
            }
        } catch (Exception e) {
            System.out.println("Connection was broken");
        }
    }
}
