package me.pintoadmin.pages;

import me.pintoadmin.MessageManager;

import java.io.IOException;
import java.net.*;

public class NetworkingManager {
    private static volatile NetworkingManager instance;
    public static int PORT = 51337;
    private final int BUFSIZE = 1024;
    private NetworkingManager(){
        PORT = ConfigLoader.getInstance().getPort();
        init();
    }

    public static NetworkingManager getInstance(){
        if (instance == null) { // First check (no logging/locking overhead)
            synchronized (NetworkingManager.class) {
                if (instance == null) { // Second check (inside synchronization)
                    instance = new NetworkingManager();
                }
            }
        }
        return instance;
    }

    private Thread socketThread;
    private DatagramSocket socket;

    public void init(){
        socketThread = new Thread(() -> {
            try {
                socket = new DatagramSocket(PORT);
                while (!Thread.currentThread().isInterrupted()) {
                    byte[] buf = new byte[BUFSIZE];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);

                    socket.receive(packet);

                    String msg = new String(packet.getData());
                    InetSocketAddress addr = new InetSocketAddress(packet.getAddress(), packet.getPort());

                    if (MessageManager.getInstance().newChat(addr.getHostName(), String.valueOf(PORT), addr.getHostString())) {
                        MessageManager.getInstance().recieveMessage(msg, addr.getHostName());
                    }
                }
            } catch (BindException ignored){
                throw new RuntimeException("There's another instance running!");
            } catch (SocketException ignored) {
            }  catch (IOException e){
                throw new RuntimeException(e);
            }
        });
        socketThread.start();
    }

    public void deinit(){
        socketThread.interrupt();
        if(socket != null) socket.close();
    }

    public void reload(){
        deinit();
        init();
    }

    public void sendMsgPacket(String message, String host, String port){
        try {
            InetSocketAddress addr = new InetSocketAddress(host, Integer.parseInt(port));
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, addr.getAddress(), addr.getPort());
            socket.send(packet);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
