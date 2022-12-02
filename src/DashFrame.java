import javax.swing.*;
import java.net.Socket;

public class DashFrame extends JFrame implements Runnable{
    Socket socket;
    String username;
    String status;

    public DashFrame(Socket socket, String username, String status) {
        this.socket = socket;
        this.username = username;
        this.status = status;
    }

    @Override
    public void run() {

    }
}
