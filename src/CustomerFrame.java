import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class CustomerFrame extends JFrame implements Runnable {
    Socket socket;
    String username;
    JFrame frame;
    JButton message;
    JButton block;
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
        frame.add(dash, gbc);

        gbc.gridy = 4;
        frame.add(edit, gbc);

        gbc.gridy = 5;
        frame.add(delete, gbc);

        gbc.gridy = 6;
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

                            pw.write("Search");           //writes command to server
                            pw.println();
                            pw.flush();

                            pw.write(username);
                            pw.println();
                            pw.flush();

                            pw.write(search);           //writes over user client is searching for
                            pw.println();
                            pw.flush();

                            pw.write("customer");           //writes over user status
                            pw.println();
                            pw.flush();

                            String response = bfr.readLine();

                            switch (response) {
                                case "No" -> {
                                    JOptionPane.showMessageDialog(null, "This store doesn't " +
                                            "exist", "Not Existing", JOptionPane.ERROR_MESSAGE);
                                }
                                case "Blocked" -> {
                                    JOptionPane.showMessageDialog(null, "This store seller has"
                                                    + " blocked you/You have blocked this store seller",
                                            "Block Error", JOptionPane.ERROR_MESSAGE);
                                }
                                case "Yes" -> {
                                    String storeSeller = bfr.readLine();        //gets store seller name
                                    frame.dispose();
                                    SwingUtilities.invokeLater(new ChatFrame(socket, username, "customer",
                                            storeSeller, "seller"));
                                }
                            }
                        } else if (choice.equals("1")) {            //no - view list
                            pw.write("MessageOptions");     //writes command to server
                            pw.println();
                            pw.flush();

                            pw.write(username);                 //writes username to server
                            pw.println();
                            pw.flush();

                            pw.write("customer");           //writes status to customer
                            pw.println();
                            pw.flush();

                            String serverResponse = bfr.readLine();

                            switch (serverResponse) {
                                case "None" -> {
                                    JOptionPane.showConfirmDialog(null, "No stores to message "
                                            + "at this time", "No stores to message", JOptionPane.PLAIN_MESSAGE);
                                }
                                case "Yes" -> {
                                    ArrayList<String> stores = new ArrayList<>();

                                    String line;
                                    while ((line = bfr.readLine()) != null) {
                                        if (!line.equals("End")) {
                                            stores.add(line);
                                        } else if (line.equals("End")) {
                                            break;
                                        }
                                    }

                                    String[] customerOptions = new String[stores.size()];
                                    for (int i = 0; i < stores.size(); i++) {
                                        customerOptions[i] = stores.get(i);
                                    }

                                    String storeSelection = selectOption("Who do you want to message?",
                                            customerOptions, "Choose store");

                                    if (storeSelection == null) {
                                        return;
                                    } else {
                                        pw.write("Search");
                                        pw.println();
                                        pw.flush();

                                        pw.write(username);
                                        pw.println();
                                        pw.flush();

                                        pw.write(storeSelection);
                                        pw.println();
                                        pw.flush();

                                        pw.write("customer");
                                        pw.println();
                                        pw.flush();

                                        String response = bfr.readLine();

                                        if (response.equals("Yes")) {
                                            String storeSeller = bfr.readLine();
                                            SwingUtilities.invokeLater(new ChatFrame(socket, username,
                                                    "customer", storeSeller, "seller"));
                                            frame.dispose();
                                        } else if (response.equals("blocked")) {
                                            JOptionPane.showMessageDialog(null, "This user has"
                                                            + " blocked you/You have blocked this user",
                                                    "Block Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }


                                }
                            }


                        }
                    }
                } else if (e.getSource() == block) {
                    String[] options = {"Block", "Invisible"};
                    String choice = String.valueOf(JOptionPane.showOptionDialog(null, "Do you "
                                    + "want to block a customer or become invisible to them?",
                            "Blocking/Invisibility", 0,
                            JOptionPane.INFORMATION_MESSAGE, null, options, null));

                    //option 0: block, option 1: invisible
                    if (choice == null) {
                        return;
                    } else if (choice.isEmpty()) {
                        return;
                    } else if (choice.equals("0")) {
                        String blocked = JOptionPane.showInputDialog(null, "What user do you " +
                                "want to block?", "Block Customer", JOptionPane.QUESTION_MESSAGE);

                        if (blocked == null) {
                            return;
                        } else if (blocked.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "You have not typed anything",
                                    "No Input", JOptionPane.ERROR_MESSAGE);
                        } else {
                            pw.write("Block");          //writes command to server
                            pw.println();
                            pw.flush();

                            pw.write(username);             //writes username to server
                            pw.println();
                            pw.flush();

                            pw.write(blocked);              //writes over who client wants to block
                            pw.println();
                            pw.flush();

                            String response = bfr.readLine();   //gets response from server
                            switch (response) {
                                case "No" -> {
                                    JOptionPane.showMessageDialog(null, "This user does not" +
                                            " exist!", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                case "Already" -> {
                                    JOptionPane.showMessageDialog(null, "You have already" +
                                            " blocked this user!", "Already blocked!", JOptionPane.ERROR_MESSAGE);
                                }
                                case "Yes" -> {
                                    JOptionPane.showMessageDialog(null, "You have blocked " +
                                            blocked + "!", "Success!", JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }

                    } else if (choice.equals("1")) {
                        String invisibleTo = JOptionPane.showInputDialog(null, "What user " +
                                        "do you want to be invisible to?", "Invisibility mode",
                                JOptionPane.QUESTION_MESSAGE);

                        if (invisibleTo == null) {
                            return;
                        } else if (invisibleTo.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "You have not typed anything",
                                    "No Input", JOptionPane.ERROR_MESSAGE);
                        } else {
                            pw.write("Invisible");          //writes command to server
                            pw.println();
                            pw.flush();

                            pw.write(username);             //writes username to server
                            pw.println();
                            pw.flush();

                            pw.write(invisibleTo);              //writes over who client wants to block
                            pw.println();
                            pw.flush();

                            String response = bfr.readLine();   //gets response from server
                            switch (response) {
                                case "No" -> {
                                    JOptionPane.showMessageDialog(null, "This user does not" +
                                            " exist!", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                case "Already" -> {
                                    JOptionPane.showMessageDialog(null, "You are already " +
                                                    "invisible to this user!",
                                            "Already Invisible!", JOptionPane.ERROR_MESSAGE);
                                }
                                case "Yes" -> {
                                    JOptionPane.showMessageDialog(null, "You are invisible to "
                                            + invisibleTo + "!", "Success!", JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                    }
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
                        if (editSelection.equals("Username") || editSelection.equals("Password")) {
                            //checks username is valid

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

                            pw.write("customer");
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
                                    frame.setTitle("Hello " + username);
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
                    String deleteConfirm = String.valueOf(JOptionPane.showConfirmDialog(null,
                            "Are you sure you want to delete your account?",
                            "Account Deletion", JOptionPane.YES_NO_OPTION));

                    if (deleteConfirm == null) {
                        return;
                    } else {
                        if (deleteConfirm.isEmpty()) {
                            return;
                        }
                    }
                    if (deleteConfirm.equals("0")) {
                        pw.write("Delete");
                        pw.println();
                        pw.flush();

                        pw.write(username);
                        pw.println();
                        pw.flush();

                        String response = bfr.readLine();
                        if (response.equals("Success")) {
                            JOptionPane.showMessageDialog(null, "Your account was deleted",
                                    "Success!", JOptionPane.INFORMATION_MESSAGE);
                            frame.dispose();
                            SwingUtilities.invokeLater(new LoginFrame(socket));
                        } else {
                            JOptionPane.showMessageDialog(null, "Unable to delete account",
                                    "Deletion Failure", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                } else if (e.getSource() == logout) {
                    frame.dispose();
                    SwingUtilities.invokeLater(new LoginFrame(socket));
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
