import javax.swing.*;
import java.awt.*;
import java.net.Socket;

public class ChatFrame extends JFrame implements Runnable {
    Socket socket;
    String username;
    String userStatus;
    String recipient;
    String recipientStatus;
    JFrame frame;
    JButton exit;
    JButton send;
    JButton edit;
    JButton delete;
    JTextField message;

    public ChatFrame(Socket socket, String username, String userStatus, String recipient,
                     String recipientStatus) {
        this.socket = socket;
        this.username = username;
        this.userStatus = userStatus;
        this.recipient = recipient;
        this.recipientStatus = recipientStatus;
    }
    @Override
    public void run() {
        frame = new JFrame(username + " and " + recipient);
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JTextArea chatBox = new JTextArea();
        chatBox.setEditable(false);
        JScrollPane scroll = new JScrollPane(chatBox, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        exit = new JButton("Exit Chat");
        send = new JButton("Send");
        edit = new JButton("Edit Message");
        delete = new JButton("Delete Message");
        message = new JTextField(20);

        JPanel bottom = new JPanel();
        bottom.add(send);
        bottom.add(message);

        JPanel top = new JPanel();
        top.add(exit);
        top.add(edit);
        top.add(delete);

        frame.add(scroll, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);
        frame.add(top, BorderLayout.NORTH);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
