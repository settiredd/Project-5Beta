import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * EZ Messenger -- CustomerFrame
 *
 * Creates a frame for customer users.
 *
 * @author Shreeya Ettireddy, Ben Sitzman, Caden Edam, lab sec L29
 *
 * @version 12/11/22
 *
 */
public class CustomerFrame extends JFrame implements Runnable {
    String username;

    JFrame customerFrame;
    JButton messageButton;
    JButton blockButton;
    JButton dashboardButton;
    JButton editAccountButton;
    JButton deleteAccountButton;
    JButton logoutButton;

    Socket socket;

    public CustomerFrame(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }

    @Override
    public void run() {
        customerFrame = new JFrame("Customer Options");
        customerFrame.setSize(400, 300);
        customerFrame.setLocationRelativeTo(null);
        customerFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel prompt = new JLabel("What would you like to do?");
        messageButton = new JButton("Message a store");
        messageButton.addActionListener(actionListener);

        blockButton = new JButton("Block/unblock a seller");
        blockButton.addActionListener(actionListener);

        dashboardButton = new JButton("View dashboard");
        dashboardButton.addActionListener(actionListener);

        editAccountButton = new JButton("Edit account");
        editAccountButton.addActionListener(actionListener);

        deleteAccountButton = new JButton("Delete account");
        deleteAccountButton.addActionListener(actionListener);

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(actionListener);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        customerFrame.add(prompt, gbc);

        gbc.gridy = 1;
        gbc.gridheight = 1;
        customerFrame.add(messageButton, gbc);

        gbc.gridy = 2;
        customerFrame.add(blockButton, gbc);

        gbc.gridy = 3;
        customerFrame.add(dashboardButton, gbc);

        gbc.gridy = 4;
        customerFrame.add(editAccountButton, gbc);

        gbc.gridy = 5;
        customerFrame.add(deleteAccountButton, gbc);

        gbc.gridy = 6;
        customerFrame.add(logoutButton, gbc);

        customerFrame.setVisible(true);
        customerFrame.setResizable(false);
        customerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());

                String serverResponse;

