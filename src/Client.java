import javax.swing.*;
import java.io.*;
import java.net.*;

public class Client {
    Socket socket;
    public Client (Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        Socket socket;
        try {
            socket = new Socket("localhost", 4646);
            SwingUtilities.invokeLater(new LoginFrame(socket));

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Connection to the server could not be " +
                            "established!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }
}