import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class SellerFrame extends JFrame implements Runnable {
    Socket socket;
    String username;
    JFrame frame;
    JButton store;
    JButton message;
    JButton block;
    JButton convos;
    JButton dash;
    JButton edit;
    JButton delete;
    JButton logout;

    public SellerFrame(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }

    public void run() {
        frame = new JFrame("Hello " + username);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel welcome = new JLabel("What do you want to do?");
        store = new JButton("Create New Store");
        store.addActionListener(actionListener);

        message = new JButton("Message a Customer");
        message.addActionListener(actionListener);

        block = new JButton("Block a Customer");
        block.addActionListener(actionListener);

        convos = new JButton("View a Conversation");
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
        frame.add(store, gbc);

        gbc.gridy = 2;
        frame.add(message, gbc);

        gbc.gridy = 3;
        frame.add(block, gbc);

        gbc.gridy = 4;
        frame.add(convos, gbc);

        gbc.gridy = 5;
        frame.add(dash, gbc);

        gbc.gridy = 6;
        frame.add(edit, gbc);

        gbc.gridy = 7;
        frame.add(delete, gbc);

        gbc.gridy = 8;
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

                if (e.getSource() == store) {
                    //TODO
                    System.out.println("store");
                } else if (e.getSource() == message) {
                    //TODO
                    String choice = String.valueOf(JOptionPane.showConfirmDialog(null,
                            "Click Yes to search for customer.\nClick No to view a list of customers.",
                            "Search Prompt", JOptionPane.YES_NO_OPTION));

                    if (choice == null) {
                        return;
                    } else {
                        if (choice.isEmpty()) {
                            return;
                        } else if (choice.equals("0")) {            //yes - search
                            String search = JOptionPane.showInputDialog(null,
                                    "Type in customer's exact username", "Search for Customer",
                                    JOptionPane.QUESTION_MESSAGE);

                            if (search == null) {           //cancel option
                                return;
                            }
                            if (search.isEmpty()) {         //clicks yes but has no text
                                JOptionPane.showMessageDialog(null, "Type in a customer " +
                                        "username", "No input", JOptionPane.ERROR_MESSAGE);
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

                        } else if (choice.equals("1")) {            //no - view list
                            pw.write("MessageOptions");         //writes command to server
                            pw.println();
                            pw.flush();

                            pw.write(username);
                            pw.println();
                            pw.flush();

                            String serverResponse = bfr.readLine();
                            if (serverResponse.equals("None")) {
                                JOptionPane.showConfirmDialog(null, "No customers to message " +
                                        "at this time", "No Customers to message", JOptionPane.PLAIN_MESSAGE);
                            } else if (serverResponse.equals("Yes")) {
                                ArrayList<String> customers = new ArrayList<>();

                                String line;
                                while ((line = bfr.readLine()) != null) {
                                    if (!line.equals("End")) {
                                        customers.add(line);
                                    } else if (line.equals("End")) {
                                        break;
                                    }
                                }

                                String[] customerOptions = new String[customers.size()];
                                for (int i = 0; i < customers.size(); i++) {
                                    customerOptions[i] = customers.get(i);
                                }

                                String customerSelection = selectOption("Who do you want to message?",
                                        customerOptions, "Choose customer");

                                if (customerSelection == null) {
                                    return;
                                } else {
                                    pw.write("Search");
                                    pw.println();
                                    pw.flush();

                                    if (!bfr.readLine().equals("blocked")) {
                                        SwingUtilities.invokeLater(new ChatFrame(socket, username,
                                                "seller", customerSelection, "customer"));
                                        frame.dispose();
                                    } else {
                                        JOptionPane.showMessageDialog(null, "This user has" +
                                                        " blocked you/You have blocked this user", "Block Error",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                        }
                    }
                } else if (e.getSource() == block) {
                    //TODO
                    System.out.println("block");
                } else if (e.getSource() == convos) {
                    //TODO
                    System.out.println("convos");
                } else if (e.getSource() == dash) {
                    //TODO
                    System.out.println("dash");
                } else if (e.getSource() == edit) {
                    //TODO
                    System.out.println("edit");
                } else if (e.getSource() == delete) {
                    //TODO
                    System.out.println("delete");
                } else if (e.getSource() == logout) {
                    //TODO
                    System.out.println("logout");
                }
            } catch (Exception a) {
                JOptionPane.showMessageDialog(null, "An error has occurred! (SF)",
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
