# Gmail OAuth Setup Guide

This guide explains how to configure Gmail OAuth2 authentication for sending emails from the Eco Simulator application.

## Why OAuth2?

OAuth2 provides more secure authentication compared to SMTP passwords:
- No need to share your Gmail password
- No need to enable "Less secure app access"
- Works with 2-Factor Authentication (2FA) enabled
- More granular permission control
- Tokens can be revoked without changing your password

## Prerequisites

1. A Google Cloud Platform account
2. Gmail account to send emails from
3. Internet access to complete OAuth flow

## Setup Steps

### 1. Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Note your project name and ID

### 2. Enable Gmail API

1. In the Cloud Console, go to **APIs & Services** > **Library**
2. Search for "Gmail API"
3. Click on "Gmail API" and click **Enable**

### 3. Configure OAuth Consent Screen

1. Go to **APIs & Services** > **OAuth consent screen**
2. Choose **External** (unless you have a Google Workspace account)
3. Fill in the required information:
   - App name: `Eco Simulator`
   - User support email: Your email
   - Developer contact: Your email
4. Click **Save and Continue**
5. In **Scopes**, add the following scope:
   - `https://www.googleapis.com/auth/gmail.send`
6. Click **Save and Continue**
7. Add your email as a test user
8. Click **Save and Continue**

### 4. Create OAuth 2.0 Client ID

1. Go to **APIs & Services** > **Credentials**
2. Click **Create Credentials** > **OAuth client ID**
3. Choose **Desktop app** as the application type
4. Name it `Eco Simulator Desktop Client`
5. Click **Create**
6. **Download** the JSON file (credentials.json)
7. Save it to your Eco Simulator directory

### 5. Configure Eco Simulator

1. Launch Eco Simulator
2. Go to **Settings** or **Email Configuration**
3. Check the box **"Use Gmail OAuth2"**
4. Click **Browse** and select your `credentials.json` file
5. Enter your Gmail address in the **From Email** field
6. Click **Test Connection** to authenticate:
   - A browser window will open
   - Sign in to your Google account
   - Grant permissions to Eco Simulator
   - You should see "Authentication successful" message
7. Click **Save & Close**

### 6. First Authentication

The first time you connect:
1. A browser window will open automatically
2. Sign in to your Google account
3. Review and accept the permissions
4. You'll see a confirmation page
5. The browser window will close automatically

OAuth tokens are stored in the `tokens/` directory and will be reused for future sessions.

## Troubleshooting

### "Credentials file not found" Error

- Make sure you downloaded credentials.json from Google Cloud Console
- Verify the file path is correct in the settings
- Check that the file is named exactly `credentials.json`

### "Access blocked" or "This app isn't verified" Error

- Click "Advanced" on the warning screen
- Click "Go to Eco Simulator (unsafe)"
- This is normal for development apps not published to the public

### "Token has expired" Error

- Delete the `tokens/` directory
- Run the application again and re-authenticate

### Connection Test Fails

- Verify Gmail API is enabled in Google Cloud Console
- Check that you added yourself as a test user
- Ensure your credentials.json is from a Desktop application type
- Try clearing browser cookies and cache

## Security Best Practices

1. **Never commit credentials.json to version control**
   - It's already in `.gitignore`
   - Keep it secure on your local machine

2. **Revoke access if compromised**
   - Go to [Google Account Security](https://myaccount.google.com/permissions)
   - Remove "Eco Simulator" from third-party apps

3. **Regenerate credentials if needed**
   - Delete the OAuth Client ID in Google Cloud Console
   - Create a new one and download new credentials

4. **Monitor usage**
   - Check the [Gmail API dashboard](https://console.cloud.google.com/apis/dashboard) for usage

## Fallback to SMTP

If OAuth authentication fails, the system automatically falls back to SMTP if configured:
1. Configure SMTP settings in the same dialog
2. The system will try OAuth first
3. If OAuth fails, it will use SMTP
4. If both fail, emails are saved to `reports/failed_emails/`

## Additional Resources

- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Gmail API Overview](https://developers.google.com/gmail/api/guides)
- [OAuth 2.0 for Mobile & Desktop Apps](https://developers.google.com/identity/protocols/oauth2/native-app)

## Support

For issues specific to Eco Simulator's OAuth implementation:
1. Check the application logs in the console
2. Verify your credentials.json matches the example format
3. Ensure all steps above were completed correctly
