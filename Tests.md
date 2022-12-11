TESTING
-
**Test 1**: Logging in (user exists)
- Click Login option then ok
- Type in corresponding user's password and username
- Click Login button
- Seller or customer frame will open

**Test 2**: Logging in (user doesn't exist)
- Click Login option then ok
- Type in incorrect user's password and username
- Click Login button
- Error "The username or password you entered is incorrect"
- Click ok and return to LoginFrame

**Test 3**: Creating Account (invalid inputs)
- Click Create Account option then ok
- Type an invalid username or password (less than 8 letters, has semicolon or space) or an invalid email
  (invalid email formatting)
- Click Create Account button
- Error "Please enter a valid username/password/email"
- Click ok and return to CreateAccountFrame

**Test 4**: Creating Account (valid inputs)
- Click Create Account option then ok
- Type a valid username and password (less than 8 letters, has semicolon or space) and a valid email
  (valid email formatting)
- Click Create Account button
- Takes you to status responding frame