import javax.swing.*;
import java.io.*;
import java.net.*;

public class Client {
    Socket socket;
    public Client (Socket socket) {
        this.socket = socket;
    }
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

    public static void main(String[] args) {
        Socket socket;
        try {
            socket = new Socket("localhost", 4242);

            String[] loginOptions = new String[]{"Login", "Create account", "Quit"};
            String loginSelection = selectOption("What would you like to do?", loginOptions);
            if (loginSelection == null || loginSelection.equals("Quit")) {
                JOptionPane.showMessageDialog(null, "See you next time!", "Farewell",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (loginSelection.equals("Login")) {
                    SwingUtilities.invokeLater(new LoginFrame(socket));
                } else if (loginSelection.equals("Create account")) {
                    SwingUtilities.invokeLater(new CreateAccountFrame(socket));
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Connection to the server could not be " +
                            "established!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
}