import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

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
    JTextField messageText;
    JButton sendFile;
    JButton export;
    JButton refresh;
    JTextArea chatBox;

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

        try {
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            frame = new JFrame(username + " and " + recipient);
            frame.setSize(700, 500);
            frame.setLayout(new BorderLayout());
            frame.setLocationRelativeTo(null);

            chatBox = new JTextArea();
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

            export = new JButton("Export conversation");
            export.addActionListener(actionListener);

            refresh = new JButton("Refresh Chat");
            refresh.addActionListener(actionListener);

            messageText = new JTextField(20);

            JPanel bottom = new JPanel();
            bottom.add(send);
            bottom.add(messageText);
            bottom.add(sendFile);
            bottom.add(refresh);

            JPanel top = new JPanel();
            top.add(exit);
            top.add(edit);
            top.add(delete);
            top.add(export);

            frame.add(scroll, BorderLayout.CENTER);
            frame.add(bottom, BorderLayout.SOUTH);
            frame.add(top, BorderLayout.NORTH);

            frame.setVisible(true);
            frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);


            pw.write("ChatRunning");
            pw.println();
            pw.flush();

            pw.write(username);
            pw.println();
            pw.flush();

            pw.write(recipient);
            pw.println();
            pw.flush();

            String addToBox = "";

            String line;
            while ((line = bfr.readLine()) != null) {
                if (!line.equals("End")) {
                    addToBox = addToBox + "\n" + line;
                } else if (line.equals("End")) {
                    break;
                }
            }

            chatBox.setText(addToBox);
        } catch (Exception e) {
            e.printStackTrace();
        }


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
                if (e.getSource() == send) {
                    if (messageText.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "You have not typed in a message",
                                "No message", JOptionPane.ERROR_MESSAGE);
                    } else {
                        pw.write("SendMessage");        //writes command to server
                        pw.println();
                        pw.flush();

                        pw.write(username);                 //writes the client's username
                        pw.println();
                        pw.flush();

                        pw.write(recipient);                //writes who the client is writing to
                        pw.println();
                        pw.flush();

                        pw.write(messageText.getText());       //writes the message client wants to send
                        pw.println();
                        pw.flush();

                        if (bfr.readLine().equals("No")) {
                            JOptionPane.showMessageDialog(null, "Message could not be sent",
                                    "ERROR", JOptionPane.ERROR_MESSAGE);
                        } else {
                            String prevChat = chatBox.getText();
                            prevChat = prevChat + "\n" + username + " to " + recipient + " @" +
                                    String.valueOf(LocalDateTime.now()) + " :" + messageText.getText();
                            chatBox.setText(prevChat);
                            messageText.setText("");

                        }
                    }
                }
                if (e.getSource() == refresh) {
                    pw.write("ChatRunning");
                    pw.println();
                    pw.flush();

                    pw.write(username);
                    pw.println();
                    pw.flush();

                    pw.write(recipient);
                    pw.println();
                    pw.flush();

                    String addToBox = "";

                    String line;
                    while ((line = bfr.readLine()) != null) {
                        if (!line.equals("End")) {
                            addToBox = addToBox + "\n" + line;
                        } else if (line.equals("End")) {
                            break;
                        }
                    }

                    chatBox.setText(addToBox);
                }
            } catch (Exception a) {
                JOptionPane.showMessageDialog(null, "An issue occurred (ChF)", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };
}