                if (e.getSource() == messageButton) {
                    String[] searchOptions = {"Select a store", "Search for a store"};
                    String searchSelection = selectOption("What would you like to do?", searchOptions);

                    if (searchSelection != null) {
                        switch (searchSelection) {
                            case "Select a store" -> {
                                writer.write("MESSAGE OPTIONS");
                                writer.println();
                                writer.flush();

                                writer.write(username);
                                writer.println();
                                writer.flush();

                                serverResponse = reader.readLine();
                                switch (serverResponse) {
                                    case "STORES FOUND" -> {
                                        String[] stores = new String[0];

                                        String line;
                                        while (!(line = reader.readLine()).equals("END;")) {
                                            stores = Arrays.copyOf(stores, stores.length + 1);
                                            stores[stores.length - 1] = line;
                                        }

                                        String storeSelection = selectOption("Which store would you like " +
                                                "to message?", stores);

                                        if (storeSelection != null) {
                                            writer.write("SEARCH");
                                            writer.println();
                                            writer.flush();

                                            writer.write(username);
                                            writer.println();
                                            writer.flush();

                                            writer.write(storeSelection);
                                            writer.println();
                                            writer.flush();

                                            serverResponse = reader.readLine();
                                            switch (serverResponse) {
                                                case "BLOCKED YOU" -> {
                                                    JOptionPane.showMessageDialog(null,
                                                            "This seller has blocked you!", "Error",
                                                            JOptionPane.ERROR_MESSAGE);
                                                }
                                                case "YOU BLOCKED" -> {
                                                    JOptionPane.showMessageDialog(null, "You " +
                                                                    "have blocked this seller!", "Error",
                                                            JOptionPane.ERROR_MESSAGE);
                                                }
                                                case "FILE ERROR" -> {
                                                    JOptionPane.showMessageDialog(null,
                                                            "There was an error reading a file!", "Error",
                                                            JOptionPane.ERROR_MESSAGE);
                                                }
                                                default -> {
                                                    customerFrame.dispose();

                                                    SwingUtilities.invokeLater(new ChatFrame(socket, username,
                                                            "customer", serverResponse));
                                                }
                                            }
                                        }
                                    }
                                    case "NO STORES" -> {
                                        JOptionPane.showMessageDialog(null, "There are no " +
                                                        "stores for you to message at this time!", "Error",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                    case "FILE ERROR" -> {
                                        JOptionPane.showMessageDialog(null, "There was an " +
                                                "error reading a file!", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                            case "Search for a store" -> {
                                String storeToMessage;
                                do {
                                    storeToMessage = JOptionPane.showInputDialog(null,
                                            "Enter the name of the store you would like to message:",
                                            "Search", JOptionPane.QUESTION_MESSAGE);
                                    if (storeToMessage == null) {
                                        break;
                                    } else if (storeToMessage.isEmpty()) {
                                        JOptionPane.showMessageDialog(null, "Please enter a " +
                                                "store name!", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } while (storeToMessage.isEmpty());

                                if (storeToMessage != null) {
                                    writer.write("SEARCH");
                                    writer.println();
                                    writer.flush();

                                    writer.write(username);
                                    writer.println();
                                    writer.flush();

                                    writer.write(storeToMessage);
                                    writer.println();
                                    writer.flush();

                                    serverResponse = reader.readLine();
                                    switch (serverResponse) {
                                        case "STORE NOT FOUND" -> {
                                            JOptionPane.showMessageDialog(null, "No stores " +
                                                            "exist with this name!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        case "BLOCKED YOU" -> {
                                            JOptionPane.showMessageDialog(null, "This seller " +
                                                            "has blocked you!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        case "YOU BLOCKED" -> {
                                            JOptionPane.showMessageDialog(null, "You have " +
                                                            "blocked this seller!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        case "FILE ERROR" -> {
                                            JOptionPane.showMessageDialog(null, "There was " +
                                                            "an error reading a file!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        default -> {
                                            customerFrame.dispose();

                                            SwingUtilities.invokeLater(new ChatFrame(socket, username,
                                                    "customer", serverResponse));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (e.getSource() == blockButton) {
                    String[] blockOptions = {"Block/unblock a seller", "Become visible/invisible to a seller"};
                    String blockSelection = selectOption("What would you like to do?", blockOptions);

                    if (blockSelection != null) {
                        switch (blockSelection) {
                            case "Block/unblock a seller" -> {
                                String userToBlock;
                                do {
                                    userToBlock = JOptionPane.showInputDialog(null,
                                            "Enter the username of the seller you would like to block or " +
                                                    "unblock:", "Update Block Status",
                                            JOptionPane.QUESTION_MESSAGE);
                                    if (userToBlock == null) {
                                        break;
                                    } else if (userToBlock.isEmpty()) {
                                        JOptionPane.showMessageDialog(null, "Please enter a " +
                                                "username!", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } while (userToBlock.isEmpty());

                                if (userToBlock != null) {
                                    writer.write("UPDATE BLOCK STATUS");
                                    writer.println();
                                    writer.flush();

                                    writer.write(username);
                                    writer.println();
                                    writer.flush();

                                    writer.write(userToBlock);
                                    writer.println();
                                    writer.flush();

                                    serverResponse = reader.readLine();
                                    switch (serverResponse) {
                                        case "USER NOT FOUND" -> {
                                            JOptionPane.showMessageDialog(null, "No sellers " +
                                                            "exist with this username!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        case "FILE ERROR" -> {
                                            JOptionPane.showMessageDialog(null, "There was " +
                                                            "an error reading a file!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        default -> {
                                            JOptionPane.showMessageDialog(null, "You have " +
                                                            "successfully " + serverResponse +
                                                            " " + userToBlock + "!",
                                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    }
                                }
                            }
                            case "Become visible/invisible to a seller" -> {
                                String userToBecomeInvisibleTo;
                                do {
                                    userToBecomeInvisibleTo = JOptionPane.showInputDialog(null,
                                            "Enter the username of the seller you would like to become " +
                                                    "visible or invisible to:", "Update Visibility",
                                            JOptionPane.QUESTION_MESSAGE);
                                    if (userToBecomeInvisibleTo == null) {
                                        break;
                                    } else if (userToBecomeInvisibleTo.isEmpty()) {
                                        JOptionPane.showMessageDialog(null, "Please enter a " +
                                                "username!", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } while (userToBecomeInvisibleTo.isEmpty());

                                if (userToBecomeInvisibleTo != null) {
                                    writer.write("UPDATE VISIBILITY");
                                    writer.println();
                                    writer.flush();

                                    writer.write(username);
                                    writer.println();
                                    writer.flush();

                                    writer.write(userToBecomeInvisibleTo);
                                    writer.println();
                                    writer.flush();

                                    serverResponse = reader.readLine();
                                    switch (serverResponse) {
                                        case "USER NOT FOUND" -> {
                                            JOptionPane.showMessageDialog(null, "No sellers " +
                                                            "exist with this username!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        case "FILE ERROR" -> {
                                            JOptionPane.showMessageDialog(null, "There was " +
                                                            "an error reading a file!", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        default -> {
                                            JOptionPane.showMessageDialog(null, "You have " +
                                                            "successfully become " + serverResponse + " to " +
                                                            userToBecomeInvisibleTo + "!", "Success",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (e.getSource() == dashboardButton) {
                    JOptionPane.showMessageDialog(null, "This feature has not been " +
                            "implemented yet!", "Error", JOptionPane.ERROR_MESSAGE);
                }

                if (e.getSource() == editAccountButton) {
                    String[] editOptions = {"Username", "Password", "Email"};
                    String editSelection = selectOption("What would you like to edit?", editOptions);

                    if (editSelection != null) {
                        String newInfo;
                        do {
                            newInfo = JOptionPane.showInputDialog(null, "Enter your new " +
                                    editSelection.toLowerCase() + ":", "Edit", JOptionPane.QUESTION_MESSAGE);
                            if (newInfo == null) {
                                break;
                            } else if (newInfo.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Please enter a new " +
                                        editSelection.toLowerCase() + "!", "Error", JOptionPane.ERROR_MESSAGE);
                            } else if (editSelection.equals("Username") || editSelection.equals("Password")) {
                                if (newInfo.length() < 8 || newInfo.contains(" ") || newInfo.contains(";")) {
                                    JOptionPane.showMessageDialog(null, "Please enter a " +
                                            "valid " + editSelection.toLowerCase() + " (at least 8 characters, " +
                                            "no semicolons or spaces).", "Error", JOptionPane.ERROR_MESSAGE);
                                    newInfo = "";
                                }
                            } else if (editSelection.equals("Email")) {
                                String emailRegex = "^[a-zA-Z0-9_+&*-]+" +
                                        "(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                                        "(?:[a-zA-Z0-9-]+\\.)+" +
                                        "[a-zA-Z]{2,7}$";
                                Pattern checkValidity = Pattern.compile(emailRegex);

                                if (!checkValidity.matcher(newInfo).matches()) {
                                    JOptionPane.showMessageDialog(null, "Please enter a " +
                                            "valid email!", "Error", JOptionPane.ERROR_MESSAGE);
                                    newInfo = "";
                                }
                            }
                        } while (newInfo.isEmpty());

                        if (newInfo != null) {
                            writer.write("EDIT ACCOUNT");
                            writer.println();
                            writer.flush();

                            writer.write(username);
                            writer.println();
                            writer.flush();

                            writer.write(editSelection.toUpperCase());
                            writer.println();
                            writer.flush();

                            writer.write(newInfo);
                            writer.println();
                            writer.flush();

                            serverResponse = reader.readLine();
                            switch (serverResponse) {
                                case "ACCOUNT EDIT SUCCESSFUL" -> {
                                    if (editSelection.equals("Username")) {
                                        username = newInfo;
                                    }
                                    JOptionPane.showMessageDialog(null, editSelection +
                                                    " edited successfully!", "Success",
                                            JOptionPane.INFORMATION_MESSAGE);
                                }
                                case "NO CHANGE" -> {
                                    JOptionPane.showMessageDialog(null, "The " +
                                                    editSelection.toLowerCase() + " you entered is the same " +
                                                    "as your current " + editSelection.toLowerCase() + "!",
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                case "NEW INFO TAKEN" -> {
                                    if (editSelection.equals("Username")) {
                                        JOptionPane.showMessageDialog(null, "The username " +
                                                "you entered is taken!", "Error", JOptionPane.ERROR_MESSAGE);
                                    } else {
                                        JOptionPane.showMessageDialog(null, "The email you " +
                                                        "entered is already being used!", "Error",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                case "FILE ERROR" -> {
                                    JOptionPane.showMessageDialog(null, "There was an error " +
                                            "reading a file!", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                }

                if (e.getSource() == deleteAccountButton) {
                    int confirmDelete = JOptionPane.showConfirmDialog(null,
                            "Are you sure you would like to delete your account?",
                            "Delete Account", JOptionPane.YES_NO_OPTION);

                    if (confirmDelete == 0) {
                        writer.write("DELETE ACCOUNT");
                        writer.println();
                        writer.flush();

                        writer.write(username);
                        writer.println();
                        writer.flush();

                        serverResponse = reader.readLine();
                        switch (serverResponse) {
                            case "ACCOUNT DELETION SUCCESSFUL" -> {
                                JOptionPane.showMessageDialog(null, "Your account was " +
                                        "deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                customerFrame.dispose();
                                SwingUtilities.invokeLater(new LoginFrame(socket));
                            }
                            case "FILE ERROR" -> {
                                JOptionPane.showMessageDialog(null, "There was an error " +
                                        "reading a file!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }

                if (e.getSource() == logoutButton) {
                    customerFrame.dispose();
                    SwingUtilities.invokeLater(new LoginFrame(socket));
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