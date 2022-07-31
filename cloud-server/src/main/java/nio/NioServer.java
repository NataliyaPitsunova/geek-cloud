
package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;


public class NioServer {

    private Path dir = Path.of(System.getProperty("user.home"));

    private ServerSocketChannel server;
    private Selector selector;

    public NioServer() throws IOException {
        server = ServerSocketChannel.open();
        selector = Selector.open();
        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept();
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }

        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder s = new StringBuilder();
        while (channel.isOpen()) {
            int read = channel.read(buf);
            if (read < 0) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                s.append((char) buf.get());
            }
            buf.clear();
        }

        String message = s.toString().trim();
        if (message.startsWith("ls")) {       //содержание папки
            String[] list = new File(String.valueOf(dir)).list();
            for (String file : list) {
                channel.write(ByteBuffer.wrap(file.getBytes(UTF_8)));
                channel.write(ByteBuffer.wrap("\r\n".getBytes(UTF_8)));
            }
            channel.write(ByteBuffer.wrap("\r\n->".getBytes(UTF_8)));
        }
        if (message.startsWith("cat")) { //содержания файла с проверкой на наличие, и большее, чем 1 количество пробелов между командой и именем файла
            String[] msg = message.split(" ");
            String fileName = null;
            for (int i = 1; i < msg.length; i++) {
                if (msg[i].length() > 1) {
                    fileName = msg[i];
                    break;
                }
            }

            File f = new File(String.valueOf(dir.resolve(fileName).toFile()));
            if (f.exists()) {
                List<String> lines = Files.readAllLines(Paths.get(f.getPath()), UTF_8);
                for (String line : lines) {
                    channel.write(ByteBuffer.wrap(line.getBytes(UTF_8)));
                    channel.write(ByteBuffer.wrap("\r\n".getBytes(UTF_8)));
                }
                channel.write(ByteBuffer.wrap("\r\n->".getBytes(UTF_8)));
            } else {
                channel.write(ByteBuffer.wrap("Файл с таким именем не найден\n".getBytes(UTF_8)));
                channel.write(ByteBuffer.wrap("\r\n->".getBytes(UTF_8)));
            }
        }

        if (message.startsWith("cd")) {/* cd path - перейти в папку с именем*/
            String[] mess = message.split(" ");
            if (mess.length > 2) {
                channel.write(ByteBuffer.wrap("Неверно указана выбранная директория\n".getBytes(UTF_8)));
                channel.write(ByteBuffer.wrap("\r\n->".getBytes(UTF_8)));
            } else {
                Path newDir = dir.resolve(mess[1]);
                File d = newDir.toFile();
                if (!(d.isDirectory())) {
                    channel.write(ByteBuffer.wrap("Указаное имя не принадлежит директории\n".getBytes(UTF_8)));
                    channel.write(ByteBuffer.wrap("\r\n->".getBytes(UTF_8)));
                } else {
                    if (d.exists()) {
                        dir = newDir;
                        channel.write(ByteBuffer.wrap(("вы перешли в директорию " + newDir + " \n").getBytes(UTF_8)));
                        channel.write(ByteBuffer.wrap("\r\n->".getBytes(UTF_8)));
                    } else {
                        channel.write(ByteBuffer.wrap("не существует данной директории\n".getBytes(UTF_8)));
                        channel.write(ByteBuffer.wrap("\r\n->".getBytes(UTF_8)));
                    }
                }
            }
        }
    }

    /* cd path - перейти в папку с именем*/
    private void handleAccept() throws IOException {
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap("Welcome in Mike terminal!\n-> ".getBytes(UTF_8)));
    }
}