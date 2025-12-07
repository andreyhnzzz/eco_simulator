# OAuth Credentials Directory

This directory contains the OAuth2 credentials for Gmail API authentication.

## Setup Instructions

1. **Download credentials from Google Cloud Console**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create or select a project
   - Enable Gmail API
   - Configure OAuth consent screen
   - Create OAuth 2.0 Client ID (Desktop app type)
   - Download the credentials JSON file

2. **Place the credentials file**
   - Copy the downloaded file to this directory
   - Rename it to `credentials.json`
   - The file should be at: `src/main/resources/oauth/credentials.json`

3. **File format**
   - See `credentials.json.example` for the expected format
   - Your actual credentials will have real values for:
     - `client_id`
     - `client_secret`
     - `project_id`

## Alternative: External Credentials

You can also keep credentials outside the project:
- Use the "Browse" button in the Email Settings dialog
- Select a credentials.json file from any location
- The path will be saved and used instead of the resource path

## Security Notes

⚠️ **IMPORTANT**: 
- The `credentials.json` file is in `.gitignore` and will NOT be committed
- Never share your credentials file publicly
- Keep it secure on your local machine
- If compromised, revoke access in Google Cloud Console and generate new credentials

## Resources

- [Gmail OAuth Setup Guide](../../../GMAIL_OAUTH_SETUP.md)
- [Google OAuth Documentation](https://developers.google.com/identity/protocols/oauth2)
