import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CustomerFrame extends JFrame implements Runnable {
    Socket socket;
    String username;
    JFrame frame;
    JButton message;
    JButton block;
    JButton convos;
    JButton dash;
    JButton edit;
    JButton delete;
    JButton logout;

    public CustomerFrame(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }

    @Override
    public void run() {
        frame = new JFrame("Hello " + username);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel welcome = new JLabel("What do you want to do?");
        message = new JButton("Message a Store");
        message.addActionListener(actionListener);

        block = new JButton("Block a Seller");
        block.addActionListener(actionListener);

        convos = new JButton("View Conversations");
        convos.addActionListener(actionListener);

        dash = new JButton("View Dashboard");
        dash.addActionListener(actionListener);

        edit = new JButton("Edit Account");
        edit.addActionListener(actionListener);

        delete = new JButton("Delete Account");
        delete.addActionListener(actionListener);

        logout = new JButton("Logout");
        logout.addActionListener(actionListener);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        frame.add(welcome, gbc);

        gbc.gridy = 1;
        gbc.gridheight = 1;
        frame.add(message, gbc);

        gbc.gridy = 2;
        frame.add(block, gbc);

        gbc.gridy = 3;
        frame.add(convos, gbc);

        gbc.gridy = 4;
        frame.add(dash, gbc);

        gbc.gridy = 5;
        frame.add(edit, gbc);

        gbc.gridy = 6;
        frame.add(delete, gbc);

        gbc.gridy = 7;
        frame.add(logout, gbc);

        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if (e.getSource() == message) {
                    String choice = String.valueOf(JOptionPane.showConfirmDialog(null,
                            "Click Yes to search for store.\nClick No to view a list of stores.",
                            "Search Prompt", JOptionPane.YES_NO_OPTION));

                    if (choice == null) {
                        return;
                    } else {
                        if (choice.isEmpty()) {
                            return;
                        } else if (choice.equals("0")) {            //yes - search
                            String search = JOptionPane.showInputDialog(null,
                                    "Type in an exact store name", "Search for Store",
                                    JOptionPane.QUESTION_MESSAGE);

                            if (search == null) {           //cancel option
                                return;
                            }
                            if (search.isEmpty()) {         //clicks yes but has no text
                                JOptionPane.showMessageDialog(null, "Type in a store " +
                                        "name", "No input", JOptionPane.ERROR_MESSAGE);
                            }
                        } else if (choice.equals("1")) {            //no - view list
                            pw.write("MessageOptions");     //writes command to server
                            pw.println();
                            pw.flush();

                            pw.write(username);
                            pw.println();
                            pw.flush();

                        }
                    }
                } else if (e.getSource() == block) {

                } else if (e.getSource() == convos) {

                } else if (e.getSource() == dash) {

                } else if (e.getSource() == edit) {

                } else if (e.getSource() == delete) {

                } else if (e.getSource() == logout) {

                }
            } catch (Exception a) {
                JOptionPane.showMessageDialog(null, "An error has occurred! (CuF)",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    };


    public void run1() {
        try {
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String[] loginOptions = new String[]{"Message Store", "Block Seller",
                    "View Conversations", "View Dashboard", "Edit Account", "Delete Account", "Logout"};

            String userSelection = selectOption("What would you like to do?", loginOptions,
                    "Hello " + username);

            if (userSelection == null) {
                return;
            }

            switch (userSelection) {
                case "Message Store" -> {
                    String choice = String.valueOf(JOptionPane.showConfirmDialog(null,
                            "Do you want to search for a customer? Click no to view a list of customers",
                            "Search Prompt", JOptionPane.YES_NO_OPTION));
                }
                case "Block Seller" -> {
                    //TODO
                }
                case "View Conversations" -> {
                    //TODO
                }
                case "View Dashboard" -> {
                    //TODO
                }
                case "Edit Account" -> {
                    //TODO
                }
                case "Delete Account" -> {
                    //TODO
                }
                case "Logout" -> {
                    //TODO
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
