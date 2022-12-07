import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

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
                if (e.getSource() == delete) {
                    pw.write("DeleteMessage");
                    pw.println();
                    pw.flush();

                    pw.write(username);
                    pw.println();
                    pw.flush();

                    pw.write(recipient);
                    pw.println();
                    pw.flush();

                    String response = bfr.readLine();

                    switch (response) {
                        case "Yes" -> {
                            ArrayList<String> messagesSent = new ArrayList<>();

                            String line;
                            while ((line = bfr.readLine()) != null) {
                                if (!line.equals("End")) {
                                    messagesSent.add(line);
                                } else if (line.equals("End")) {
                                    break;
                                }
                            }

                            String[] messageOptions = new String[messagesSent.size()];
                            for (int i = 0; i < messagesSent.size(); i++) {
                                messageOptions[i] = messagesSent.get(i);
                            }

                            String userSelection = selectOption("What message do you want to delete?",
                                    messageOptions, "Choose message");

                            if (userSelection == null) {
                                pw.write("Stop");
                                pw.println();
                                pw.flush();
                                return;
                            } else {
                                pw.write("Continue");
                                pw.println();
                                pw.flush();

                                pw.write(userSelection);
                                pw.println();
                                pw.flush();

                                String deleteResponse = bfr.readLine();
                                if (deleteResponse.equals("Yes")) {
                                    JOptionPane.showMessageDialog(null, "Successfully " +
                                                    "deleted your message. Please refresh to see updated chat",
                                            "Success", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(null, "Something went " +
                                                    "wrong in deleting your message.", "Deletion failure",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }

                        }
                        case "NoMessages" -> {
                            JOptionPane.showMessageDialog(null,
                                    "No messages exist in this chat", "No messages",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        case "SentNone" -> {
                            JOptionPane.showMessageDialog(null, "You have not sent any " +
                                    "messages to delete", "You sent no messages", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                if (e.getSource() == edit) {
                    pw.write("EditMessage");
                    pw.println();
                    pw.flush();

                    pw.write(username);
                    pw.println();
                    pw.flush();

                    pw.write(recipient);
                    pw.println();
                    pw.flush();

                    String response = bfr.readLine();

                    switch (response) {
                        case "NoMessages" -> {
                            JOptionPane.showMessageDialog(null,
                                    "No messages exist in this chat", "No messages",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        case "SentNone" -> {
                            JOptionPane.showMessageDialog(null, "You have not sent any " +
                                    "messages to delete", "You sent no messages", JOptionPane.ERROR_MESSAGE);
                        }
                        case "Yes" -> {
                            ArrayList<String> messagesSent = new ArrayList<>();

                            String line;
                            while ((line = bfr.readLine()) != null) {
                                if (!line.equals("End")) {
                                    messagesSent.add(line);
                                } else if (line.equals("End")) {
                                    break;
                                }
                            }

                            String[] messageOptions = new String[messagesSent.size()];
                            for (int i = 0; i < messagesSent.size(); i++) {
                                messageOptions[i] = messagesSent.get(i);
                            }

                            String userSelection = selectOption("What message do you want to edit?",
                                    messageOptions, "Choose message");

                            if (userSelection == null) {
                                pw.write("Stop");
                                pw.println();
                                pw.flush();
                                return;
                            } else {
                                String changedTo = JOptionPane.showInputDialog(null, "What " +
                                                "do you want to change that message to?", "Edit Message",
                                        JOptionPane.QUESTION_MESSAGE);

                                if (changedTo == null) {
                                    pw.write("Stop");
                                    pw.println();
                                    pw.flush();
                                    return;
                                } else if (changedTo.isEmpty()) {
                                    pw.write("Stop");
                                    pw.println();
                                    pw.flush();
                                    JOptionPane.showMessageDialog(null, "You have not typed " +
                                            "anything", "No input", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    pw.write("Continue");
                                    pw.println();
                                    pw.flush();
                                    pw.write(userSelection);
                                    pw.println();
                                    pw.flush();

                                    pw.write(changedTo);
                                    pw.println();
                                    pw.flush();

                                    String editResponse = bfr.readLine();

                                    if (editResponse.equals("Yes")) {
                                        JOptionPane.showMessageDialog(null, "Successfully " +
                                                        "deleted your message. Please refresh to see updated chat",
                                                "Success", JOptionPane.INFORMATION_MESSAGE);
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Something went " +
                                                        "wrong in deleting your message.", "Deletion failure",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception a) {
                JOptionPane.showMessageDialog(null, "An issue occurred (ChF)", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    public static String selectOption(String prompt, String[] options, String title) {
        String selection;
        try {
            selection = (String) JOptionPane.showInputDialog(null, prompt, title,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        } catch (NullPointerException e) {
            return null;
        }
        return selection;
    }
}
