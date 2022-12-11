import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * EZ Messenger -- Server
 *
 * Server for the program.
 *
 * @author Shreeya Ettireddy, Ben Sitzman, Caden Edam, lab sec L29
 *
 * @version 12/11/22
 *
 */
public class Server implements Runnable {
    private static final File usersFile = new File("users.txt");
    private static final File storesFile = new File("stores.txt");
    private static final File invisibleListFile = new File("invisibleList.txt");
    private static final File blockListFile = new File("blockList.txt");
    private static final File conversationLogFile = new File("conversationLog.txt");

    Socket socket;

    public Server(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4343);

        while (true) {
            Socket socket = serverSocket.accept();
            Server newServer = new Server(socket);

            new Thread(newServer).start();
        }
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            while (true) {
                if (!usersFile.exists()) {
                    usersFile.createNewFile();
                }
                if (!storesFile.exists()) {
                    storesFile.createNewFile();
                }
                if (!invisibleListFile.exists()) {
                    invisibleListFile.createNewFile();
                }
                if (!blockListFile.exists()) {
                    blockListFile.createNewFile();
                }
                if (!conversationLogFile.exists()) {
                    conversationLogFile.createNewFile();
                }

                String command = reader.readLine();

                if (command == null) {
                    reader.close();
                    writer.close();
                    socket.close();

                    return;
                }

                switch (command) {
                    case "LOGIN" -> {
                        String usernameEntered = reader.readLine();
                        String passwordEntered = reader.readLine();

                        String result = checkLogin(usernameEntered, passwordEntered);

                        writer.write(result);
                        writer.println();
                        writer.flush();

                        if (result.equals("LOGIN SUCCESSFUL")) {
                            String[] userInfoSplit = getUserInfo(usernameEntered).split(";");
                            String status = userInfoSplit[0];

                            writer.write(status);
                            writer.println();
                            writer.flush();
                        }
                    }
                    case "CREATE ACCOUNT" -> {
                        String statusSelected = reader.readLine();
                        String usernameEntered = reader.readLine();
                        String passwordEntered = reader.readLine();
                        String emailEntered = reader.readLine();

                        String result = createAccount(statusSelected, usernameEntered, passwordEntered, emailEntered);

                        writer.write(result);
                        writer.println();
                        writer.flush();
                    }
                    case "MESSAGE OPTIONS" -> {
                        String username = reader.readLine();

                        String[] userInfoSplit = getUserInfo(username).split(";");
                        String status = userInfoSplit[0];

                        if (status.equals("customer")) {
                            ArrayList<String> fileContents = readFile(storesFile);

                            if (fileContents != null) {
                                if (fileContents.size() > 0) {
                                    ArrayList<String> visibleStores = new ArrayList<>();

                                    for (String line : fileContents) {
                                        String[] splitLine = line.split(";owned by;");
                                        String storeName = splitLine[0];
                                        String seller = splitLine[1];

                                        if (isVisible(username, seller)) {
                                            visibleStores.add(storeName);
                                        }
                                    }

                                    if (visibleStores.size() > 0) {
                                        writer.write("STORES FOUND");
                                        writer.println();
                                        writer.flush();

                                        for (String store : visibleStores) {
                                            writer.write(store);
                                            writer.println();
                                            writer.flush();
                                        }

                                        writer.write("END;");
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        writer.write("NO STORES");
                                        writer.println();
                                        writer.flush();
                                    }
                                } else {
                                    writer.write("NO STORES");
                                    writer.println();
                                    writer.flush();
                                }
                            } else {
                                writer.write("FILE ERROR");
                                writer.println();
                                writer.flush();
                            }
                        } else if (status.equals("seller")) {
                            ArrayList<String> fileContents = readFile(usersFile);

                            if (fileContents != null) {
                                if (fileContents.size() > 0) {
                                    ArrayList<String> visibleCustomers = new ArrayList<>();

                                    for (String line : fileContents) {
                                        String[] splitLine = line.split(";");

                                        if (splitLine[0].equals("customer")) {
                                            String customer = splitLine[1];

                                            if (isVisible(username, customer)) {
                                                visibleCustomers.add(customer);
                                            }
                                        }
                                    }

                                    if (visibleCustomers.size() > 0) {
                                        writer.write("CUSTOMERS FOUND");
                                        writer.println();
                                        writer.flush();

                                        for (String customer : visibleCustomers) {
                                            writer.write(customer);
                                            writer.println();
                                            writer.flush();
                                        }

                                        writer.write("END;");
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        writer.write("NO CUSTOMERS");
                                        writer.println();
                                        writer.flush();
                                    }
                                } else {
                                    writer.write("NO CUSTOMERS");
                                    writer.println();
                                    writer.flush();
                                }
                            } else {
                                writer.write("FILE ERROR");
                                writer.println();
                                writer.flush();
                            }
                        }
                    }
                    case "SEARCH" -> {
                        String username = reader.readLine();
                        String search = reader.readLine();

                        String[] userInfoSplit = getUserInfo(username).split(";");
                        String status = userInfoSplit[0];

                        boolean returnedResult = false;

                        if (status.equals("customer")) {
                            ArrayList<String> fileContents = readFile(storesFile);

                            if (fileContents != null) {
                                if (fileContents.size() > 0) {
                                    for (String line : fileContents) {
                                        String[] splitLine = line.split(";owned by;");
                                        String store = splitLine[0];

                                        if (store.equals(search)) {
                                            returnedResult = true;

                                            String seller = splitLine[1];
                                            String blockStatus = getBlockStatus(username, seller);

                                            if (blockStatus.equals("NOT BLOCKED")) {
                                                writer.write(seller);
                                                writer.println();
                                                writer.flush();
                                            } else {
                                                writer.write(blockStatus);
                                                writer.println();
                                                writer.flush();
                                            }

                                            createConversationFiles(username, seller);

                                            break;
                                        }
                                    }

                                    if (!returnedResult) {
                                        writer.write("STORE NOT FOUND");
                                        writer.println();
                                        writer.flush();
                                    }
                                } else {
                                    writer.write("STORE NOT FOUND");
                                    writer.println();
                                    writer.flush();
                                }
                            } else {
                                writer.write("FILE ERROR");
                                writer.println();
                                writer.flush();
                            }
                        } else if (status.equals("seller")) {
                            ArrayList<String> fileContents = readFile(usersFile);

                            if (fileContents != null) {
                                for (String line : fileContents) {
                                    String[] splitLine = line.split(";");

                                    if (splitLine[0].equals("customer")) {
                                        String customer = splitLine[1];

                                        if (customer.equals(search)) {
                                            returnedResult = true;

                                            String blockStatus = getBlockStatus(username, customer);

                                            if (blockStatus.equals("NOT BLOCKED")) {
                                                writer.write(customer);
                                                writer.println();
                                                writer.flush();
                                            } else {
                                                writer.write(blockStatus);
                                                writer.println();
                                                writer.flush();
                                            }

                                            createConversationFiles(username, customer);

                                            break;
                                        }
                                    }
                                }

                                if (!returnedResult) {
                                    writer.write("CUSTOMER NOT FOUND");
                                    writer.println();
                                    writer.flush();
                                }
                            } else {
                                writer.write("FILE ERROR");
                                writer.println();
                                writer.flush();
                            }
                        }
                    }
                    case "CREATE STORE" -> {
                        String username = reader.readLine();
                        String storeName = reader.readLine();

                        String result = createStore(username, storeName);

                        writer.write(result);
                        writer.println();
                        writer.flush();
                    }
                    case "EDIT ACCOUNT" -> {
                        String username = reader.readLine();
                        String infoToEdit = reader.readLine();
                        String newInfo = reader.readLine();

                        ArrayList<String> fileContents = readFile(usersFile);

                        if (fileContents != null) {
                            String userInfo = getUserInfo(username);
                            String[] userInfoSplit = userInfo.split(";");

                            switch (infoToEdit) {
                                case "USERNAME" -> {
                                    if (username.equals(newInfo)) {
                                        writer.write("NO CHANGE");
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        boolean newInfoTaken = false;

                                        for (String line : fileContents) {
                                            String[] splitLine = line.split(";");

                                            if (splitLine[1].equals(newInfo)) {
                                                newInfoTaken = true;
                                                break;
                                            }
                                        }

                                        if (!newInfoTaken) {
                                            for (String line : fileContents) {
                                                if (line.equals(userInfo)) {
                                                    String newLine = userInfoSplit[0] + ";" + newInfo + ";" +
                                                            userInfoSplit[2] + ";" + userInfoSplit[3];

                                                    fileContents.set(fileContents.indexOf(line), newLine);

                                                    rewriteFile(usersFile, fileContents);
                                                    break;
                                                }
                                            }

                                            boolean fileError = false;

                                            fileContents = readFile(invisibleListFile);

                                            if (fileContents != null) {
                                                if (fileContents.size() > 0) {
                                                    for (String line : fileContents) {
                                                        String[] splitLine = line.split(
                                                                ";invisible to;");

                                                        if (splitLine[0].equals(username)) {
                                                            String newLine = newInfo + ";invisible to;" + splitLine[1];

                                                            fileContents.set(fileContents.indexOf(line), newLine);
                                                        }
                                                        if (splitLine[1].equals(username)) {
                                                            String newLine = splitLine[0] + ";invisible " +
                                                                    "to;" + newInfo;

                                                            fileContents.set(fileContents.indexOf(line), newLine);
                                                        }
                                                    }

                                                    rewriteFile(invisibleListFile, fileContents);
                                                }
                                            } else {
                                                fileError = true;
                                            }

                                            fileContents = readFile(blockListFile);

                                            if (fileContents != null) {
                                                if (fileContents.size() > 0) {
                                                    for (String line : fileContents) {
                                                        String[] splitLine = line.split(";blocked;");

                                                        if (splitLine[0].equals(username)) {
                                                            String newLine = newInfo + ";blocked;" + splitLine[1];

                                                            fileContents.set(fileContents.indexOf(line), newLine);
                                                        }
                                                        if (splitLine[1].equals(username)) {
                                                            String newLine = splitLine[0] + ";blocked;" + newInfo;

                                                            fileContents.set(fileContents.indexOf(line), newLine);
                                                        }
                                                    }

                                                    rewriteFile(blockListFile, fileContents);
                                                }
                                            } else {
                                                fileError = true;
                                            }

                                            fileContents = readFile(conversationLogFile);

                                            if (fileContents != null) {
                                                if (fileContents.size() > 0) {
                                                    for (String line : fileContents) {
                                                        String[] splitLine = line.split(" & ");

                                                        if (splitLine[0].equals(username)) {
                                                            String newLine = newInfo + " & " + splitLine[1];

                                                            fileContents.set(fileContents.indexOf(line), newLine);

                                                            File fileToRename = new File(line + ".txt");
                                                            fileToRename.renameTo(new File(newLine + ".txt"));
                                                        }
                                                        if (splitLine[1].equals(username)) {
                                                            String newLine = splitLine[0] + " & " + newInfo;

                                                            fileContents.set(fileContents.indexOf(line), newLine);

                                                            File fileToRename = new File(line + ".txt");
                                                            fileToRename.renameTo(new File(newLine + ".txt"));
                                                        }
                                                    }

                                                    rewriteFile(conversationLogFile, fileContents);
                                                }
                                            } else {
                                                fileError = true;
                                            }

                                            String status = userInfoSplit[0];

                                            if (status.equals("seller")) {
                                                fileContents = readFile(storesFile);

                                                if (fileContents != null) {
                                                    if (fileContents.size() > 0) {
                                                        for (String line : fileContents) {
                                                            String[] splitLine = line.split(";owned by;");

                                                            if (splitLine[1].equals(username)) {
                                                                String newLine = splitLine[0] + ";owned by;" + newInfo;

                                                                fileContents.set(fileContents.indexOf(line), newLine);
                                                            }
                                                        }

                                                        rewriteFile(storesFile, fileContents);
                                                    }
                                                } else {
                                                    fileError = true;
                                                }
                                            }

                                            if (fileError) {
                                                writer.write("FILE ERROR");
                                                writer.println();
                                                writer.flush();
                                            } else {
                                                writer.write("ACCOUNT EDIT SUCCESSFUL");
                                                writer.println();
                                                writer.flush();
                                            }
                                        } else {
                                            writer.write("NEW INFO TAKEN");
                                            writer.println();
                                            writer.flush();
                                        }
                                    }
                                }
                                case "PASSWORD" -> {
                                    String password = userInfoSplit[2];

                                    if (password.equals(newInfo)) {
                                        writer.write("NO CHANGE");
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        for (String line : fileContents) {
                                            if (line.equals(userInfo)) {
                                                String newLine = userInfoSplit[0] + ";" + userInfoSplit[1] + ";" +
                                                        newInfo + ";" + userInfoSplit[3];

                                                fileContents.set(fileContents.indexOf(line), newLine);

                                                rewriteFile(usersFile, fileContents);
                                                break;
                                            }
                                        }

                                        writer.write("ACCOUNT EDIT SUCCESSFUL");
                                        writer.println();
                                        writer.flush();
                                    }
                                }
                                case "EMAIL" -> {
                                    String email = userInfoSplit[3];

                                    if (email.equals(newInfo)) {
                                        writer.write("NO CHANGE");
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        boolean newInfoTaken = false;

                                        for (String line : fileContents) {
                                            String[] splitLine = line.split(";");

                                            if (splitLine[3].equals(newInfo)) {
                                                newInfoTaken = true;
                                                break;
                                            }
                                        }

                                        if (!newInfoTaken) {
                                            for (String line : fileContents) {
                                                if (line.equals(userInfo)) {
                                                    String newLine = userInfoSplit[0] + ";" + userInfoSplit[1] + ";" +
                                                            userInfoSplit[2] + ";" + newInfo;

                                                    fileContents.set(fileContents.indexOf(line), newLine);

                                                    rewriteFile(usersFile, fileContents);
                                                    break;
                                                }
                                            }

                                            writer.write("ACCOUNT EDIT SUCCESSFUL");
                                            writer.println();
                                            writer.flush();
                                        } else {
                                            writer.write("NEW INFO TAKEN");
                                            writer.println();
                                            writer.flush();
                                        }
                                    }
                                }
                            }
                        } else {
                            writer.write("FILE ERROR");
                            writer.println();
                            writer.flush();
                        }
                    }
                    case "DELETE ACCOUNT" -> {
                        String username = reader.readLine();

                        ArrayList<String> fileContents = readFile(usersFile);

                        if (fileContents != null) {
                            String userInfo = getUserInfo(username);

                            for (String line : fileContents) {
                                if (line.equals(userInfo)) {
                                    fileContents.remove(line);

                                    rewriteFile(usersFile, fileContents);
                                    break;
                                }
                            }

                            boolean fileError = false;

                            fileContents = readFile(invisibleListFile);

                            if (fileContents != null) {
                                if (fileContents.size() > 0) {
                                    for (int i = 0; i < fileContents.size(); i++) {
                                        String line = fileContents.get(i);
                                        String[] splitLine = line.split(";invisible to;");

                                        if (splitLine[0].equals(username) || splitLine[1].equals(username)) {
                                            fileContents.remove(line);
                                            i = 0;
                                        }
                                    }

                                    if (fileContents.size() == 1) {
                                        String line = fileContents.get(0);
                                        String[] splitLine = line.split(";invisible to;");

                                        if (splitLine[0].equals(username) || splitLine[1].equals(username)) {
                                            fileContents.remove(line);
                                        }
                                    }

                                    rewriteFile(invisibleListFile, fileContents);
                                }
                            } else {
                                fileError = true;
                            }

                            fileContents = readFile(blockListFile);

                            if (fileContents != null) {
                                if (fileContents.size() > 0) {
                                    for (int i = 0; i < fileContents.size(); i++) {
                                        String line = fileContents.get(i);
                                        String[] splitLine = line.split(";blocked;");

                                        if (splitLine[0].equals(username) || splitLine[1].equals(username)) {
                                            fileContents.remove(line);
                                            i = 0;
                                        }
                                    }

                                    if (fileContents.size() == 1) {
                                        String line = fileContents.get(0);
                                        String[] splitLine = line.split(";blocked;");

                                        if (splitLine[0].equals(username) || splitLine[1].equals(username)) {
                                            fileContents.remove(line);
                                        }
                                    }

                                    rewriteFile(blockListFile, fileContents);
                                }
                            } else {
                                fileError = true;
                            }

                            fileContents = readFile(conversationLogFile);

                            if (fileContents != null) {
                                if (fileContents.size() > 0) {
                                    for (int i = 0; i < fileContents.size(); i++) {
                                        String line = fileContents.get(i);
                                        String[] splitLine = line.split(" & ");

                                        if (splitLine[0].equals(username) || splitLine[1].equals(username)) {
                                            fileContents.remove(line);

                                            File fileToDelete = new File(line + ".txt");
                                            fileToDelete.delete();

                                            i = 0;
                                        }
                                    }

                                    if (fileContents.size() == 1) {
                                        String line = fileContents.get(0);
                                        String[] splitLine = line.split(" & ");

                                        if (splitLine[0].equals(username) || splitLine[1].equals(username)) {
                                            fileContents.remove(line);

                                            File fileToDelete = new File(line + ".txt");
                                            fileToDelete.delete();
                                        }
                                    }

                                    rewriteFile(conversationLogFile, fileContents);
                                }
                            } else {
                                fileError = true;
                            }

                            String[] userInfoSplit = userInfo.split(";");
                            String status = userInfoSplit[0];

                            if (status.equals("seller")) {
                                fileContents = readFile(storesFile);

                                if (fileContents != null) {
                                    if (fileContents.size() > 0) {
                                        for (int i = 0; i < fileContents.size(); i++) {
                                            String line = fileContents.get(i);
                                            String[] splitLine = line.split(";owned by;");

                                            if (splitLine[1].equals(username)) {
                                                fileContents.remove(line);
                                                i = 0;
                                            }
                                        }

                                        if (fileContents.size() == 1) {
                                            String line = fileContents.get(0);
                                            String[] splitLine = line.split(";owned by;");

                                            if (splitLine[1].equals(username)) {
                                                fileContents.remove(line);
                                            }
                                        }

                                        rewriteFile(storesFile, fileContents);
                                    }
                                } else {
                                    fileError = true;
                                }
                            }

                            if (fileError) {
                                writer.write("FILE ERROR");
                                writer.println();
                                writer.flush();
                            } else {
                                writer.write("ACCOUNT DELETION SUCCESSFUL");
                                writer.println();
                                writer.flush();
                            }
                        } else {
                            writer.write("FILE ERROR");
                            writer.println();
                            writer.flush();
                        }
                    }
                    case "UPDATE BLOCK STATUS" -> {
                        String username = reader.readLine();
                        String userToBlock = reader.readLine();

                        ArrayList<String> fileContents = readFile(usersFile);

                        if (fileContents != null) {
                            boolean userFound = false;

                            String[] userInfoSplit = getUserInfo(username).split(";");
                            String status = userInfoSplit[0];

                            for (String line : fileContents) {
                                String[] splitLine = line.split(";");

                                if (status.equals("customer")) {
                                    if (splitLine[0].equals("seller") && splitLine[1].equals(userToBlock)) {
                                        userFound = true;
                                    }
                                } else if (status.equals("seller")) {
                                    if (splitLine[0].equals("customer") && splitLine[1].equals(userToBlock)) {
                                        userFound = true;
                                    }
                                }
                            }

                            if (userFound) {
                                String result = updateBlockStatus(username, userToBlock);

                                writer.write(result.toLowerCase());
                                writer.println();
                                writer.flush();
                            } else {
                                writer.write("USER NOT FOUND");
                                writer.println();
                                writer.flush();
                            }
                        } else {
                            writer.write("FILE ERROR");
                            writer.println();
                            writer.flush();
                        }
                    }
                    case "UPDATE VISIBILITY" -> {
                        String username = reader.readLine();
                        String userToCheck = reader.readLine();

                        ArrayList<String> fileContents = readFile(usersFile);

                        if (fileContents != null) {
                            boolean userFound = false;

                            String[] userInfoSplit = getUserInfo(username).split(";");
                            String status = userInfoSplit[0];

                            for (String line : fileContents) {
                                String[] splitLine = line.split(";");

                                if (status.equals("customer")) {
                                    if (splitLine[0].equals("seller") && splitLine[1].equals(userToCheck)) {
                                        userFound = true;
                                    }
                                } else if (status.equals("seller")) {
                                    if (splitLine[0].equals("customer") && splitLine[1].equals(userToCheck)) {
                                        userFound = true;
                                    }
                                }
                            }

                            if (userFound) {
                                String result = updateVisibility(username, userToCheck);

                                writer.write(result.toLowerCase());
                                writer.println();
                                writer.flush();
                            } else {
                                writer.write("USER NOT FOUND");
                                writer.println();
                                writer.flush();
                            }
                        } else {
                            writer.write("FILE ERROR");
                            writer.println();
                            writer.flush();
                        }
                    }
                    case "SEND MESSAGE" -> {
                        String username = reader.readLine();
                        String recipient = reader.readLine();
                        String message = reader.readLine();

                        String result = sendMessage(username, recipient, message);

                        writer.write(result);
                        writer.println();
                        writer.flush();
                    }
                    case "GET CONVERSATION" -> {
                        String username = reader.readLine();
                        String recipient = reader.readLine();

                        ArrayList<String> fileContents = readFile(new File(username + " & " + recipient +
                                ".txt"));

                        if (fileContents != null) {
                            writer.write("CONVERSATION FOUND");
                            writer.println();
                            writer.flush();

                            for (String line : fileContents) {
                                writer.write(line);
                                writer.println();
                                writer.flush();
                            }

                            writer.write("END;");
                            writer.println();
                            writer.flush();
                        } else {
                            writer.write("FILE ERROR");
                            writer.println();
                            writer.flush();
                        }
                    }
                    case "DELETE MESSAGE" -> {
                        String username = reader.readLine();
                        String recipient = reader.readLine();

                        File conversationToEdit = new File(username + " & " + recipient + ".txt");

                        ArrayList<String> fileContents = readFile(conversationToEdit);

                        if (fileContents != null) {
                            if (fileContents.size() > 0) {
                                ArrayList<String> messagesSent = new ArrayList<>();

                                for (String line : fileContents) {
                                    String[] splitLine = line.split(" @ ");
                                    String[] users = splitLine[0].split(" to ");

                                    if (users[0].equals(username)) {
                                        messagesSent.add(line);
                                    }
                                }

                                if (messagesSent.size() > 0) {
                                    writer.write("MESSAGES FOUND");
                                    writer.println();
                                    writer.flush();

                                    for (String message : messagesSent) {
                                        writer.write(message);
                                        writer.println();
                                        writer.flush();
                                    }

                                    writer.write("END;");
                                    writer.println();
                                    writer.flush();

                                    String messageToDelete = reader.readLine();

                                    for (String line : fileContents) {
                                        if (line.equals(messageToDelete)) {
                                            fileContents.remove(line);

                                            rewriteFile(conversationToEdit, fileContents);
                                            break;
                                        }
                                    }
                                } else {
                                    writer.write("NO MESSAGES SENT");
                                    writer.println();
                                    writer.flush();
                                }
                            } else {
                                writer.write("NO MESSAGES SENT");
                                writer.println();
                                writer.flush();
                            }
                        } else {
                            writer.write("FILE ERROR");
                            writer.println();
                            writer.flush();
                        }
                    }
                    case "EDIT MESSAGE" -> {
                        String username = reader.readLine();
                        String recipient = reader.readLine();

                        File userToRecipient = new File(username + " & " + recipient + ".txt");
                        File recipientToUser = new File(recipient + " & " + username + ".txt");

                        ArrayList<String> fileContents = readFile(userToRecipient);

                        if (fileContents != null) {
                            if (fileContents.size() > 0) {
                                ArrayList<String> messagesSent = new ArrayList<>();

                                for (String line : fileContents) {
                                    String[] splitLine = line.split(" @ ");
                                    String[] users = splitLine[0].split(" to ");

                                    if (users[0].equals(username)) {
                                        messagesSent.add(line);
                                    }
                                }

                                if (messagesSent.size() > 0) {
                                    writer.write("MESSAGES FOUND");
                                    writer.println();
                                    writer.flush();

                                    for (String message : messagesSent) {
                                        writer.write(message);
                                        writer.println();
                                        writer.flush();
                                    }

                                    writer.write("END;");
                                    writer.println();
                                    writer.flush();

                                    String messageToEdit = reader.readLine();
                                    String newMessage = reader.readLine();

                                    String localDateTime = String.valueOf(LocalDateTime.now());
                                    String date = localDateTime.substring(0, localDateTime.indexOf('T'));
                                    String time = localDateTime.substring(localDateTime.indexOf('T') + 1,
                                            localDateTime.indexOf('.'));

                                    String newLine = username + " to " + recipient + " (edited) @ " + date + " " +
                                            time + ": " + newMessage;

                                    for (String line : fileContents) {
                                        if (line.equals(messageToEdit)) {
                                            fileContents.set(fileContents.indexOf(line), newLine);

                                            rewriteFile(userToRecipient, fileContents);
                                            break;
                                        }
                                    }

                                    fileContents = readFile(recipientToUser);

                                    if (fileContents != null) {
                                        for (String line : fileContents) {
                                            if (line.equals(messageToEdit)) {
                                                fileContents.set(fileContents.indexOf(line), newLine);

                                                rewriteFile(recipientToUser, fileContents);
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    writer.write("NO MESSAGES SENT");
                                    writer.println();
                                    writer.flush();
                                }
                            } else {
                                writer.write("NO MESSAGES SENT");
                                writer.println();
                                writer.flush();
                            }
                        } else {
                            writer.write("FILE ERROR");
                            writer.println();
                            writer.flush();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String checkLogin(String usernameEntered, String passwordEntered) {
        ArrayList<String> fileContents = readFile(usersFile);

        if (fileContents != null) {
            if (fileContents.size() > 0) {
                for (String line : fileContents) {
                    String[] splitLine = line.split(";");

                    if (splitLine[1].equals(usernameEntered)) {
                        if (splitLine[2].equals(passwordEntered)) {
                            return "LOGIN SUCCESSFUL";
                        }
                    }
                }
            }
        } else {
            return "FILE ERROR";
        }

        return "LOGIN UNSUCCESSFUL";
    }

    public synchronized String createAccount(String statusSelected, String usernameEntered,
                                             String passwordEntered, String emailEntered) {
        ArrayList<String> fileContents = readFile(usersFile);

        if (fileContents != null) {
            boolean usernameTaken = false;
            boolean emailTaken = false;

            if (fileContents.size() > 0) {
                for (String line : fileContents) {
                    String[] splitLine = line.split(";");

                    if (splitLine[1].equals(usernameEntered)) {
                        usernameTaken = true;
                    }
                    if (splitLine[3].equals(emailEntered)) {
                        emailTaken = true;
                    }
                }
            }

            if (usernameTaken && emailTaken) {
                return "BOTH TAKEN";
            } else if (usernameTaken) {
                return "USERNAME TAKEN";
            } else if (emailTaken) {
                return "EMAIL TAKEN";
            }
        } else {
            return "FILE ERROR";
        }

        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(usersFile, true));
            pw.println(statusSelected + ";" + usernameEntered + ";" + passwordEntered + ";" + emailEntered);
            pw.close();
        } catch (FileNotFoundException e) {
            return "FILE NOT FOUND";
        }

        return "ACCOUNT CREATION SUCCESSFUL";
    }

    public synchronized String createStore(String username, String storeNameEntered) {
        ArrayList<String> fileContents = readFile(storesFile);

        if (fileContents != null) {
            if (fileContents.size() > 0) {
                for (String line : fileContents) {
                    String[] splitLine = line.split(";owned by;");

                    if (splitLine[0].equals(storeNameEntered)) {
                        return "NAME TAKEN";
                    }
                }
            }
        } else {
            return "FILE ERROR";
        }

        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(storesFile, true));
            pw.println(storeNameEntered + ";owned by;" + username);
            pw.close();
        } catch (FileNotFoundException e) {
            return "FILE NOT FOUND";
        }

        return "STORE CREATION SUCCESSFUL";
    }

    public static String getUserInfo(String username) {
        ArrayList<String> fileContents = readFile(usersFile);

        if (fileContents != null) {
            if (fileContents.size() > 0) {
                for (String line : fileContents) {
                    String[] splitLine = line.split(";");

                    if (splitLine[1].equals(username)) {
                        return line;
                    }
                }
            }
        }

        return null;
    }

    public synchronized void createConversationFiles(String username, String recipient) {
        File userAndRecipient = new File(username + " & " + recipient + ".txt");
        File recipientAndUser = new File(recipient + " & " + username + ".txt");

        File[] conversation = {userAndRecipient, recipientAndUser};

        try {
            for (File file : conversation) {
                if (!file.exists()) {
                    file.createNewFile();

                    PrintWriter pw = new PrintWriter(new FileOutputStream(conversationLogFile, true));
                    pw.println(file.getName().substring(0, file.getName().indexOf('.')));
                    pw.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized String sendMessage(String username, String recipient, String message) {
        File userAndRecipient = new File(username + " & " + recipient + ".txt");
        File recipientAndUser = new File(recipient + " & " + username + ".txt");

        File[] conversation = {userAndRecipient, recipientAndUser};

        try {
            for (File file : conversation) {
                if (!file.exists()) {
                    file.createNewFile();

                    PrintWriter pw = new PrintWriter(new FileOutputStream(conversationLogFile, true));
                    pw.println(file.getName().substring(0, file.getName().indexOf('.')));
                    pw.close();
                }

                String localDateTime = String.valueOf(LocalDateTime.now());
                String date = localDateTime.substring(0, localDateTime.indexOf('T'));
                String time = localDateTime.substring(localDateTime.indexOf('T') + 1, localDateTime.indexOf('.'));

                PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
                pw.println(username + " to " + recipient + " @ " + date + " " + time + ": " + message);
                pw.close();
            }
        } catch (IOException e) {
            return "MESSAGE SEND UNSUCCESSFUL";
        }

        return "MESSAGE SEND SUCCESSFUL";
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
            return null;
        }

        return fileContents;
    }

    public static boolean isVisible(String username, String userToCheck) {
        ArrayList<String> fileContents = readFile(invisibleListFile);

        if (fileContents != null) {
            for (String line : fileContents) {
                if (line.equals(userToCheck + ";invisible to;" + username)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static String getBlockStatus(String username, String userToCheck) {
        ArrayList<String> fileContents = readFile(blockListFile);

        if (fileContents != null) {
            for (String line : fileContents) {
                if (line.equals(userToCheck + ";blocked;" + username)) {
                    return "BLOCKED YOU";
                } else if (line.equals(username + ";blocked;" + userToCheck)) {
                    return "YOU BLOCKED";
                }
            }

            return "NOT BLOCKED";
        }
        return "FILE ERROR";
    }

    public synchronized void rewriteFile(File fileToRewrite, ArrayList<String> newFileContents) {
        StringBuilder newFile = new StringBuilder();

        for (String line : newFileContents) {
            newFile.append(line).append("\n");
        }

        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileToRewrite, false));
            pw.print(newFile);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized String updateBlockStatus(String username, String userToCheck) {
        ArrayList<String> fileContents = readFile(blockListFile);

        if (fileContents != null) {
            if (fileContents.size() > 0) {
                for (String line : fileContents) {
                    if (line.equals(username + ";blocked;" + userToCheck)) {
                        fileContents.remove(line);
                        rewriteFile(blockListFile, fileContents);

                        return "UNBLOCKED";
                    }
                }
            }

            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(blockListFile, true));
                pw.println(username + ";blocked;" + userToCheck);
                pw.close();
            } catch (FileNotFoundException e) {
                return "FILE NOT FOUND";
            }

            return "BLOCKED";
        }

        return "FILE ERROR";
    }

    public synchronized String updateVisibility(String username, String userToCheck) {
        ArrayList<String> fileContents = readFile(invisibleListFile);

        if (fileContents != null) {
            if (fileContents.size() > 0) {
                for (String line : fileContents) {
                    if (line.equals(username + ";invisible to;" + userToCheck)) {
                        fileContents.remove(line);
                        rewriteFile(invisibleListFile, fileContents);

                        return "VISIBLE";
                    }
                }
            }

            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(invisibleListFile, true));
                pw.println(username + ";invisible to;" + userToCheck);
                pw.close();
            } catch (FileNotFoundException e) {
                return "FILE NOT FOUND";
            }

            return "INVISIBLE";
        }

        return "FILE ERROR";
    }
}