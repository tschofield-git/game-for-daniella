package danya.net;

import javafx.scene.input.KeyEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

public class Client {

    Socket socket;
    DataOutputStream dOut;
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    public Client(Server server){
        new Client(server.getServerAddress().getHostAddress(), server.getServerPort());
    }

    public Client(String hostIP, int port){
        LOGGER.info("Connecting to " + hostIP + " on port " + port);
        try {
            socket = new Socket(InetAddress.getByName(hostIP), port);
            dOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void sendKeyEvent(KeyEvent keyEvent){
        LOGGER.info("Sending keyEvent " + keyEvent.toString());
        try {
            dOut.writeUTF(keyEvent.getCode().toString());
            dOut.flush();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void closeClientConnection(){
        LOGGER.info("Closing client connection");
        try {
            socket.close();
            dOut.writeByte(-1);
            dOut.close();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

}
