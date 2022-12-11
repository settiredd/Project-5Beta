# Project-5Beta

**This repository was shifted over from a different one**
-On the previous, Ben handled some of the exception handling, server fields creation, isInvisible method, and isBlocked method. He also created a readFile method that was used throughout the whole code as well as a selectOptions method that was used throughout the code as well.

Project 5: EZ Messenger
-
EZ Messenger is an application that allows sellers and customers to communicate using a chat system.

Compile & Run
-
- Download all classes.
- Run server and then run client.
- Relevant files are created outside of the src folder
- To run multiple clients (concurrently) edit Client class configurations to run multiple instances.

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
- server description

**Client**
- client description

**LoginFrame**
- LoginFrame description

**CreateAccountFrame**
- CreateAccountFrame description

**SellerFrame**
- SellerFrame description

**CustomerFrame**
- CustomerFrame description

**ChatFrame**
- ChatFrame description
