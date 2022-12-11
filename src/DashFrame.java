import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Socket;
import java.util.*;

public class DashFrame extends JFrame implements Runnable {
    Socket socket;
    String username;
    String status;
    JFrame frame;
    JButton viewHighLow;
    JButton viewLowHigh;
    JButton exit;
    JButton mostCommonWord;
    JLabel dashboardPanel;
    boolean ascending;
    ArrayList<String> dashboard = new ArrayList<>();

    public DashFrame(Socket socket, String username, String status) {
        this.socket = socket;
        this.username = username;
        this.status = status;
    }

    public void run() {
        frame = new JFrame(username + "'s dashboard");
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        viewHighLow = new JButton("View High-Low");
        viewHighLow.addActionListener(actionListener);

        viewLowHigh = new JButton("View Low-High");
        viewLowHigh.addActionListener(actionListener);

        exit = new JButton("Exit");
        exit.addActionListener(actionListener);

        String output = "";

        try {
            boolean ascending = false;      // Accounts for sorting
            BufferedReader br = new BufferedReader(new FileReader(username + "messages.txt"));
            String line = "";

            while (line!= null) {       // This loop iterates through every store the user has
                int count = 0;
                int maxCount = 0;
                String word = null;
                ArrayList<String> messageSenders = new ArrayList<>();
                ArrayList<String> words = new ArrayList<>();
                HashMap<String, Integer> data = new HashMap<>();
                LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
                ArrayList<Integer> numMessages = new ArrayList<>();
                String message = "";

                while ((message = br.readLine()) != null) {     // This loop iterates through every message in the store
                    String[] splitLine = message.split(" ");
                    String[] splitMessage = message.split(":");

                    String customer = splitLine[0];
                    String customerMessage = splitMessage[3];

                    String[] messageWords = customerMessage.split(" ");
                    String[] modifiedWords = Arrays.copyOfRange(messageWords, 1, messageWords.length);
                    Collections.addAll(words, modifiedWords);

                    if (messageSenders.contains(customer)) {        // This tallies # of messages each customer has sent
                        int value = data.get(customer);
                        data.replace(customer, ++value);
                    } else {
                        data.put(customer, 1);
                        messageSenders.add(customer);
                    }
                }

                for (Map.Entry<String, Integer> entry : data.entrySet()) {      // Adds customer message values to ArrayList & sorts them
                    numMessages.add(entry.getValue());
                }
                Collections.sort(numMessages);

                for (int num : numMessages) {       // Puts the data into a linked hashmap
                    for (Map.Entry<String, Integer> entry : data.entrySet()) {
                        if (entry.getValue().equals(num)) {
                            sortedMap.put(entry.getKey(), num);
                        }
                    }
                }

                for (int i = 0; i < words.size(); i++) {        // Iterates through messages to find most common message
                    count = 1;
                    for (int j = i+1; j < words.size(); j++) {
                        if (words.get(i).equals(words.get(j))) {
                            count++;
                        }
                    }

                    if (count > maxCount) {
                        maxCount = count;
                        word = words.get(i);
                    }
                }

                dashboard.add(username + ": " + sortedMap + ", Most common word: " + word);
                line = br.readLine();
            }

            for (int i = 0; i < dashboard.size(); i++) {
                String dashboardLine = dashboard.get(i).toString();

                output += dashboardLine + "\n";
            }
            ascending = true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Can't find your dashboard :(", "Error", JOptionPane.ERROR_MESSAGE);
        }

        JLabel dashboardPanel = new JLabel("              " + output);
        frame.add(dashboardPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.add(exit);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == viewHighLow) {

                if (!ascending) {
                    JOptionPane.showMessageDialog(null, "It's already sorted this way!",
                            "Already Sorted", JOptionPane.ERROR_MESSAGE);
                }
            }

            if (e.getSource() == viewLowHigh) {

                if (ascending) {
                    JOptionPane.showMessageDialog(null, "It's already sorted this way!",
                            "Already Sorted", JOptionPane.ERROR_MESSAGE);
                } else {
                    frame.getContentPane().removeAll();

                    frame.add(dashboardPanel, BorderLayout.CENTER);

                    JPanel bottom = new JPanel();
                    bottom.add(viewHighLow);
                    bottom.add(viewLowHigh);
                    bottom.add(exit);
                    frame.add(bottom, BorderLayout.SOUTH);
                }
            }

            if (e.getSource() == exit) {
                if (status.equals("seller")) {
                    frame.dispose();
                    SwingUtilities.invokeLater(new SellerFrame(socket, username));
                } else if (status.equals("customer")) {
                    frame.dispose();
                    SwingUtilities.invokeLater(new CustomerFrame(socket, username));
                }
            }
        }
    };
}
