# Project-5Beta

**This repository was shifted over from a different one**
-On the previous, Ben handled some of the exception handling, server fields creation, isInvisible method, and isBlocked method. He also created a readFile method that was used throughout the whole code as well as a selectOptions method that was used throughout the code as well.

Project 5: EZ Messenger
-
EZ Messenger is an application that allows sellers and customers to communicate using a chat system.

Seller statistics was optional for us to complete since a group of three is only required to do the core and one 
selection (we did core and blocking)

Compile & Run
-
- Download all classes.
- Run server and then run client.
- Relevant files are created outside of the src folder
- To run multiple clients (concurrently) edit Client class configurations to run multiple instances.
- Change port number on client on server side if you run into connection issues (make sure they are the same on both 
- ends)

File Formatting
-
**users.txt**
- users.txt: stores list of all users, their status (seller or customer), email, and password.
- users.txt formatting: status;username;password;email. 
- EXAMPLE: seller;sellerExample;password;example@gmail.com

**stores.txt**
- stores.txt: stores.txt stores a list of all stores along with the owners of each store
- stores.txt formatting: username;store name. 
- EXAMPLE: sellerExample;ToysRUs

**blockLists.txt & invisibleList.txt**
- blockLists.txt & invisibleList.txt: stores who users are blocked by/invisible to/who users blocked.
- blockList.txt formatting: userBlocking;blocked;userBlocked. 
- EXAMPLE: sellerExample;blocked;customerExample
- invisibleList.txt formatting: userInvisible;invisible to;userInvisibleTo.
- EXAMPLE: sellerExample;invisible to;customerExample

**conversationLog.txt**
- conversationLog.txt: stores list of conversations between users
- conversationLog.txt formatting: line 1: user1 & user2, line 2: user2 & user1
- EXAMPLE: line 1: sellerExample & customerExample, line 2: customerExample & sellerExample
- IF FILES ARE MANUALLY EDITED PLEASE ENSURE ONE BLANK LINE UNDER LAST TEXT LINE

Class Descriptions
-
**Server**
- The server is what handles all the backend of this program. It is also concurrent and allows multiple users because
of the way we launched new threads each time a client was connected. In the run method of server, we handle all the
"commands" that are sent from the client. A few examples of the commands are "Login", "MesageOptions", "Edit Account", 
and "Delete account." There are countless other commands to mention but those are just a few. The server taken in the
commands and writes back the proper information that the client is asking for.

**Client**
- When the client is run, it tries to get a connection established with the server. If the connection is not
established, the user gets and error that connection couldn't be established. If connection is established, we 
immediately invoke the LoginFrame.

**LoginFrame**
- In the beginning of LoginFrame, the user is asked if they want to login, create an account, or quit. If they want to 
create an account, the CreateAccountFrame is invoked. If the want to quit, they are given a farewell message. Otherwise,
they stay in the login frame. Within the LoginFrame, the user types in their username and password and clicks the login
 button. If the data they put in matches an existing user, they will be taken to either the SellerFrame or CustomerFrame.
Otherwise, invalid inputs will be dealt with using JOptionPanes.

**CreateAccountFrame**
- This frame is where users create their accounts. They first choose the radiobutton of their status, then type in 
their desired username, password, and then email they want to use. If the usernames or emails are taken, the client will
be notified of that. 

**SellerFrame**
- The seller frame has buttons handling each action a seller could take. 
- Create store button: pops up a JOP where the user types in what they want to name their store and notifies them if 
the store was successfully created. 
- Message button: seller gets the option to search for a customer or to view a list of customers. Then depending on that 
they will be able to open the chat box with their selected user or be notifed that the person they want to message has 
either blocked/does not exist/or is a seller.
- The blocking button will give users the option to block/unblock a user and to become invisble/visble to a user. All 
invalid inputs are handled.
- Edit button: JOP will pop up asking what the user want to change. Then user will type in what they want to change 
their selected edit item to. We will check for invalid inputs and then inform the user of whether their change 
was successful. 
- Delete account button: user will be asked if they are sure they want to delete the account and then if confirm, they are 
taken back to the LoginFrame and they cannot login with the previous account.
- Logout button: takes user back to the LoginFrame.

**CustomerFrame**
- The customer frame has buttons handling each action a customer could take. 
- Message button: seller gets the option to search for a store or to view a list of stores. Then depending on that
    they will be able to open the chat box with their selected user or be notifed that the person they want to message has
    either blocked/does not exist/or is a seller.
- The blocking button will give users the option to block/unblock a user and to become invisble/visble to a user. All
  invalid inputs are handled.
- Edit button: JOP will pop up asking what the user want to change. Then user will type in what they want to change
  their selected edit item to. We will check for invalid inputs and then inform the user of whether their change
  was successful.
- Delete account button: user will be asked if they are sure they want to delete the account and then if confirm, they are
  taken back to the LoginFrame and they cannot login with the previous account.
- Logout button: takes user back to the LoginFrame.

**ChatFrame**
- This frame is where users do the messaging core. There is a large textarea in the center where the previous messages 
will be displayed. There is also a top panel of buttons and a bottom panel of buttons. Button descriptions below.
- Send button: will send the message user has typed in the text box next to send button.
- Refresh button: will refresh the chat to show new messages.
- Edit button: user will see a list of the messages they have sent and will choose one to edit. They will then be taken to a 
JOP where they type in what they want to change that message to. Edited messages will be shown in both ends of the chat.
You will have to refresh to see the edited message.
- Delete button: user will see a list of the messages they have sent and will choose which one they want to delete. 
Deleted messages only disappear on the end of the person that sent and deleted it. You will have to refresh chat to 
see the deleted message. (Other person in chat will still have the messages that a user deletes)
