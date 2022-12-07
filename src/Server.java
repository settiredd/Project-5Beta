import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
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
                                return false;
                            }
                            if (userExists && !correctPassword) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return userExists && correctPassword; //returns true if user exists and correct password is used
    }

    synchronized String checkCreation(String stat, String user, String pass, String ema) {
        boolean userUsed = false;           //checks if wanted username is used
        boolean emaUsed = false;            //checks if wanted email is used
        String result = "";
        try {
            ArrayList<String> fileContents;
            if (usersFile.exists()) {
                fileContents = readFile(usersFile);
                if (fileContents != null) {
                    if (fileContents.size() > 0) {
                        for (String line : fileContents) {
                            String[] splitLine = line.split(";");
                            if (splitLine[1].equals(user)) {            //compares usernames
                                userUsed = true;
                                break;
                            } else if (splitLine[3].equals(ema)) {
                                emaUsed = true;                         //compares emails
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
                    result = "Yes";                     //when "Yes" this user is successfully created

                    BufferedWriter userWriter = new BufferedWriter(new FileWriter(usersFile, true));
                    userWriter.write(stat + ";" + user + ";" + pass + ";" + ema);
                    userWriter.write("\n");
                    userWriter.close();                         //writes new user to users.txt file
                } else {
                    if (userUsed) {
                        result = "No-Username";         //tells client this username is being used
                    } else if (emaUsed) {
                        result = "No-Email";            //tells client this email is being used
                    }
                }
            }
        } catch (Exception e) {
            result = "No";          //handles any exception that may happen without error
        }
        if (result.isEmpty()) {
            result = "No";
        }
        return result;
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
            if (!conversationLogFile.exists()) {
                conversationLogFile.createNewFile();
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
                    case "Login" -> {                               //user is logging in
                        String checkUsername = bfr.readLine();
                        String checkPassword = bfr.readLine();

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
                        String user = bfr.readLine();           //gets username
                        String stat = bfr.readLine();           //gets username

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
                                                        sendUsers.add(userToCheck);  //does not list if user invisible
                                                    }
                                                }
                                            }
                                        }

                                        if (sendUsers.size() == 0) {
                                            pw.write("None");       //tells user that there is no one to message
                                            pw.println();
                                            pw.flush();
                                        } else {
                                            pw.write("Yes");        //lets client know users exist
                                            pw.println();
                                            pw.flush();

                                            for (int i = 0; i < sendUsers.size(); i++) {
                                                pw.write(sendUsers.get(i));    //sends possible recipients to client
                                                pw.println();
                                                pw.flush();
                                            }

                                            pw.write("End");    //lets client know that recipients list is done
                                            pw.println();
                                            pw.flush();
                                        }
                                    } else {
                                        pw.write("None");       //no users to message
                                        pw.println();
                                        pw.flush();
                                    }
                                } else {
                                    pw.write("None");           //no users to message
                                    pw.println();
                                    pw.flush();
                                }
                            }
                        } else if (stat.equals("customer")) {       //TODO
                            ArrayList<String> storesContent;
                            ArrayList<String> sendStores = new ArrayList<>();
                            String storeSeller;

                            synchronized (o) {
                                storesContent = readFile(storesFile);
                            }

                            if (storesContent != null) {
                                if (storesContent.size() > 0) {
                                    for (String line : storesContent) {
                                        String[] splitLine = line.split(";");
                                        storeSeller = splitLine[0];

                                        if (!isInvisible(storeSeller, user)) {
                                            sendStores.add(splitLine[1]);
                                        }
                                    }

                                    pw.write("Yes");        //lets client know users exist
                                    pw.println();
                                    pw.flush();

                                    for (int i = 0; i < sendStores.size(); i++) {
                                        pw.write(sendStores.get(i));    //sends possible recipients to client
                                        pw.println();
                                        pw.flush();
                                    }

                                    pw.write("End");    //lets client know that recipients list is done
                                    pw.println();
                                    pw.flush();

                                } else {
                                    pw.write("None");       //no users to message
                                    pw.println();
                                    pw.flush();
                                }
                            } else {
                                pw.write("None");       //no users to message
                                pw.println();
                                pw.flush();
                            }
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
                                                        String customerName = splitLine[1];
                                                        pw.write("Yes");      //checks if searched is a customer
                                                        pw.println();
                                                        pw.flush();

                                                        synchronized (o) {  //checks if convo file exists else creates
                                                            File f = new File(user + " & " + customerName);
                                                            File f2 = new File(customerName + " & " + user);

                                                            if (!f.exists() && !f2.exists()) {
                                                                f.createNewFile();
                                                                f2.createNewFile();

                                                                BufferedWriter convoLogWriter = new BufferedWriter(new
                                                                        FileWriter(conversationLogFile, true));
                                                                convoLogWriter.write(user + " & " + customerName
                                                                        + "\n");
                                                                convoLogWriter.write(customerName + " & " + user
                                                                        + '\n');
                                                                convoLogWriter.close();
                                                            }

                                                        }

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
                            ArrayList<String> fileContents;
                            boolean storeExists = false;
                            boolean blocked = false;
                            boolean invisible = false;
                            String storeSeller = "";

                            synchronized (o) {
                                fileContents = readFile(storesFile);
                            }

                            if (fileContents != null) {
                                if (fileContents.size() > 0) {
                                    for (String line : fileContents) {
                                        String[] splitLine = line.split(";");
                                        if (splitLine[1].equals(search)) {
                                            storeSeller = splitLine[0];
                                            storeExists = true;
                                            if (isBlocked(storeSeller, user)) {
                                                blocked = true;
                                            }
                                            if (isInvisible(storeSeller, user)) {
                                                invisible = true;
                                            }
                                        }
                                    }
                                } else {
                                    pw.write("No");
                                    pw.println();
                                    pw.flush();
                                }
                            } else {
                                pw.write("No");
                                pw.println();
                                pw.flush();
                            }

                            if (invisible) {
                                pw.write("No");
                                pw.println();
                                pw.flush();
                            } else if (blocked) {
                                pw.write("blocked");
                                pw.println();
                                pw.flush();
                            } else if (storeExists) {
                                pw.write("Yes");
                                pw.println();
                                pw.flush();

                                pw.write(storeSeller);      //sends over store seller
                                pw.println();
                                pw.flush();

                                synchronized (o) {      //checks if convo file exists else creates
                                    File f = new File(user + " & " + storeSeller);
                                    File f2 = new File(storeSeller + " & " + user);

                                    if (!f.exists() && !f2.exists()) {
                                        f.createNewFile();
                                        f2.createNewFile();

                                        BufferedWriter convoLogWriter =
                                                new BufferedWriter(new FileWriter(conversationLogFile, true));
                                        convoLogWriter.write(user + " & " + storeSeller + "\n");
                                        convoLogWriter.write(storeSeller + " & " + user + '\n');
                                        convoLogWriter.close();
                                    }
                                }
                            } else {
                                pw.write("No");
                                pw.println();
                                pw.flush();
                            }

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
                            pw.write("Yes");        //tells client that store was created
                            pw.println();
                            pw.flush();

                            writeStore(user, storeName);
                        } else {
                            pw.write("No");         //tells client store couldn't be created
                            pw.println();
                            pw.flush();
                        }
                    }
                    case "Edit" -> {
                        String user = bfr.readLine();           //gets username
                        String stat = bfr.readLine();           //gets client status
                        String itemToEdit = bfr.readLine();     //gets what the user is changing
                        String newItem = bfr.readLine();        //gets what the user wants to change it to
                        ArrayList<String> fileContents;
                        synchronized (o) {                      //concurrently reads file
                            fileContents = readFile(usersFile);
                        }

                        boolean beingUsed = false;
                        boolean same = false;

                        String[] infoSplit;
                        synchronized (o) {
                            infoSplit = getUserInfo(user).split(";");
                        }

                        switch (itemToEdit) {
                            case "Username":
                                String userUsername = infoSplit[1];         //gets user's current username

                                if (userUsername.equals(newItem)) {         //checks if user put their current username
                                    same = true;
                                } else {
                                    if (fileContents != null) {
                                        for (String line : fileContents) {
                                            String[] splitLine = line.split(";");

                                            if (splitLine[1].equals(newItem)) {
                                                beingUsed = true;           //if username is already being used
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (!beingUsed && !same) {
                                    pw.write("Yes");
                                    pw.println();
                                    pw.flush();

                                    for (int i = 0; i < fileContents.size(); i++) {
                                        if (fileContents.get(i).equals(getUserInfo(user))) {
                                            fileContents.remove(i);

                                            fileContents.add(getUserInfo(user).replace(infoSplit[1], newItem));
                                        }
                                    }

                                    synchronized (o) {              //concurrently writes to file
                                        BufferedWriter userWriter =
                                                new BufferedWriter(new FileWriter(usersFile, false));
                                        for (int i = 0; i < fileContents.size(); i++) {
                                            userWriter.write(fileContents.get(i) + "\n");
                                        }
                                        userWriter.close();
                                    }

                                    ArrayList<String> convoListContent;

                                    synchronized (o) {
                                        convoListContent = readFile(conversationLogFile);
                                    }

                                    if (convoListContent != null) {
                                        if (convoListContent.size() > 0) {
                                            String line;
                                            for (int i = 0; i < convoListContent.size(); i++) {
                                                line = convoListContent.get(i);
                                                String[] splitLine = line.split(" ");
                                                String user1 = splitLine[0];
                                                String user2 = splitLine[2];

                                                if (user1.equals(userUsername) || user2.equals(userUsername)) {
                                                    if (user1.equals(userUsername)) {
                                                        convoListContent.set(i, newItem + " & " + user2);
                                                        File f = new File(user1 + " & " + user2);
                                                        f.renameTo(new File(newItem + " & " + user2));
                                                        i = 0;
                                                    } else if (user2.equals(userUsername)) {
                                                        convoListContent.set(i, user1 + " & " + newItem);
                                                        File f = new File(user1 + " & " + user2);
                                                        f.renameTo(new File(user1 + " & " + newItem));
                                                    }
                                                }
                                            }

                                            synchronized (o) {
                                                BufferedWriter convoLogWriter = new BufferedWriter
                                                        (new FileWriter(conversationLogFile, false));
                                                for (int i = 0; i < convoListContent.size(); i++) {
                                                    convoLogWriter.write(convoListContent.get(i) + "\n");
                                                }
                                                convoLogWriter.close();
                                            }
                                        }
                                    }


                                    if (stat.equals("seller")) {
                                        synchronized (o) {
                                            ArrayList<String> storeList;
                                            storeList = readFile(storesFile);

                                            if (storeList != null) {
                                                if (storeList.size() > 0) {
                                                    String line = "";

                                                    for (int i = 0; i < storeList.size(); i++) {
                                                        line = storeList.get(i);
                                                        String[] splitLine = line.split(";");
                                                        String sellerName = splitLine[0];
                                                        String storeName = splitLine[1];

                                                        if (sellerName.equals(userUsername)) {
                                                            storeList.remove(i);
                                                            storeList.add(newItem + ";" + storeName);
                                                            i = 0;
                                                        }
                                                    }

                                                    BufferedWriter storeWriter = new
                                                            BufferedWriter(new FileWriter(storesFile, false));
                                                    for (String line2 : storeList) {
                                                        storeWriter.write(line2 + "\n");
                                                    }
                                                    storeWriter.close();
                                                }
                                            }
                                        }
                                    }
                                } else if (same) {
                                    pw.write("Same");           //tells client username is same
                                    pw.println();
                                    pw.flush();
                                } else if (beingUsed) {
                                    pw.write("Used");           //tells client that username is being used
                                    pw.println();
                                    pw.flush();
                                }

                                break;
                            case "Password":
                                String userPassword = infoSplit[2];         //gets user's current password

                                if (userPassword.equals(newItem)) {         //checks if user put their current password
                                    same = true;
                                }

                                if (same) {
                                    pw.write("Same");    //tells user that they input their current password
                                    pw.println();
                                    pw.flush();
                                } else {
                                    pw.write("Yes");
                                    pw.println();
                                    pw.flush();

                                    for (int i = 0; i < fileContents.size(); i++) {
                                        if (fileContents.get(i).equals(getUserInfo(user))) {
                                            fileContents.remove(i);

                                            fileContents.add(getUserInfo(user).replace(infoSplit[2], newItem));
                                        }
                                    }
                                    synchronized (o) {              //concurrently writes to file
                                        BufferedWriter passWriter =
                                                new BufferedWriter(new FileWriter(usersFile, false));
                                        for (int i = 0; i < fileContents.size(); i++) {
                                            passWriter.write(fileContents.get(i) + "\n");
                                        }
                                        passWriter.close();
                                    }
                                }


                                break;
                            case "Email":
                                String userEmail = infoSplit[3];            //gets user's current email

                                if (userEmail.equals(newItem)) {         //checks if user put their current email
                                    same = true;
                                } else {
                                    if (fileContents != null) {
                                        for (String line : fileContents) {
                                            String[] splitLine = line.split(";");

                                            if (splitLine[3].equals(newItem)) {
                                                beingUsed = true;           //if username is already being used
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (!beingUsed && !same) {
                                    pw.write("Yes");
                                    pw.println();
                                    pw.flush();

                                    for (int i = 0; i < fileContents.size(); i++) {
                                        if (fileContents.get(i).equals(getUserInfo(user))) {
                                            fileContents.remove(i);

                                            fileContents.add(getUserInfo(user).replace(infoSplit[3], newItem));
                                        }
                                    }
                                    synchronized (o) {              //concurrently writes to file
                                        BufferedWriter emailWriter =
                                                new BufferedWriter(new FileWriter(usersFile, false));
                                        for (int i = 0; i < fileContents.size(); i++) {
                                            emailWriter.write(fileContents.get(i) + "\n");
                                        }
                                        emailWriter.close();
                                    }
                                } else if (same) {
                                    pw.write("Same");           //tells client email is same
                                    pw.println();
                                    pw.flush();
                                } else if (beingUsed) {
                                    pw.write("Used");           //tells client that email is being used
                                    pw.println();
                                    pw.flush();
                                }
                                break;
                        }

                    }
                    case "Delete" -> {
                        String user = bfr.readLine();
                        ArrayList<String> fileContents;
                        boolean deleted = false;
                        synchronized (o) {
                            fileContents = readFile(usersFile);
                        }

                        for (int i = 0; i < fileContents.size(); i++) {
                            if (fileContents.get(i).equals(getUserInfo(user))) {
                                fileContents.remove(i);
                                deleted = true;
                            }
                        }

                        synchronized (o) {              //concurrently writes to file
                            BufferedWriter usersWriter =
                                    new BufferedWriter(new FileWriter(usersFile, false));
                            for (int i = 0; i < fileContents.size(); i++) {
                                usersWriter.write(fileContents.get(i) + "\n");
                            }
                            usersWriter.close();
                        }

                        if (deleted) {
                            pw.write("Success");
                            pw.println();
                            pw.flush();
                        } else {
                            pw.write("Fail");
                            pw.println();
                            pw.flush();
                        }

                    }
                    case "Block" -> {
                        String user = bfr.readLine();
                        String blocked = bfr.readLine();
                        boolean userExists = false;                 //checks if user exists
                        boolean alreadyBlocked = false;             //check if user already blocked "blocked"
                        ArrayList<String> usersContents;
                        ArrayList<String> blockContents;

                        synchronized (o) {
                            usersContents = readFile(usersFile);
                            blockContents = readFile(blockListFile);
                        }

                        for (String line : usersContents) {
                            String[] splitLine = line.split(";");
                            if (splitLine[1].equals(blocked)) {
                                userExists = true;
                                break;
                            }
                        }

                        if (!userExists) {
                            pw.write("No");             //tells client that this user doesn't exist
                            pw.println();
                            pw.flush();
                        } else {
                            for (String line : blockContents) {
                                if (line.equals(user + ";blocked;" + blocked)) {
                                    alreadyBlocked = true;
                                }
                            }

                            if (alreadyBlocked) {
                                pw.write("Already");         //tells client that this user is already blocked
                                pw.println();
                                pw.flush();
                            }
                        }

                        if (userExists && !alreadyBlocked) {
                            pw.write("Yes");                //Yes, user will be blocked
                            pw.println();
                            pw.flush();

                            synchronized (o) {              //concurrently writes in file
                                BufferedWriter blockWriter =
                                        new BufferedWriter(new FileWriter(blockListFile, true));
                                blockWriter.write(user + ";blocked;" + blocked + "\n");
                                blockWriter.close();
                            }
                        }
                    }
                    case "Invisible" -> {

                        String user = bfr.readLine();
                        String invisibleTo = bfr.readLine();
                        boolean userExists = false;                 //checks if user exists
                        boolean alreadyInvisible = false;             //check if user already invisible
                        ArrayList<String> usersContents;
                        ArrayList<String> invisibleContents;

                        synchronized (o) {
                            usersContents = readFile(usersFile);
                            invisibleContents = readFile(invisibleListFile);
                        }

                        for (String line : usersContents) {
                            String[] splitLine = line.split(";");
                            if (splitLine[1].equals(invisibleTo)) {
                                userExists = true;
                                break;
                            }
                        }

                        if (!userExists) {
                            pw.write("No");             //tells client that this user doesn't exist
                            pw.println();
                            pw.flush();
                        } else {
                            for (String line : invisibleContents) {
                                if (line.equals(user + ";invisible to;" + invisibleTo)) {
                                    alreadyInvisible = true;
                                }
                            }

                            if (alreadyInvisible) {
                                pw.write("Already");         //tells client that this user is already blocked
                                pw.println();
                                pw.flush();
                            }
                        }

                        if (userExists && !alreadyInvisible) {
                            pw.write("Yes");                //Yes, user will be blocked
                            pw.println();
                            pw.flush();

                            synchronized (o) {              //concurrently writes in file
                                BufferedWriter invisibleWriter =
                                        new BufferedWriter(new FileWriter(invisibleListFile, true));
                                invisibleWriter.write(user + ";invisible to;" + invisibleTo + "\n");
                                invisibleWriter.close();
                            }
                        }
                    }
                    case "SendMessage" -> {
                        try {
                            String user = bfr.readLine();
                            String messageTo = bfr.readLine();
                            String message = bfr.readLine();

                            sendMessage(user, messageTo, message);
                            pw.write("Yes");
                            pw.println();
                            pw.flush();
                        } catch (Exception e) {
                            pw.write("No");
                            pw.println();
                            pw.flush();
                        }
                    }
                    case "ChatRunning" -> {
                        try {
                            String user = bfr.readLine();

                            String receiver = bfr.readLine();
                            ArrayList<String> fileContents;

                            synchronized (o) {
                                fileContents = readFile(new File(user + " & " + receiver));
                            }

                            for (int i = 0; i < fileContents.size(); i++) {
                                pw.write(fileContents.get(i));
                                pw.println();
                                pw.flush();
                            }

                            pw.write("End");
                            pw.println();
                            pw.flush();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    case "DeleteMessage" -> {     //only delete it for the user who initiated the delete operation.
                        String user = bfr.readLine();
                        String recipient = bfr.readLine();
                        ArrayList<String> fileContents;
                        ArrayList<String> userMessages = new ArrayList<>();

                        synchronized (o) {
                            fileContents = readFile(new File(user + " & " + recipient));
                        }

                        if (fileContents != null) {
                            if (fileContents.size() > 0) {
                                for (String line : fileContents) {
                                    String[] userSplit = line.split(" ");
                                    String sender = userSplit[0];
                                    String receiver = userSplit[2];

                                    if (sender.equals(user)) {
                                        String[] messageSplit = line.split(":");
                                        String message = messageSplit[3];
                                        userMessages.add(message);
                                    }
                                }

                                if (userMessages != null) {
                                    if (userMessages.size() > 0) {
                                        pw.write("Yes");
                                        pw.println();
                                        pw.flush();

                                        for (int i = 0; i < userMessages.size(); i++) {
                                            pw.write(userMessages.get(i));
                                            pw.println();
                                            pw.flush();
                                        }

                                        pw.write("End");
                                        pw.println();
                                        pw.flush();

                                        String continueResponse = bfr.readLine();
                                        if (continueResponse.equals("Continue")) {
                                            String messageToDelete = bfr.readLine();
                                            for (int i = 0; i < userMessages.size(); i++) {
                                                if (userMessages.get(i).equals(messageToDelete)) {
                                                    userMessages.remove(i);
                                                    fileContents.remove(i);
                                                    break;
                                                }
                                            }

                                            synchronized (o) {
                                                BufferedWriter deleteWriter = new BufferedWriter(
                                                        new FileWriter(user + " & " + recipient));
                                                for (int j = 0; j < fileContents.size(); j++) {
                                                    deleteWriter.write(fileContents.get(j) + "\n");
                                                }
                                                deleteWriter.close();
                                            }

                                            pw.write("Yes");
                                            pw.println();
                                            pw.flush();
                                        }

                                    } else {
                                        pw.write("SentNone");
                                        pw.println();
                                        pw.flush();
                                    }
                                } else {
                                    pw.write("SentNone");
                                    pw.println();
                                    pw.flush();
                                }
                            } else {
                                pw.write("NoMessages");
                                pw.println();
                                pw.flush();
                            }
                        } else {
                            pw.write("NoMessages");
                            pw.println();
                            pw.flush();
                        }
                    }
                    case "EditMessage" -> {
                        String user = bfr.readLine();
                        String recipient = bfr.readLine();
                        ArrayList<String> fileContents1;            //user to recipient fileContents
                        ArrayList<String> fileContents2;            //recipient to user fileContents
                        ArrayList<String> userMessages = new ArrayList<>();

                        synchronized (o) {
                            fileContents1 = readFile(new File(user + " & " + recipient));
                        }
                        synchronized (o) {
                            fileContents2 = readFile(new File(recipient + " & " + user));
                        }

                        if (fileContents1 != null) {
                            if (fileContents1.size() > 0) {
                                for (String line : fileContents1) {
                                    String[] userSplit = line.split(" ");
                                    String sender = userSplit[0];
                                    String receiver = userSplit[2];

                                    if (sender.equals(user)) {
                                        String[] messageSplit = line.split(":");
                                        String message = messageSplit[3];
                                        userMessages.add(message);
                                    }
                                }

                                if (userMessages != null) {
                                    if (userMessages.size() > 0) {
                                        pw.write("Yes");
                                        pw.println();
                                        pw.flush();

                                        for (int i = 0; i < userMessages.size(); i++) {
                                            pw.write(userMessages.get(i));
                                            pw.println();
                                            pw.flush();
                                        }

                                        pw.write("End");
                                        pw.println();
                                        pw.flush();         //same as delete until here

                                        String continuePrompt = bfr.readLine();

                                        if (continuePrompt.equals("Continue")) {
                                            String messageToEdit = bfr.readLine();    //message the user wants to edit
                                            String newMessage = bfr.readLine();     //thing that message is changed to

                                            for (int i = 0; i < userMessages.size(); i++) {
                                                if (userMessages.get(i).equals(messageToEdit)) {
                                                    userMessages.set(i, newMessage);

                                                    String messageLine = fileContents1.get(i);
                                                    String[] splitMessage = messageLine.split(":");

                                                    if (fileContents2 != null) {
                                                        if (fileContents2.size() > 0) {
                                                            for (int j = 0; j < fileContents2.size(); j++) {
                                                                if (fileContents2.get(j).equals(messageLine)) {
                                                                    fileContents2.set(j, splitMessage[0] + ":" +
                                                                            splitMessage[1] + ":" + splitMessage[2] +
                                                                            ":" + newMessage);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }

                                                    fileContents1.set(i, splitMessage[0] + ":" + splitMessage[1] + ":" +
                                                            splitMessage[2] + ":" + newMessage);
                                                    break;
                                                }
                                            }

                                            synchronized (o) {
                                                BufferedWriter editWriter = new BufferedWriter(
                                                        new FileWriter(user + " & " + recipient,
                                                                false));
                                                for (int x = 0; x < fileContents1.size(); x++) {
                                                    editWriter.write(fileContents1.get(x) + "\n");
                                                }
                                                editWriter.close();
                                            }

                                            synchronized (o) {
                                                BufferedWriter file2Writer = new BufferedWriter(new
                                                        FileWriter(recipient + " & " + user));
                                                for (int x = 0; x < fileContents2.size(); x++) {
                                                    file2Writer.write(fileContents2.get(x) + "\n");
                                                }
                                                file2Writer.close();
                                            }

                                            pw.write("Yes");
                                            pw.println();
                                            pw.flush();

                                        }
                                    } else {
                                        pw.write("SentNone");
                                        pw.println();
                                        pw.flush();
                                    }
                                } else {
                                    pw.write("SentNone");
                                    pw.println();
                                    pw.flush();
                                }
                            } else {
                                pw.write("NoMessages");
                                pw.println();
                                pw.flush();
                            }
                        } else {
                            pw.write("NoMessages");
                            pw.println();
                            pw.flush();
                        }
                    }
                }
            }

        } catch (Exception e) {
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
            e.printStackTrace();
        }
        return userInfo;
    }

    synchronized void sendMessage(String user, String messageTo, String message) {
        try {
            File f = new File(user + " & " + messageTo);
            File f2 = new File(messageTo + " & " + user);

            BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
            bw.write(user + " to " + messageTo + " @" + String.valueOf(LocalDateTime.now()) + " :" + message +
                    "\n");
            bw.close();

            BufferedWriter bw2 = new BufferedWriter(new FileWriter(f2, true));
            bw2.write(user + " to " + messageTo + " @" + String.valueOf(LocalDateTime.now()) + " :" + message +
                    "\n");
            bw2.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized void writeStore(String user, String storeName) {
        try {
            if (storesFile.exists()) {
                BufferedWriter bfr = new BufferedWriter(new FileWriter(storesFile, true));
                bfr.write(user + ";" + storeName + "\n");
                bfr.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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