## 🛠 Prerequisites

Before running the application, ensure you meet the following requirements:

### 1. Java Runtime 🚀

- **JDK 21** or higher is required.

### 2. Network & VPN 🛡️

- Since the target resource is restricted in many regions, a **System-Wide VPN** is mandatory.

### 3. Valid Browser Session (Cookies) 🍪

- You should extract manually valid Cookies from a real browser session and setup `ParserConfig.EUA_COOKIE` constant

## 🔐 SSL Certificate Setup 

### How to fix it
You should manually import the site certificate into your local Java Truststore using the `keytool` utility.

1.  **Export the Certificate:** from `https://leonbets.com` 
2.  **Import via Terminal:** Run the following command as **Administrator**:

```powershell
# Navigate to your JDK bin folder (example path)
cd "C:\Program Files\Java\jdk-21\bin"

# Run keytool to import the certificate
.\keytool -importcert -alias leonbets -keystore "..\lib\security\cacerts" -file "C:\Path\To\Your\Downloaded\leonbets.cer"
```