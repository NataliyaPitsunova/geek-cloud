package netty.Handler;

import com.geekbrains.cloud.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;

public class CloudFilesHandler extends SimpleChannelInboundHandler<CloudMessage> {
    private Path currentDir;

    public CloudFilesHandler() {
        currentDir = Path.of("server_files");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListFiles(currentDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(currentDir));
        } else if (cloudMessage instanceof GoToDirServer goToDirServer) {                   //переход в папку
            if (!(goToDirServer.getName().equals("[ ... ]"))) {     //если имя выбранного объекта не [ ... ]
                if (currentDir.resolve(goToDirServer.getName()).toFile().isDirectory()) {  //проверка на тип директория/файл
                    currentDir = currentDir.resolve(goToDirServer.getName());
                    ctx.writeAndFlush(new ListFiles(currentDir));// возвращаем список файлов
                }
            } else {
                currentDir = currentDir.getParent();        //иначе возвращаемся на папку выше
                ctx.writeAndFlush(new ListFiles(currentDir));//отсылаем список файлов
            }
        }
    }
}