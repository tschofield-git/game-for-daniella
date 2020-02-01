package danya.net;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class ConnectionDetails {

    InetAddress hostAddress;
    int portNumber;

    public ConnectionDetails(){}

    public ConnectionDetails(InetAddress hostAddress, int portNumber){
        this.hostAddress = hostAddress;
        this.portNumber = portNumber;
    }

    public ConnectionDetails(Socket socket){
        this.hostAddress = socket.getInetAddress();
        this.portNumber = socket.getLocalPort();
    }

    public InetAddress getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    public void setHostAddressFromHostname(String hostName){
        try {
            this.hostAddress = InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

}
