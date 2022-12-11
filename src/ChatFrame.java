import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * EZ Messenger -- ChatFrame
 *
 * Creates the ChatFrame where users chat in and do message options.
 *
 * @author Shreeya Ettireddy, Ben Sitzman, Caden Edam, lab sec L29
 *
 * @version 12/11/22
 *
 */
public class ChatFrame extends JFrame implements Runnable {
    String username;
    String status;
    String recipient;

    JFrame chatFrame;
    JButton exitButton;
    JButton sendMessageButton;
    JButton editMessageButton;
    JButton deleteMessageButton;
    JButton sendFileButton;
    JButton exportConversationButton;
    JButton refreshChatButton;
    JTextField messageTextField;
    JTextArea chatBox;

    Socket socket;

    public ChatFrame(Socket socket, String username, String status, String recipient) {
        this.socket = socket;
        this.username = username;
        this.status = status;
        this.recipient = recipient;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            chatFrame = new JFrame(username + " & " + recipient);
            chatFrame.setSize(700, 500);
            chatFrame.setLayout(new BorderLayout());
            chatFrame.setLocationRelativeTo(null);

            chatBox = new JTextArea();
            chatBox.setEditable(false);
            JScrollPane scroll = new JScrollPane(chatBox, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            exitButton = new JButton("Exit chat");
            exitButton.addActionListener(actionListener);

            sendMessageButton = new JButton("Send");
            sendMessageButton.addActionListener(actionListener);

            sendFileButton = new JButton("Send .txt file");
            sendFileButton.addActionListener(actionListener);

            editMessageButton = new JButton("Edit message");
            editMessageButton.addActionListener(actionListener);

            deleteMessageButton = new JButton("Delete message");
            deleteMessageButton.addActionListener(actionListener);

            exportConversationButton = new JButton("Export conversation");
            exportConversationButton.addActionListener(actionListener);

            refreshChatButton = new JButton("Refresh chat");
            refreshChatButton.addActionListener(actionListener);

            messageTextField = new JTextField(20);

            JPanel bottom = new JPanel();
            bottom.add(sendMessageButton);
            bottom.add(messageTextField);
            bottom.add(sendFileButton);
            bottom.add(refreshChatButton);

            JPanel top = new JPanel();
            top.add(exitButton);
            top.add(editMessageButton);
            top.add(deleteMessageButton);
            top.add(exportConversationButton);

            chatFrame.add(scroll, BorderLayout.CENTER);
            chatFrame.add(bottom, BorderLayout.SOUTH);
            chatFrame.add(top, BorderLayout.NORTH);

            chatFrame.setVisible(true);
            chatFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

            writer.write("GET CONVERSATION");
            writer.println();
            writer.flush();

            writer.write(username);
            writer.println();
            writer.flush();

            writer.write(recipient);
            writer.println();
            writer.flush();

            String serverResponse = reader.readLine();
            switch (serverResponse) {
                case "CONVERSATION FOUND" -> {
                    StringBuilder conversation = new StringBuilder();

                    String line;
                    while (!(line = reader.readLine()).equals("END;")) {
                        conversation.append(line).append("\n");
                    }

                    chatBox.setText(String.valueOf(conversation));
                }
                case "FILE ERROR" -> {
                    JOptionPane.showMessageDialog(null, "There was an error reading a file!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String serverResponse;

                if (e.getSource() == exitButton) {
                    if (status.equals("customer")) {
                        chatFrame.dispose();
                        SwingUtilities.invokeLater(new CustomerFrame(socket, username));
                    } else if (status.equals("seller")) {
                        chatFrame.dispose();
                        SwingUtilities.invokeLater(new SellerFrame(socket, username));
                    }
                }

                if (e.getSource() == sendMessageButton) {
                    if (messageTextField.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please enter a message!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        writer.write("SEND MESSAGE");
                        writer.println();
                        writer.flush();

                        writer.write(username);
                        writer.println();
                        writer.flush();

                        writer.write(recipient);
                        writer.println();
                        writer.flush();

                        writer.write(messageTextField.getText());
                        writer.println();
                        writer.flush();

                        serverResponse = reader.readLine();
                        switch (serverResponse) {
                            case "MESSAGE SEND SUCCESSFUL" -> {
                                String localDateTime = String.valueOf(LocalDateTime.now());
                                String date = localDateTime.substring(0, localDateTime.indexOf('T'));
                                String time = localDateTime.substring(localDateTime.indexOf('T') + 1,
                                        localDateTime.indexOf('.'));

                                if (chatBox.getText().isEmpty()) {
                                    chatBox.setText(username + " to " + recipient + " @ " + date + " " + time + ": " +
                                            messageTextField.getText());
                                } else {
                                    chatBox.setText(chatBox.getText() + "\n" + username + " to " + recipient + " @ " +
                                            date + " " + time + ": " + messageTextField.getText());
                                }
                                messageTextField.setText("");
                            }
                            case "MESSAGE SEND UNSUCCESSFUL" -> {
                                JOptionPane.showMessageDialog(null, "Message could not be " +
                                        "sent!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }

                if (e.getSource() == refreshChatButton) {
                    writer.write("GET CONVERSATION");
                    writer.println();
                    writer.flush();

                    writer.write(username);
                    writer.println();
                    writer.flush();

                    writer.write(recipient);
                    writer.println();
                    writer.flush();

                    serverResponse = reader.readLine();
                    switch (serverResponse) {
                        case "CONVERSATION FOUND" -> {
                            StringBuilder conversation = new StringBuilder();

                            String line;
                            while (!(line = reader.readLine()).equals("END;")) {
                                conversation.append(line).append("\n");
                            }

                            chatBox.setText(String.valueOf(conversation));
                        }
                        case "FILE ERROR" -> {
                            JOptionPane.showMessageDialog(null, "There was an error reading " +
                                    "a file!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                if (e.getSource() == deleteMessageButton) {
                    writer.write("DELETE MESSAGE");
                    writer.println();
                    writer.flush();

                    writer.write(username);
                    writer.println();
                    writer.flush();

                    writer.write(recipient);
                    writer.println();
                    writer.flush();

                    serverResponse = reader.readLine();
                    switch (serverResponse) {
                        case "MESSAGES FOUND" -> {
                            String[] messagesSent = new String[0];

                            String line;
                            while (!(line = reader.readLine()).equals("END;")) {
                                messagesSent = Arrays.copyOf(messagesSent, messagesSent.length + 1);
                                messagesSent[messagesSent.length - 1] = line;
                            }

                            String messageSelection = selectOption("Which message would you like to delete?",
                                    messagesSent);

                            if (messageSelection != null) {
                                writer.write(messageSelection);
                                writer.println();
                                writer.flush();

                                JOptionPane.showMessageDialog(null, "Message deleted " +
                                        "successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        case "NO MESSAGES SENT" -> {
                            JOptionPane.showMessageDialog(null, "You have not sent any " +
                                    "messages to this user!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        case "FILE ERROR" -> {
                            JOptionPane.showMessageDialog(null, "There was an error reading " +
                                    "a file!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                if (e.getSource() == editMessageButton) {
                    writer.write("EDIT MESSAGE");
                    writer.println();
                    writer.flush();

                    writer.write(username);
                    writer.println();
                    writer.flush();

                    writer.write(recipient);
                    writer.println();
                    writer.flush();

                    serverResponse = reader.readLine();
                    switch (serverResponse) {
                        case "MESSAGES FOUND" -> {
                            String[] messagesSent = new String[0];

                            String line;
                            while (!(line = reader.readLine()).equals("END;")) {
                                messagesSent = Arrays.copyOf(messagesSent, messagesSent.length + 1);
                                messagesSent[messagesSent.length - 1] = line;
                            }

                            String messageSelection = selectOption("Which message would you like to edit?",
                                    messagesSent);

                            if (messageSelection != null) {
                                writer.write(messageSelection);
                                writer.println();
                                writer.flush();

                                String newMessage;
                                do {
                                    newMessage = JOptionPane.showInputDialog(null, "What " +
                                                    "would you like to edit this message to be?", "Edit",
                                            JOptionPane.QUESTION_MESSAGE);
                                    if (newMessage == null) {
                                        break;
                                    } else if (newMessage.isEmpty()) {
                                        JOptionPane.showMessageDialog(null, "Please enter a " +
                                                "new message!", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } while (newMessage.isEmpty());

                                if (newMessage != null) {
                                    writer.write(newMessage);
                                    writer.println();
                                    writer.flush();

                                    JOptionPane.showMessageDialog(null, "Message edited " +
                                            "successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                        case "NO MESSAGES SENT" -> {
                            JOptionPane.showMessageDialog(null, "You have not sent any " +
                                    "messages to this user!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        case "FILE ERROR" -> {
                            JOptionPane.showMessageDialog(null, "There was an error reading " +
                                    "a file!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                if (e.getSource() == sendFileButton) { // TODO
                    JOptionPane.showMessageDialog(null, "This feature has not been " +
                            "implemented yet!", "Error", JOptionPane.ERROR_MESSAGE);
                }

                if (e.getSource() == exportConversationButton) { // TODO
                    JOptionPane.showMessageDialog(null, "This feature has not been " +
                            "implemented yet!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(null, "Connection lost!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    public static String selectOption(String prompt, String[] options) {
        String selection;
        try {
            selection = (String) JOptionPane.showInputDialog(null, prompt, "Options",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        } catch (NullPointerException e) {
            return null;
        }
        return selection;
    }
}