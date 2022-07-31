package com.geekbrains.cloud.june.cloudserver.Handler;

import com.geekbrains.cloud.*;
import com.geekbrains.cloud.june.regin.Registration.AuthService;
import com.geekbrains.cloud.june.regin.Registration.InMemory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class CloudHandler extends SimpleChannelInboundHandler<CloudMessage> {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public AuthService authService;
    private Path currentDir = Path.of("server_files");
    private String root;
    private ConnectedUser connectedUser;

    public CloudHandler(ConnectedUser connectedUser) {
        this.connectedUser = connectedUser;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListFiles(currentDir, "server_files"));

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        String nameClass = cloudMessage.getClass().getSimpleName();
        switch (nameClass) {
            case "RenameFile":
                if (cloudMessage instanceof RenameFile renameFile) {
                    Path p = currentDir.resolve(renameFile.getSource());
                    try {
                        if (p.toFile().exists()) {
                            if (p.toFile().renameTo(new File(currentDir.resolve(renameFile.getDestination()).toString()))) {
                                System.out.println("Rename complite");
                            } else {
                                System.out.println("Rename failed");
                            }
                        } else {
                            System.out.println("File No exist");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ctx.writeAndFlush(new ListFiles(currentDir, root));
                    break;
                }
            case "FileRequest":
                if (cloudMessage instanceof FileRequest fileRequest) {
                    if (currentDir.resolve(fileRequest.getName()).toFile().isDirectory()) {     // для создания папки
                        copyDirectory(currentDir.resolve(fileRequest.getName()), ctx);
                    } else {
                        ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
                    }
                    break;
                }
            case "FileMessage":
                if (cloudMessage instanceof FileMessage fileMessage) {
                    Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
                    ctx.writeAndFlush(new ListFiles(currentDir, root));
                    break;
                }

            case "SignIn":
                if (cloudMessage instanceof SignIn signIn) {
                    authService = new InMemory();
                    authService.start();
                    switch (signIn.getActionWithFile()) {
                        case signIn:        //вход
                            String autorization = authService.checkLoginAndPassword(signIn.getLogin(), signIn.getPassword());
                            if (!(autorization.equals("No Auth"))) {
                                if (!(connectedUser.findUserOnline(signIn.getLogin()))) {
                                    connectedUser.addUser(signIn.getLogin());
                                    connectedUser.getUsers();
                                    System.out.println("users");
                                } else {
                                    autorization = "UserOnline";
                                }
                            }
                            ctx.writeAndFlush(new ResponseServer(autorization));
                            authService.end();
                            break;
                        case registr:       //регистрация и вход
                            boolean registr = authService.registrationLogin(signIn.getLogin(), signIn.getPassword());
                            currentDir.resolve(signIn.getLogin()).toFile().mkdir();
                            ctx.writeAndFlush(new ResponseRegistr(registr));
                            authService.end();
                            break;
                    }
                }
                break;

            case "GoToDirServer":
                if (cloudMessage instanceof GoToDirServer goToDirServer) {
                    switch (goToDirServer.getActionWithFile()) {    //тут выбор какое действие на стороне ервера произвести -создать папку, перейти в папку или удалить фАЙЛ/ПААПКУ
                        case goTo:
                            root = goToDirServer.getNameUser();
                            if (!(goToDirServer.getName().equals("[ ... ]"))) {     //если имя выбранного объекта не [ ... ]
                                if (currentDir.resolve(goToDirServer.getName()).toFile().isDirectory()) {  //проверка на тип директория/файл
                                    currentDir = currentDir.resolve(goToDirServer.getName());
                                    ctx.writeAndFlush(new ListFiles(currentDir, root));// возвращаем список файлов
                                }
                            } else {
                                if (!currentDir.toFile().getName().equals(goToDirServer.getNameUser())) {
                                    currentDir = currentDir.getParent();        //иначе возвращаемся на папку выше
                                    ctx.writeAndFlush(new ListFiles(currentDir, root));//отсылаем список файлов
                                }
                            }
                            break;

                        case goToRoot:
                            if (currentDir.toString().contains(goToDirServer.getName())) {
                                while (currentDir.toString().contains(goToDirServer.getName())) {
                                    currentDir = currentDir.getParent();
                                }
                                ctx.writeAndFlush(new ListFiles(currentDir, root));
                            }
                            break;

                        case copy:

                            if (currentDir.resolve(goToDirServer.getName()).toFile().isDirectory()) {  //проверка на тип директория/файл
                                currentDir = currentDir.resolve(goToDirServer.getName());
                            }
                            break;

                        case delete:
                            if (!(goToDirServer.getName().equals("[ ... ]"))) {     //если имя выбранного объекта не [ ... ]
                                if (currentDir.resolve(goToDirServer.getName()).toFile().isFile()) {
                                    currentDir.resolve(goToDirServer.getName()).toFile().delete();
                                    ctx.writeAndFlush(new ListFiles(currentDir, root));// возвращаем список файлов
                                } else {
                                    for (File f : currentDir.resolve(goToDirServer.getName()).toFile().listFiles()) {   //удаление если это папка
                                        recursiveDelete(f);
                                    }
                                    currentDir.resolve(goToDirServer.getName()).toFile().delete();
                                    ctx.writeAndFlush(new ListFiles(currentDir, root));
                                }
                            }
                            break;
                        case create:
                            File f = new File(currentDir.resolve(goToDirServer.getName()).toString());
                            f.mkdir();
                            ctx.writeAndFlush(new ListFiles(currentDir, root));
                            break;
                    }
                    break;
                }
        }
    }

    public static void recursiveDelete(File file) {
        if (!file.exists()) return;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        file.delete();
    }

    public void recursiveCopy(Path path, ChannelHandlerContext ctx) throws IOException {
        if (path.toFile().isDirectory()) {
            copyDirectory(path, ctx);
        } else {
            ctx.writeAndFlush(new FileMessage(path));
        }
    }

    private void copyDirectory(Path path, ChannelHandlerContext ctx) throws IOException {
        String file = path.toFile().getName();
        ctx.writeAndFlush(new CopyDir(file));
        for (Path p : Files.list(path).collect(Collectors.toList())) {
            recursiveCopy(p, ctx);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        connectedUser.removeUser(root);
    }
}
