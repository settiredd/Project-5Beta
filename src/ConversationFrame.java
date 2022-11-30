import javax.swing.*;
import java.net.Socket;

public class ConversationFrame extends JFrame implements Runnable {
    Socket socket;
    String username;
    String userStatus;
    String recipient;
    String recipientStatus;

    public ConversationFrame(Socket socket, String username, String userStatus, String recipient,
                             String recipientStatus) {
        this.socket = socket;
        this.username = username;
        this.userStatus = userStatus;
        this.recipient = recipient;
        this.recipientStatus = recipientStatus;
    }
    @Override
    public void run() {

    }
}
