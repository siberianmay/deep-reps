# GitHub Secrets Configuration

This document lists all secrets that must be configured in the GitHub repository settings for CI/CD pipelines to function.

## Required Secrets for CI Pipeline

### Development
- `GEMINI_DEV_API_KEY` - Gemini API key for development builds (used in assembleDebug)

## Required Secrets for Release Pipeline

### Keystore Signing
- `KEYSTORE_BASE64` - Base64-encoded upload keystore file
  - Generate with: `base64 -w 0 app/upload.keystore`
- `KEYSTORE_PASSWORD` - Password for the keystore file
- `KEY_ALIAS` - Alias of the key within the keystore
- `KEY_PASSWORD` - Password for the key alias

### API Keys
- `GEMINI_PROD_API_KEY` - Gemini API key for production builds

### Play Store Publishing
- `PLAY_SERVICE_ACCOUNT_JSON` - Google Play Service Account JSON (plain text, not base64)
  - Create service account in Google Cloud Console
  - Grant "Release Manager" role in Play Console
  - Generate and download JSON key

## Configuration Steps

1. Navigate to repository Settings > Secrets and variables > Actions
2. Click "New repository secret"
3. Add each secret listed above with its corresponding value

## Security Notes

- NEVER commit these secrets to the repository
- Rotate secrets if exposed
- Use separate API keys for dev/staging/prod environments
- Service account should have minimum required permissions (principle of least privilege)
- Upload keystore should be backed up securely offline

## Keystore Generation (First-Time Setup)

```bash
# Generate upload keystore (one-time operation)
keytool -genkey -v -keystore app/upload.keystore \
  -alias upload -keyalg RSA -keysize 2048 -validity 10000

# Convert to base64 for GitHub secret
base64 -w 0 app/upload.keystore > upload.keystore.base64.txt

# Store the base64 output as KEYSTORE_BASE64 secret
# BACKUP the original upload.keystore file securely offline
# NEVER commit the keystore file to git
```

## Play App Signing

Deep Reps uses Google Play App Signing:
- Upload keystore is used to sign AAB before upload
- Google manages the app signing key
- Ensures key security and enables key rotation if upload key is compromised

See: https://developer.android.com/studio/publish/app-signing#app-signing-google-play
