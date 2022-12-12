TESTING
-
**Test 1**: Logging in (user exists)
- Click Login option then ok
- Type in corresponding user's password and username
- Click Login button
- RESULT: Seller or customer frame will open

TEST PASSED

**Test 2**: Logging in (user doesn't exist)
- Click Login option then ok
- Type in incorrect user's password and username
- Click Login button
- RESULT: Error "The username or password you entered is incorrect"
- Click ok and return to LoginFrame

TEST PASSED

**Test 3**: Creating Account (invalid inputs)
- Click Create Account option then ok
- Type an invalid username or password (less than 8 letters, has semicolon or space) or an invalid email
  (invalid email formatting)
- Click Create Account button
- RESULT: Error "Please enter a valid username/password/email"
- Click ok and return to CreateAccountFrame

TEST PASSED

**Test 4**: Creating Account (valid inputs)
- Click Create Account option then ok
- Type a valid username and password (less than 8 letters, has semicolon or space) and a valid email
  (valid email formatting)
- Click Create Account button
- RESULT: Takes you to status responding frame

TEST PASSED

**Test 5** Searching for an existing user
- On main menu, click message button
- Choose search for user
- type in the name of an existing user.
- RESULT: If you have blocked them/if they blocked you, error that you blocked them or they blocked you will pop up.
- RESULT :Else, chat box will open directed to the user you searched for.
- RESULT: If they are invisible, you will still be able to message them.

TEST PASSED

**Test 6** Searching for a nonexistent user
- On main menu, click message button
- Choose search for user
- type in the name of a nonexistent user.
- RESULT: You will get error message "No customers with that username exists"

TEST PASSED

**Test 7** Editing username
- Click Edit button on home screen.
- Click on username option
- RESULT: If you type in invalid username or username that is taken, you will get an error.
- RESULT: If you type in valid username that is valid, all instances of previous username will be replaced with new 
username

TEST PASSED

**Test 8** Editing password
- Click edit button on home screen
- Click password option
- RESULT: If you type in an invalid password, you will get an error message
- RESULT: Else, your password will be changed and that will be what you need to login next time

TEST PASSED

**Test 9** Editing email
- Click edit button on home screen
- Click email option
- RESULT: If you type in an invalid email, you will get an error message
- RESULT: Else, your email will be changed and that will be saved as your email

TEST PASSED

**Test 10** Editing Message
- While in ChatFrame, click edit button
- RESULT: if you sent no messages, error that you have sent no messages will pop up
- RESULT: if you have sent messages, a list of messages you have sent will be shown and you can choose one to edit. 
edit will be shown on both ends of the conversation.

TEST PASSED

**Test 11** Deleting Message
- While in ChatFrame, click delete button
- RESULT: if you sent no messages, error that you have sent no messages will pop up
- RESULT: if you have sent messages, a list of messages you have sent will be shown and you can choose one to delete.
  edit will be shown on one end (person who deleted) of the conversation.

TEST PASSED

**Test 11** Logging out
- Click Logout button on main menu
- RESULT: Menu frame will close and you will be taken to the LoginFrame again

TEST PASSED

**Test 12** Deleting account
- Click delete account on main menu
- Click "Yes" on the JOptionPane that asks for confirmation
- RESULT: You will be taken to login frame and all instances of your account will be deleted from the files

TEST PASSED

**Test 13** Blocking and Unblocking
- Click block/unblock on main menu
- Click block/unblock option in JOP
- Type in the name of user you want to block or unblock.
- RESULT: If invalid username is typed you will get an error saying that a user will that username doesn't exist
- RESULT: (valid user) If user was previously blocked, you will get a message saying that you have successfully 
unblocked them. If user was unblocked before, you will get a message saying that you have successfully blocked them.

TEST PASSED

**Test 14** Visibility/Invisibility
- Click block/unblock on main menu
- Click become visible/invisible option in JOP.
- RESULT: If invalid username is typed you will get an error saying that a user will that username doesn't exist
- RESULT: (valid user) If you were previously invisible, you will get a message saying that you have successfully
  become visible to them. If you were visible before, you will get a message saying that you have successfully 
become invisible for them.

TEST PASSED
