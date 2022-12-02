import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    JButton sendFile;
    JButton exportConvo;

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
        exit.addActionListener(actionListener);

        send = new JButton("Send");
        send.addActionListener(actionListener);
        sendFile = new JButton("Send txt File");
        sendFile.addActionListener(actionListener);

        edit = new JButton("Edit Message");
        edit.addActionListener(actionListener);

        delete = new JButton("Delete Message");
        delete.addActionListener(actionListener);

        message = new JTextField(20);

        JPanel bottom = new JPanel();
        bottom.add(send);
        bottom.add(message);
        bottom.add(sendFile);

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

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if (e.getSource() == exit) {                //takes user back to menu frame
                    if (userStatus.equals("seller")) {
                        frame.dispose();
                        SwingUtilities.invokeLater(new SellerFrame(socket, username));
                    } else if (userStatus.equals("customer")) {
                        frame.dispose();
                        SwingUtilities.invokeLater(new CustomerFrame(socket, username));
                    }
                }
            } catch (Exception a) {
                JOptionPane.showMessageDialog(null, "An issue occurred (ChF)", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };
}
