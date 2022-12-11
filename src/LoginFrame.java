import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * EZ Messenger
 *
 * Frame where users can choose to login or be redirected to create an account
 *
 * @author Shreeya Ettireddy
 *
 * @version 12/11/22
 *
 */

public class LoginFrame extends JFrame implements Runnable {
    Socket socket;
    String username;
    String password;
    JButton loginButton;
    JTextField usernameText;
    JPasswordField passwordText;
    JFrame frame;

    public LoginFrame(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        String[] loginOptions = new String[]{"Login", "Create account", "Quit"};
        String loginSelection = selectOption("What would you like to do?", loginOptions);
        if (loginSelection == null || loginSelection.equals("Quit")) {
            JOptionPane.showMessageDialog(null, "See you next time!", "Farewell",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (loginSelection.equals("Create account")) {
            SwingUtilities.invokeLater(new CreateAccountFrame(socket));
        } else if (loginSelection.equals("Login")) {
            frame = new JFrame("Login");
            frame.setSize(275, 200);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            usernameText = new JTextField(15);
            passwordText = new JPasswordField(15);
            JLabel user = new JLabel("Username:");
            JLabel pass = new JLabel("Password:");

            loginButton = new JButton("Login");
            loginButton.addActionListener(actionListener);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            frame.add(user, gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 5;
            gbc.weightx = 1;
            frame.add(usernameText, gbc);

            gbc.gridy = 1;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            frame.add(pass, gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 5;
            frame.add(passwordText, gbc);

            gbc.gridx = 2;
            gbc.gridy = 6;
            frame.add(loginButton, gbc);

            frame.setVisible(true);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == loginButton) {
                try {
                    PrintWriter pw = new PrintWriter(socket.getOutputStream());
                    BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String checkUsername = usernameText.getText();
                    String checkPassword = passwordText.getText();

                    if (checkUsername.isEmpty() || checkPassword.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please fill in the empty field!",
                                "Login Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        username = checkUsername;
                        password = checkPassword;

                        pw.write("Login");          //Writes Login command
                        pw.println();
                        pw.flush();

                        pw.write(username);             //writes over the username
                        pw.println();
                        pw.flush();

                        pw.write(password);             //writes over the password
                        pw.println();
                        pw.flush();

                        String checkLogin = bfr.readLine();
                        if (checkLogin.equals("Yes")) {     //login success
                            String status = bfr.readLine();

                            if (status.equals("seller")) {      //opens seller specific frame
                                SwingUtilities.invokeLater(new SellerFrame(socket, username));
                            } else if (status.equals("customer")) {     //opens customer specific frame
                                SwingUtilities.invokeLater(new CustomerFrame(socket, username));
                            } else {
                                JOptionPane.showMessageDialog(null, "Status could not be " +
                                        "found (LF 107)", "Unknown Status", JOptionPane.ERROR_MESSAGE);
                            }

                            frame.dispose();

                        } else if (checkLogin.equals("No")) {   //login failure
                            JOptionPane.showMessageDialog(null, "Please check username and " +
                                            "password", "Unable to login",
                                    JOptionPane.ERROR_MESSAGE);
                            usernameText.setText("");
                            passwordText.setText("");
                        }
                    }
                } catch (Exception a) {
                    //handles any error we may have missed
                    JOptionPane.showMessageDialog(null, "An issue has occurred (LF 116)",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };

    public static String selectOption(String prompt, String[] options) {
        String selection;
        try {
            selection = (String) JOptionPane.showInputDialog(null, prompt,
                    "Welcome to EZ Messenger!",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        } catch (NullPointerException e) {
            return null;
        }
        return selection;
    }
}
