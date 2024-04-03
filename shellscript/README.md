# GitLab Server Maintenance Guide

## Reset Forgotten Password

If you or another user has forgotten their password, follow these steps to reset it:

1. **Access the Server Console**
   - Access the server console where GitLab is installed.

2. **Open the GitLab Rails Console**
   - Run the following command:
     ```bash
     sudo gitlab-rails console
     ```

3. **Locate the User Account**
   - Use the command below, replacing `USERNAME` with the actual username:
     ```ruby
     user = User.find_by(username: 'USERNAME')
     ```

4. **Set the New Password**
   - Replace `NEWPASSWORD` with the desired password and execute:
     ```ruby
     user.password = 'NEWPASSWORD'
     user.password_confirmation = 'NEWPASSWORD'
     ```

5. **Save the Changes**
   - Save the new password with:
     ```ruby
     user.save!
     ```

6. **Exit the Console**
   - Type `exit` to leave the console.

## Restart GitLab to Apply Configuration Changes

To apply any changes made to GitLab's configuration, such as after updating the external URL, execute the following command in your server console:

```bash
sudo gitlab-ctl reconfigure


