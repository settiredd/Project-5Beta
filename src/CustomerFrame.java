import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;

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
                    String[] editOptions = {"Username", "Password", "Email"};   //user chooses what they want to edit
                    String editSelection = selectOption("What would you like to edit", editOptions,
                            "Edit Account");

                    if (editSelection == null) {
                        return;
                    } else {
                        String newChange = JOptionPane.showInputDialog(null, "What is your new "
                                + editSelection + "?", "Edit " + editSelection, JOptionPane.QUESTION_MESSAGE);

                        if (newChange == null) {           //cancel option
                            return;
                        }
                        if (newChange.isEmpty()) {         //clicks yes but has no text
                            JOptionPane.showMessageDialog(null, "You have not inputted anything"
                                    , "No input", JOptionPane.ERROR_MESSAGE);
                        }

                        boolean validChange = false;
                        if (editSelection.equals("Username") || editSelection.equals("Password")) {         //checks username is valid

                            if (newChange.length() >= 8 && !newChange.contains(" ")
                                    && !newChange.contains(";")) {
                                validChange = true;
                            }
                            if (!validChange) {                     //tells client they put invalid username
                                JOptionPane.showMessageDialog(null, "Input a valid " +
                                                "username/password " + "(at least 8 characters, no semicolons or " +
                                                "spaces).",
                                        "Invalid username/password", JOptionPane.ERROR_MESSAGE);
                            }
                        } else if (editSelection.equals("Email")) {     //checks if email is valid
                            String emailRegex = "^[a-zA-Z0-9_+&*-]+" +
                                    "(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                                    "(?:[a-zA-Z0-9-]+\\.)+" +
                                    "[a-zA-Z]{2,7}$";
                            Pattern checkValidity = Pattern.compile(emailRegex);
                            if (checkValidity.matcher(newChange).matches()) {
                                validChange = true;

                            }
                            if (!validChange) {         //tells client they put invalid email
                                JOptionPane.showMessageDialog(null, "Enter a valid email",
                                        "Invalid email", JOptionPane.ERROR_MESSAGE);
                            }
                        }

                        if (validChange) {
                            pw.write("Edit");           //writes over the command to server
                            pw.println();
                            pw.flush();

                            pw.write(username);             //writes over username to server
                            pw.println();
                            pw.flush();

                            pw.write(editSelection);        //writes over what the user wants to edit
                            pw.println();
                            pw.flush();

                            pw.write(newChange);            //writes over what the user is changing it to
                            pw.println();
                            pw.flush();

                            String response = bfr.readLine();

                            switch (response) {
                                case "Yes" -> {
                                    JOptionPane.showMessageDialog(null, "Successfully edited!",
                                            "Success!", JOptionPane.INFORMATION_MESSAGE);
                                    if (editSelection.equals("Username")) {
                                        username = newChange;
                                    }
                                }
                                case "Same" -> {
                                    JOptionPane.showMessageDialog(null, "This is your current "
                                            + editSelection, "Unable to edit!", JOptionPane.ERROR_MESSAGE);
                                }
                                case "Used" -> {
                                    JOptionPane.showMessageDialog(null, "This " +
                                                    editSelection + " is being used!", "Unable to edit!",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }

                } else if (e.getSource() == delete) {

                } else if (e.getSource() == logout) {

                }
            } catch (Exception a) {
                JOptionPane.showMessageDialog(null, "An error has occurred! (CuF)",
                        "Error", JOptionPane.ERROR_MESSAGE);
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
