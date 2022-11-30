import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CustomerFrame extends JFrame implements Runnable {
    Socket socket;
    String username;

    public CustomerFrame(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }


    //"1. Message a store\n2. Block a seller\n3. View your conversations\n" +
     //       "4. Edit account\n5. Delete account\n6. Logout" + DashBoard
    @Override
    public void run() {
        try {
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String[] loginOptions = new String[]{"Message Store", "Block Seller",
                    "View Conversations", "View Dashboard", "Edit Account", "Delete Account", "Logout"};

            String userSelection = selectOption("What would you like to do?", loginOptions,
                    "Hello " + username);

            if (userSelection == null) {
                return;
            }

            switch (userSelection) {
                case "Message Store" -> {
                    //TODO
                }
                case "Block Seller" -> {
                    //TODO
                }
                case "View Conversations" -> {
                    //TODO
                }
                case "View Dashboard" -> {
                    //TODO
                }
                case "Edit Account" -> {
                    //TODO
                }
                case "Delete Account" -> {
                    //TODO
                }
                case "Logout" -> {
                    //TODO
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
