import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.time.LocalDateTime;

public class Server implements Runnable {
    private static final File usersFile = new File("users.txt");
    private static final File storesFile = new File("stores.txt");
    private static final File invisibleListFile = new File("invisibleList.txt");
    private static final File blockListFile = new File("blockList.txt");
    private static final File conversationLogFile = new File("conversationLog.txt");

    public static String username;
    public static String password;
    public static String email;
    public static String status;
    Object o = new Object();

    Socket socket;

    public Server(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4646);

        while (true) {
            Socket socket = serverSocket.accept();
            Server newServer = new Server(socket);

            new Thread(newServer).start();
        }
    }

    synchronized boolean userPresent(String user, String pass) {
        boolean userExists = false;
        boolean correctPassword = false;

        try {
            ArrayList<String> fileContents;
            if (usersFile.exists()) {
                if (user != null) {
                    fileContents = readFile(usersFile);
                    if (fileContents != null) {
                        if (fileContents.size() > 0) {
                            for (String line : fileContents) {
                                String[] splitLine = line.split(";");
                                if (splitLine[1].equals(user)) {
                                    userExists = true;

                                    username = user;
                                    status = splitLine[0];
                                    password = splitLine[2];
                                    email = splitLine[3];

                                    if (password.equals(pass)) {
                                        correctPassword = true;
                                    }
                                    break;
                                }
                            }
                            if (!userExists) {
                                /*JOptionPane.showMessageDialog(null, "That user does not exist!",
                                        "Error", JOptionPane.ERROR_MESSAGE); */
                                return false;
                            }
                            if (userExists && !correctPassword) {
                                /*JOptionPane.showMessageDialog(null,
                                        "Incorrect Password!",
                                        "Error", JOptionPane.ERROR_MESSAGE); */
                                return false;
                            }
                        } else {
                            /*JOptionPane.showMessageDialog(null, "No users exist yet!",
                                    "Error", JOptionPane.ERROR_MESSAGE); */

                            return false;
                        }
                    }
                }
            } else {
                /*JOptionPane.showMessageDialog(null, "No users exist yet!", "Error",
                        JOptionPane.ERROR_MESSAGE); */
                return false;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An issue occurred (85)", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return userExists && correctPassword; //returns true if user exists and correct password is used
    }

    synchronized String checkCreation(String stat, String user, String pass, String ema) {
        boolean userUsed = false;
        boolean emaUsed = false;
        String result = "";
        try {
            ArrayList<String> fileContents;
            if (usersFile.exists()) {
                fileContents = readFile(usersFile);
                if (fileContents != null) {
                    if (fileContents.size() > 0) {
                        for (String line : fileContents) {
                            String[] splitLine = line.split(";");
                            if (splitLine[1].equals(user)) {
                                userUsed = true;
                                break;
                            } else if (splitLine[3].equals(ema)) {
                                emaUsed = true;
                                break;
                            }
                        }
                    } else {
                        userUsed = false;
                        emaUsed = false;
                    }
                } else {
                    userUsed = false;
                    emaUsed = false;
                }

                if (!userUsed && !emaUsed) {
                    result = "Yes";

                    BufferedWriter userWriter = new BufferedWriter(new FileWriter(usersFile, true));
                    userWriter.write(stat + ";" + user + ";" + pass + ";" + ema);
                    userWriter.write("\n");
                    userWriter.close();
                } else {
                    if (userUsed) {
                        result = "No-Username";
                    } else if (emaUsed) {
                        result = "No-Email";
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An issue occurred (S 101)", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (result.isEmpty()) {
            result = "No";
        }
        return result;
    }

    synchronized String userExists(String username) {
        ArrayList<String> fileContents = new ArrayList<>();
        if (!usersFile.exists()) {
            return null;
        } else {

        }
        return null;
    }

    public void run() {

        try {
            if (!usersFile.exists()) {
                usersFile.createNewFile();      //creates users file if it doesn't already exist
            }
            if (!storesFile.exists()) {         //creates stores file if it doesn't already exist
                storesFile.createNewFile();
            }
            if (!blockListFile.exists()) {      //creates blocked file if it doesn't already exist
                blockListFile.createNewFile();
            }
            if (!invisibleListFile.exists()) {      //creates invisibility file if it doesn't already exist
                invisibleListFile.createNewFile();
            }


            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                String cmd = bfr.readLine();
                if (cmd == null) {
                    pw.close();
                    bfr.close();
                    socket.close();

                    return;
                }

                switch (cmd) {
                    case "Login" -> {
                        String checkUsername = bfr.readLine();
                        String checkPassword = bfr.readLine();

                        //username = checkUsername;

                        if (userPresent(checkUsername, checkPassword)) {
                            pw.write("Yes");      //writes yes to LoginFrame if user exists and correct password
                            pw.println();
                            pw.flush();

                            pw.write(status);        //writes user status over to LoginFrame
                            pw.println();
                            pw.flush();
                        } else {
                            pw.write("No");
                            pw.println();
                            pw.flush();
                        }
                    }
                    case "Create Account" -> {
                        /** status, username, password, email **/
                        String stat = bfr.readLine();
                        String user = bfr.readLine();
                        String pass = bfr.readLine();
                        String ema = bfr.readLine();

                        pw.write(checkCreation(stat, user, pass, ema));
                        pw.println();
                        pw.flush();

                        //username = user;
                    }
                    case "MessageOptions" -> {
                        String user = bfr.readLine();
                        String stat = bfr.readLine();

                        if (stat.equals("seller")) {
                            ArrayList<String> fileContents;
                            ArrayList<String> sendUsers = new ArrayList<>();

                            if (usersFile.exists()) {
                                synchronized (o) {                      //so file reading is concurrent
                                    fileContents = readFile(usersFile);
                                }
                                if (fileContents != null) {
                                    if (fileContents.size() > 0) {
                                        for (String line : fileContents) {
                                            String[] splitLine = line.split(";");

                                            if (stat.equals("seller")) {
                                                if (splitLine[0].equals("customer")) {
                                                    String userToCheck = splitLine[1];

                                                    if (!isInvisible(user, userToCheck)) {
                                                        sendUsers.add(userToCheck);
                                                    }
                                                }
                                            }
                                        }

                                        if (sendUsers.size() == 0) {
                                            pw.write("None");
                                            pw.println();
                                            pw.flush();
                                        } else {
                                            pw.write("Yes");        //lets client know users exist
                                            pw.println();
                                            pw.flush();

                                            for (int i = 0; i < sendUsers.size(); i++) {
                                                pw.write(sendUsers.get(i));
                                                pw.println();
                                                pw.flush();
                                            }

                                            pw.write("End");
                                            pw.println();
                                            pw.flush();
                                        }
                                    } else {
                                        pw.write("None");
                                        pw.println();
                                        pw.flush();
                                    }
                                } else {
                                    pw.write("None");
                                    pw.println();
                                    pw.flush();
                                }
                            }
                        } else if (stat.equals("customer")) {

                        }
                    }
                    case "Search" -> {
                        String user = bfr.readLine();
                        String search = bfr.readLine();
                        String stat = bfr.readLine();

                        if (stat.equals("seller")) {

                            if (isInvisible(user, search)) {
                                pw.write("No");             //cannot message users who are invisible
                                pw.println();
                                pw.flush();
                            } else if (isBlocked(user, search)) {
                                pw.write("blocked");            //writes to client that user is blocked
                                pw.println();
                                pw.flush();
                            } else {
                                ArrayList<String> fileContents;
                                boolean canMessage = false;
                                if (usersFile.exists()) {
                                    synchronized (o) {                      //so file reading is concurrent
                                        fileContents = readFile(usersFile);
                                    }
                                    if (fileContents != null) {
                                        if (fileContents.size() > 0) {
                                            for (String line : fileContents) {
                                                String[] splitLine = line.split(";");

                                                if (splitLine[1].equals(search)) {
                                                    if (splitLine[0].equals("customer")) {
                                                        pw.write("Yes");      //checks if searched is a customer
                                                        pw.println();
                                                        pw.flush();

                                                        canMessage = true;
                                                    }
                                                }
                                            }

                                            if (!canMessage) {
                                                pw.write("No");      //user doesn't exist or is a seller
                                                pw.println();
                                                pw.flush();
                                            }
                                        }
                                    }
                                }
                            }

                        } else if (stat.equals("customer")) {

                        }

                    }
                    case "Create Store" -> {
                        String user = bfr.readLine();
                        String storeName = bfr.readLine();
                        ArrayList<String> fileContents;
                        boolean canCreate = true;

                        synchronized (o) {
                            fileContents = readFile(storesFile);        //concurrently reads file
                        }

                        if (fileContents != null) {             //does nothing is file contents are null
                            if (fileContents.size() > 0) {
                                for (String line : fileContents) {
                                    String[] splitLine = line.split(";");

                                    if (splitLine[1].equals(storeName)) {
                                        canCreate = false;          //cannot create if store name is being used
                                    }
                                }
                            }
                        }

                        if (canCreate) {
                            pw.write("Yes");
                            pw.println();
                            pw.flush();

                            writeStore(user, storeName);
                        } else {
                            pw.write("No");
                            pw.println();
                            pw.flush();
                        }
                    } case "Edit" -> {
                        String user = bfr.readLine();
                        String itemToEdit = bfr.readLine();

                        String[] infoSplit = getUserInfo(user).split(";");

                        if (itemToEdit.equals("Username")) {
                            String userUsername = infoSplit[1];         //gets user's current username

                        } else if (itemToEdit.equals("Password")) {
                            String userPassword = infoSplit[2];         //gets user's current password

                        } else if (itemToEdit.equals("Email")) {
                            String userEmail = infoSplit[3];            //gets user's current email

                        }

                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "A problem has occurred (S 143)", "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            e.printStackTrace();
        }

    }

    synchronized String getUserInfo(String user) {
        String userInfo = "";
        try {
            if (usersFile.exists()) {
                BufferedReader bfr = new BufferedReader(new FileReader(usersFile));
                String line;

                while ((line = bfr.readLine()) != null) {
                    String[] splitLine = line.split(";");

                    if (splitLine[1].equals(user)) {
                        userInfo = line;
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An issue occurred in getting user",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return userInfo;
    }

    synchronized void writeStore(String user, String storeName) {
        try {
            if (storesFile.exists()) {
                BufferedWriter bfr = new BufferedWriter(new FileWriter(storesFile, true));
                bfr.write(username + ";" + storeName + "\n");
                bfr.close();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An issue occurred in writing store",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static ArrayList<String> readFile(File file) {
        ArrayList<String> fileContents = new ArrayList<>();
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bfr.readLine()) != null) {
                fileContents.add(line);
            }
            bfr.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "File could not be read!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return fileContents;
    }

    public static boolean isInvisible(String user, String userToCheck) {
        if (invisibleListFile.exists()) {
            ArrayList<String> fileContents = readFile(invisibleListFile);
            if (fileContents != null) {
                for (String line : fileContents) {
                    if (line.equals(userToCheck + ";invisible to;" + user)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isBlocked(String user, String userToCheck) {
        if (blockListFile.exists()) {
            ArrayList<String> fileContents = readFile(blockListFile);
            if (fileContents != null) {
                for (String line : fileContents) {
                    if (line.equals(userToCheck + ";blocked;" + user)) {
                        return true;
                    } else if (line.equals(user + ";blocked;" + userToCheck)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}