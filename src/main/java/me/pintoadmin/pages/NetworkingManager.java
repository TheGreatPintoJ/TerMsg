package me.pintoadmin.pages;

import me.pintoadmin.MessageManager;

import java.io.IOException;
import java.net.*;
import java.nio.Buffer;

public class NetworkingManager {
    public static int PORT = 51337;
    private final int BUFSIZE = 1024;
    public NetworkingManager(){
        init();
    }

    private Thread socketThread;
    private DatagramSocket socket;

    public void init(){
        socketThread = new Thread(() -> {
            try {
                socket = new DatagramSocket(PORT);
                byte[] buf = new byte[BUFSIZE];
                while (!Thread.currentThread().isInterrupted()){
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength());
                    InetSocketAddress addr = new InetSocketAddress(packet.getAddress(), packet.getPort());

                    if(MessageManager.getInstance().newChat(addr.getHostName(), String.valueOf(PORT), addr.getHostString())){
                        MessageManager.getInstance().recieveMessage(msg, addr.getHostName());
                    }
                    buf = new byte[BUFSIZE];
                }
            } catch (BindException ignored){
                throw new RuntimeException("There's another instance running!");
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
            DatagramPacket packet = new DatagramPacket(message.getBytes(), 0, addr.getAddress(), addr.getPort());
            socket.send(packet);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
