import javax.swing.*;
import java.io.*;
import java.net.*;

/**
 * EZ Messenger -- Client
 *
 * The Client class for the program, frame execution begins here.
 *
 * @author Shreeya Ettireddy, Ben Sitzman, Caden Edam, lab sec L29
 *
 * @version 12/11/22
 *
 */
public class Client {
    Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 3434);
            SwingUtilities.invokeLater(new LoginFrame(socket));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Connection to the server could not be " +
                    "established!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}