import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;

/**
 * EZ Messenger -- CreateAccountFrame
 *
 * Creates a frame users to create an account.
 *
 * @author Shreeya Ettireddy, Ben Sitzman, Caden Edam, lab sec L29
 *
 * @version 12/11/22
 *
 */
public class CreateAccountFrame extends JFrame implements Runnable {
    String status;
    String username;
    String password;
    String email;

    JFrame createAccountFrame;
    JTextField usernameTextField;
    JPasswordField passwordTextField;
    JTextField emailTextField;
    JRadioButton sellerButton;
    JRadioButton customerButton;
    JButton createAccountButton;

    Socket socket;

    public CreateAccountFrame(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        createAccountFrame = new JFrame("Create Account");
        createAccountFrame.setSize(400, 300);
        createAccountFrame.setLocationRelativeTo(null);
        createAccountFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel statusLabel = new JLabel("What is your status?");
        customerButton = new JRadioButton("Customer");
        sellerButton = new JRadioButton("Seller");

        ButtonGroup group = new ButtonGroup();
        group.add(customerButton);
        group.add(sellerButton);

        JLabel usernameLabel = new JLabel("Username:");
        usernameTextField = new JTextField(10);

        JLabel passwordLabel = new JLabel("Password:");
        passwordTextField = new JPasswordField(10);

        JLabel emailLabel = new JLabel("Email:");
        emailTextField = new JTextField(15);

        createAccountButton = new JButton("Create account");
        createAccountButton.addActionListener(actionListener);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        createAccountFrame.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        createAccountFrame.add(customerButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        createAccountFrame.add(sellerButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        createAccountFrame.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        createAccountFrame.add(usernameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        createAccountFrame.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        createAccountFrame.add(passwordTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        createAccountFrame.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        createAccountFrame.add(emailTextField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        createAccountFrame.add(createAccountButton, gbc);

        createAccountFrame.setVisible(true);
        createAccountFrame.setResizable(false);
        createAccountFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == createAccountButton) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());

                    String serverResponse;

                    String usernameEntered = usernameTextField.getText();
                    String passwordEntered = passwordTextField.getText();
                    String emailEntered = emailTextField.getText();

                    if (usernameEntered.isEmpty() || passwordEntered.isEmpty() || emailEntered.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please fill in the empty " +
                                "field(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else if (!sellerButton.isSelected() && !customerButton.isSelected()) {
                        JOptionPane.showMessageDialog(null, "Please select a status!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (sellerButton.isSelected()) {
                            status = "seller";
                        } else if (customerButton.isSelected()) {
                            status = "customer";
                        }

                        boolean validUsername = false;

                        if (usernameEntered.length() >= 8 && !usernameEntered.contains(" ") &&
                                !usernameEntered.contains(";") && !usernameEntered.contains("."))  {
                            validUsername = true;
                            username = usernameEntered;
                        } else {
                            JOptionPane.showMessageDialog(null, "Please enter a valid " +
                                            "username (at least 8 characters, no semicolons, periods, or spaces)!",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            usernameTextField.setText("");
                        }

                        boolean validPassword = false;

                        if (passwordEntered.length() >= 8 && !passwordEntered.contains(" ")
                                && !passwordEntered.contains(";") && !passwordEntered.contains("."))  {
                            validPassword = true;
                            password = passwordEntered;
                        } else {
                            JOptionPane.showMessageDialog(null, "Please enter a valid " +
                                            "password (at least 8 characters, no semicolons, periods, or spaces)!",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            passwordTextField.setText("");
                        }

                        String emailRegex = "^[a-zA-Z0-9_+&*-]+" +
                                "(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                                "(?:[a-zA-Z0-9-]+\\.)+" +
                                "[a-zA-Z]{2,7}$";
                        Pattern checkValidity = Pattern.compile(emailRegex);

                        boolean validEmail = false;

                        if (checkValidity.matcher(emailEntered).matches()) {
                            validEmail = true;
                            email = emailEntered;
                        } else {
                            JOptionPane.showMessageDialog(null, "Please enter a valid email!",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            emailTextField.setText("");
                        }

                        if (validUsername && validPassword && validEmail) {
                            writer.write("CREATE ACCOUNT");
                            writer.println();
                            writer.flush();

                            writer.write(status);
                            writer.println();
                            writer.flush();

                            writer.write(username);
                            writer.println();
                            writer.flush();

                            writer.write(password);
                            writer.println();
                            writer.flush();

                            writer.write(email);
                            writer.println();
                            writer.flush();

                            serverResponse = reader.readLine();
                            switch (serverResponse) {
                                case "ACCOUNT CREATION SUCCESSFUL" -> {
                                    JOptionPane.showMessageDialog(null, "Account created " +
                                            "successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                                    createAccountFrame.dispose();

                                    if (status.equals("customer")) {
                                        SwingUtilities.invokeLater(new CustomerFrame(socket, username));
                                    } else if (status.equals("seller")) {
                                        SwingUtilities.invokeLater(new SellerFrame(socket, username));
                                    }
                                }
                                case "USERNAME TAKEN" -> {
                                    JOptionPane.showMessageDialog(null, "The username you " +
                                            "entered is taken!", "Error", JOptionPane.ERROR_MESSAGE);
                                    usernameTextField.setText("");
                                }
                                case "EMAIL TAKEN" -> {
                                    JOptionPane.showMessageDialog(null, "The email you " +
                                            "entered is already being used!", "Error", JOptionPane.ERROR_MESSAGE);
                                    emailTextField.setText("");
                                }
                                case "BOTH TAKEN" -> {
                                    JOptionPane.showMessageDialog(null, "The username and " +
                                                    "email you entered are both taken!", "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                    usernameTextField.setText("");
                                    emailTextField.setText("");
                                }
                                case "FILE ERROR" -> {
                                    JOptionPane.showMessageDialog(null, "There was an error " +
                                            "reading a file!", "Error", JOptionPane.ERROR_MESSAGE);
                                    usernameTextField.setText("");
                                    emailTextField.setText("");
                                }
                            }
                        }
                    }
                } catch (IOException ioException) {
                    JOptionPane.showMessageDialog(null, "Connection lost!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };
}