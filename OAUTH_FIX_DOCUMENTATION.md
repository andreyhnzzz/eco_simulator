# OAuth2 Module-Info Fix Documentation

## Problem Description

When running a JavaFX application with JPMS (Java Platform Module System) that uses Google OAuth2 client library with `LocalServerReceiver`, the following runtime error occurs:

```
java.lang.NoClassDefFoundError: com/sun/net/httpserver/HttpHandler
Caused by: java.lang.ClassNotFoundException: com.sun.net.httpserver.HttpHandler
```

## Root Cause Analysis

### Why the Error Occurs in Modular Environments

In modular Java applications (JPMS), modules must explicitly declare their dependencies through the `module-info.java` file. The Google OAuth client library (`google-oauth-client-jetty`) uses `LocalServerReceiver` for the OAuth2 authorization flow, which internally depends on the JDK's HTTP server implementation.

The class `com.sun.net.httpserver.HttpHandler` is part of the **`jdk.httpserver`** module, which is:
- **Not included by default** in the module graph
- A JDK platform module that provides HTTP server functionality
- Required for `LocalServerReceiver` to create a local HTTP server for OAuth callbacks

### Why It Works in Non-Modular Environments

In traditional classpath-based applications:
- All JDK platform modules are accessible by default
- No explicit module declaration is needed
- The `jdk.httpserver` module is automatically available

### Modular Environment Behavior

In JPMS applications:
- Only explicitly required modules are included in the module graph
- Platform modules like `jdk.httpserver` must be explicitly declared
- Without the declaration, classes from that module are not accessible at runtime

## Solution

Add the following line to your `module-info.java`:

```java
requires jdk.httpserver;
```

### Complete Example

```java
module com.ecosimulator {
    // ... other requires statements ...
    
    // Google API Client for Gmail OAuth (automatic modules)
    requires com.google.api.client;
    requires google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.services.gmail;
    
    // Required for LocalServerReceiver in OAuth flow
    // This JDK module provides the HTTP server implementation used by Google OAuth client
    requires jdk.httpserver;
    
    // ... rest of module declaration ...
}
```

## Technical Details

### LocalServerReceiver Implementation

The `LocalServerReceiver` class from Google OAuth client:
1. Creates a local HTTP server on a specified port (default: 8888)
2. Opens the user's browser to the authorization URL
3. Waits for the OAuth callback on the local server
4. Captures the authorization code from the callback
5. Returns the code to complete the OAuth flow

### Dependencies Chain

```
Your Application
  └─> google-oauth-client-jetty
      └─> LocalServerReceiver
          └─> com.sun.net.httpserver.HttpServer
              └─> com.sun.net.httpserver.HttpHandler (from jdk.httpserver module)
```

## Alternative Solutions

### 1. Using Desktop Browser Flow (Current Solution)
✅ **Recommended**: Add `requires jdk.httpserver;`
- Simple and direct
- No code changes needed
- Standard OAuth2 flow

### 2. Manual Authorization Code Flow
Without `LocalServerReceiver`:
```java
// 1. Generate authorization URL
GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
    .setAccessType("offline")
    .build();

String authUrl = flow.newAuthorizationUrl()
    .setRedirectUri("urn:ietf:wg:oauth:2.0:oob")
    .build();

// 2. User manually visits URL and copies code
System.out.println("Visit: " + authUrl);
System.out.println("Enter authorization code:");
String code = new BufferedReader(new InputStreamReader(System.in)).readLine();

// 3. Exchange code for tokens
TokenResponse response = flow.newTokenRequest(code)
    .setRedirectUri("urn:ietf:wg:oauth:2.0:oob")
    .execute();
```

⚠️ **Drawbacks**:
- Requires manual user intervention
- Poor user experience
- More error-prone

### 3. Web-Based OAuth Flow
Implement a full web server:
```java
// Use embedded web server (e.g., Jetty, Undertow)
// Requires additional dependencies and configuration
```

⚠️ **Drawbacks**:
- More complex implementation
- Additional dependencies
- Overkill for desktop applications

## Verification

### Testing the Fix

1. **Build the application**:
   ```bash
   mvn clean compile
   ```

2. **Run with module system**:
   ```bash
   mvn javafx:run
   ```

