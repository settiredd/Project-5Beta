import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;

public class CreateAccountFrame extends JFrame implements Runnable {
    Socket socket;
    String username;
    String password;
    String status;
    String email;
    JTextField userText;
    JPasswordField passText;
    JTextField emailText;
    JRadioButton seller;
    JRadioButton customer;
    JButton create;
    JFrame frame;

    public CreateAccountFrame(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        frame = new JFrame("Create Account");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel statusLabel = new JLabel("What is your status?");
        seller = new JRadioButton("Seller");
        customer = new JRadioButton("Customer");

        ButtonGroup group = new ButtonGroup();
        group.add(seller);
        group.add(customer);

        JLabel userLabel = new JLabel("Username:");
        userText = new JTextField(10);

        JLabel passLabel = new JLabel("Password:");
        passText = new JPasswordField(10);

        JLabel emailLabel = new JLabel("Email:");
        emailText = new JTextField(15);

        create = new JButton("Create Account");
        create.addActionListener(actionListener);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        frame.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        frame.add(customer, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        frame.add(seller, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        frame.add(userText, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        frame.add(passText, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        frame.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        frame.add(emailText, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        frame.add(create, gbc);

        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == create) {
                try {
                    PrintWriter pw = new PrintWriter(socket.getOutputStream());
                    BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String checkUsername = userText.getText();
                    String checkPassword = passText.getText();
                    String checkEmail = emailText.getText();

                    if (checkUsername.isEmpty() || checkPassword.isEmpty() || checkEmail.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please fill in the empty field!",
                                "Login Error", JOptionPane.ERROR_MESSAGE);
                    } else if (!seller.isSelected() && !customer.isSelected()) {       //makes sure user selects status
                        JOptionPane.showMessageDialog(null, "Select your status!",
                                "Login Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        boolean goodUsername;
                        boolean goodPassword;
                        boolean goodEmail;

                        if (seller.isSelected()) {          //assigns status
                            status = "seller";
                        } else if (customer.isSelected()) {
                            status = "customer";
                        }

                        /** check username before sending to server **/
                        if (checkUsername.length() >= 8 && !checkUsername.contains(" ")
                                && !checkUsername.contains(";"))  {
                            goodUsername = true;
                            username = checkUsername;
                        } else {
                            JOptionPane.showMessageDialog(null, "Create a valid username " +
                                            "(at least 8 characters, no semicolons or spaces).",
                                    "Invalid username", JOptionPane.ERROR_MESSAGE);
                            userText.setText("");
                            goodUsername = false;
                        }

                        /** check password before sending to server **/
                        if (checkPassword.length() >= 8 && !checkPassword.contains(" ")
                                && !checkPassword.contains(";"))  {
                            goodPassword = true;
                            password = checkPassword;
                        } else {
                            JOptionPane.showMessageDialog(null, "Create a valid password " +
                                            "(at least 8 characters, no semicolons or spaces).",
                                    "Invalid password", JOptionPane.ERROR_MESSAGE);
                            passText.setText("");
                            goodPassword = false;
                        }

                        /** check email before sending to server **/
                        String emailRegex = "^[a-zA-Z0-9_+&*-]+" +
                                "(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                                "(?:[a-zA-Z0-9-]+\\.)+" +
                                "[a-zA-Z]{2,7}$";
                        Pattern checkValidity = Pattern.compile(emailRegex);
                        if (checkValidity.matcher(checkEmail).matches()) {
                            goodEmail = true;
                            email = checkEmail;
                        } else {
                            JOptionPane.showMessageDialog(null, "Enter a valid email",
                                    "Invalid email", JOptionPane.ERROR_MESSAGE);
                            emailText.setText("");
                            goodEmail = false;
                        }

                        if (goodUsername && goodPassword && goodEmail) {
                            pw.write("Create Account");         //writes command to server
                            pw.println();
                            pw.flush();

                            pw.write(status);         //writes status to server
                            pw.println();
                            pw.flush();

                            pw.write(username);         //writes username to server
                            pw.println();
                            pw.flush();

                            pw.write(password);         //writes password to server
                            pw.println();
                            pw.flush();

                            pw.write(email);         //writes email to server
                            pw.println();
                            pw.flush();

                            String result = bfr.readLine();

                            if (result.equals("Yes")) {
                                JOptionPane.showMessageDialog(null, "Account Created",
                                        "Success!", JOptionPane.PLAIN_MESSAGE);
                                //pw.close();
                                //bfr.close();
                                //socket.close();
                                frame.dispose();

                                if (status.equals("seller")) {
                                    SwingUtilities.invokeLater(new SellerFrame(socket, username));
                                } else if (status.equals("customer")) {
                                    SwingUtilities.invokeLater(new CustomerFrame(socket, username));
                                }
                            } else if (result.equals("No-Username")) {
                                JOptionPane.showMessageDialog(null, "This username is being" +
                                        " used!", "Creation Error", JOptionPane.ERROR_MESSAGE);
                                userText.setText("");
                            } else if (result.equals("No-Email")) {
                                JOptionPane.showMessageDialog(null, "This email is being" +
                                        " used!", "Creation Error", JOptionPane.ERROR_MESSAGE);
                                emailText.setText("");
                            } else {
                                JOptionPane.showMessageDialog(null, "There was an error" +
                                        " creating this account!", "Creation Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } catch (Exception a) {
                    //handles any error we may have missed
                    JOptionPane.showMessageDialog(null, "An issue has occurred (CAF 124)",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };
}
