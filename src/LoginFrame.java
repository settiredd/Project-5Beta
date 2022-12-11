import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * EZ Messenger -- LoginFrame
 *
 * Frame where users can choose to login or be redirected to create an account.
 *
 * @author Shreeya Ettireddy, Ben Sitzman, Caden Edam, lab sec L29
 *
 * @version 12/11/22
 *
 */
public class LoginFrame extends JFrame implements Runnable {
    String username;

    JFrame loginFrame;
    JTextField usernameTextField;
    JPasswordField passwordTextField;
    JButton loginButton;

    Socket socket;

    public LoginFrame(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        String[] loginOptions = new String[]{"Login", "Create account", "Quit"};
        String loginSelection = selectOption("What would you like to do?", loginOptions);

        if (loginSelection != null) {
            switch (loginSelection) {
                case "Login" -> {
                    loginFrame = new JFrame("Login");
                    loginFrame.setSize(275, 200);
                    loginFrame.setLocationRelativeTo(null);
                    loginFrame.setLayout(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();

                    usernameTextField = new JTextField(15);
                    passwordTextField = new JPasswordField(15);
                    JLabel usernameLabel = new JLabel("Username:");
                    JLabel passwordLabel = new JLabel("Password:");

                    loginButton = new JButton("Login");
                    loginButton.addActionListener(actionListener);

                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.gridwidth = 1;
                    gbc.gridheight = 1;
                    loginFrame.add(usernameLabel, gbc);

                    gbc.gridx = 1;
                    gbc.gridwidth = 5;
                    gbc.weightx = 1;
                    loginFrame.add(usernameTextField, gbc);

                    gbc.gridy = 1;
                    gbc.gridx = 0;
                    gbc.gridwidth = 1;
                    loginFrame.add(passwordLabel, gbc);

                    gbc.gridx = 1;
                    gbc.gridwidth = 5;
                    loginFrame.add(passwordTextField, gbc);

                    gbc.gridx = 2;
                    gbc.gridy = 6;
                    loginFrame.add(loginButton, gbc);

                    loginFrame.setVisible(true);
                    loginFrame.setResizable(false);
                    loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
                case "Create account" -> {
                    SwingUtilities.invokeLater(new CreateAccountFrame(socket));
                }
                case "Quit" -> {
                    JOptionPane.showMessageDialog(null, "See you next time!", "Farewell",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == loginButton) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());

                    String serverResponse;

                    String usernameEntered = usernameTextField.getText();
                    String passwordEntered = passwordTextField.getText();

                    if (usernameEntered.isEmpty() || passwordEntered.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please fill in the empty " +
                                "field(s)!","Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        writer.write("LOGIN");
                        writer.println();
                        writer.flush();

                        writer.write(usernameEntered);
                        writer.println();
                        writer.flush();

                        writer.write(passwordEntered);
                        writer.println();
                        writer.flush();

                        serverResponse = reader.readLine();
                        switch (serverResponse) {
                            case "LOGIN SUCCESSFUL" -> {
                                loginFrame.dispose();

                                username = usernameEntered;

                                String status = reader.readLine();
                                if (status.equals("customer")) {
                                    SwingUtilities.invokeLater(new CustomerFrame(socket, username));
                                } else if (status.equals("seller")) {
                                    SwingUtilities.invokeLater(new SellerFrame(socket, username));
                                }
                            }
                            case "LOGIN UNSUCCESSFUL" -> {
                                JOptionPane.showMessageDialog(null, "The username or " +
                                        "password you entered is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                                usernameTextField.setText("");
                                passwordTextField.setText("");
                            }
                            case "FILE ERROR" -> {
                                JOptionPane.showMessageDialog(null, "There was an error " +
                                        "reading a file!", "Error", JOptionPane.ERROR_MESSAGE);
                                usernameTextField.setText("");
                                passwordTextField.setText("");
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