3. **Test OAuth flow**:
   - Click on email configuration
   - Select "Use Gmail OAuth2"
   - Click "Test Connection"
   - Browser should open for authorization
   - After granting permission, callback should work without errors

### Expected Behavior

✅ **Before Fix**: `NoClassDefFoundError` when attempting OAuth flow  
✅ **After Fix**: OAuth flow completes successfully with browser redirect

## Google OAuth Policies Compatibility

### Current Status (Post-2022)

The solution is compatible with Google's current OAuth policies:

1. **OAuth 2.0 Desktop Apps**: ✅ Supported
   - Uses authorization code flow
   - Local redirect URI (http://localhost:8888)
   - Meets Google's security requirements

2. **Consent Screen**: ✅ Required
   - Must configure OAuth consent screen in Google Cloud Console
   - Desktop apps can use "External" user type
   - Test users can be added during development

3. **Refresh Tokens**: ✅ Available
   - Using `setAccessType("offline")`
   - Tokens stored locally in `tokens/` directory
   - Automatic token refresh on expiry

### Google Cloud Console Setup

1. Create project in Google Cloud Console
2. Enable Gmail API
3. Configure OAuth consent screen
4. Create OAuth 2.0 Client ID (Desktop app type)
5. Download credentials.json
6. Place in `src/main/resources/oauth/` or configure path

## Best Practices for JPMS + Third-Party Libraries

### 1. Identify Platform Module Dependencies

Use `jdeps` to analyze dependencies:
```bash
jdeps --module-path target/classes \
      --add-modules ALL-MODULE-PATH \
      --list-deps com.ecosimulator
```

### 2. Check for JDK Platform Modules

Common JDK modules that may need explicit requires:
- `jdk.httpserver` - HTTP server
- `jdk.crypto.ec` - Elliptic curve cryptography
- `jdk.unsupported` - sun.misc.Unsafe (avoid if possible)
- `jdk.security.auth` - Additional authentication
- `java.sql` - JDBC (already modularized in Java 9+)

### 3. Monitor Runtime Errors

Look for these patterns:
- `NoClassDefFoundError` for `com.sun.*` classes → JDK platform module
- `ClassNotFoundException` → Missing dependency or module not required
- `IllegalAccessError` → Needs `opens` or `exports` declaration

### 4. Test Early in Modular Mode

Always test with:
```bash
--module-path (not --class-path)
--module com.ecosimulator/com.ecosimulator.Main
```

### 5. Document Module Requirements

Add comments in `module-info.java`:
```java
// Required for OAuth2 LocalServerReceiver HTTP server
requires jdk.httpserver;
```

## Troubleshooting

### Issue: Still Getting NoClassDefFoundError

**Check**:
1. Is `requires jdk.httpserver;` in `module-info.java`?
2. Is the application running in module mode (not classpath)?
3. Is Maven compiler plugin configured for modules?

**Solution**:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>17</source>
        <target>17</target>
    </configuration>
</plugin>
```

### Issue: OAuth Flow Opens Browser but Callback Fails

**Check**:
1. Port 8888 is not blocked by firewall
2. No other application using port 8888
3. Credentials.json is correctly configured

**Solution**:
```java
LocalServerReceiver receiver = new LocalServerReceiver.Builder()
    .setPort(8888)  // Try different port if needed
    .build();
```

### Issue: "This app isn't verified" Warning

**Expected**:
- Normal for development/testing
- Click "Advanced" → "Go to [App Name] (unsafe)"

**For Production**:
- Complete OAuth app verification in Google Cloud Console
- Publish to appropriate user base

## References

- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Gmail API Documentation](https://developers.google.com/gmail/api)
- [Java Platform Module System (JPMS)](https://openjdk.org/projects/jigsaw/)
- [JDK HttpServer Module](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.httpserver/module-summary.html)
- [Google OAuth Client Library](https://github.com/googleapis/google-oauth-java-client)

## Summary

The OAuth2 `NoClassDefFoundError` in modular Java applications is resolved by:
1. Adding `requires jdk.httpserver;` to `module-info.java`
2. Understanding JPMS explicit dependency model
3. Following Google OAuth best practices for desktop applications

This solution:
- ✅ Works with Java 17+ JPMS
- ✅ Compatible with current Google OAuth policies
- ✅ Provides good user experience with browser-based flow
- ✅ Requires minimal code changes
- ✅ Maintains security best practices
