package com.dealercrest.http;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * 1. secret is stored in java KeyStore
 * keytool -list -v -keystore secrets.pkcs12
 * keytool -delete -alias <key_name> -keystore secrets.pkcs12
 * keytool -importpass -storetype pkcs12 -alias <key_name> -keystore
 * secrets.pkcs12
 * 2. accept user/password, and generate a token based on the secret
 * 3. validate the token based on the secret
 * 4. rolling JWT refresh. 
 *    If valid, and expiring soon (e.g. ≤ 25 minutes left), issue a new JWT with exp = now + 30m.
 */
public class JWebToken {

    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String encodedHeader = encode(JWT_HEADER.getBytes());
    private final Mac sha256Hmac;
    private static final String EXP = "exp";
    private static final String JTI = "jti";

    private final String ONETIME_PASSWORD;
    private final String STRIPE_SECURITY_KEY;
    private static final Logger logger = Logger.getLogger(JWebToken.class.getName());

    private static volatile JWebToken instance;

    private JWebToken() throws IllegalArgumentException {
        String keyStorePassword = System.getProperty("KEYSTORE_PASSWORD");
        if (keyStorePassword == null || keyStorePassword.isEmpty()) {
            throw new IllegalArgumentException("System property KEYSTORE_PASSWORD is not set");
        }
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(keyStorePassword.toCharArray());
            InputStream fIn = this.getClass().getResourceAsStream("/secrets.pkcs12");
            ks.load(fIn, keyStorePassword.toCharArray());
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");

            ONETIME_PASSWORD = getSecretValue(keyStorePP, ks, factory, "generalpassword");
            STRIPE_SECURITY_KEY = getSecretValue(keyStorePP, ks, factory, "stripe_test_secret_key");

            String secret = getSecretValue(keyStorePP, ks, factory, "secretkey.dev");
            byte[] hash = secret.getBytes(StandardCharsets.UTF_8);
            sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKey);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to init jWeb token", e);
        }
    }

    public String getOnetimePassword() {
        return ONETIME_PASSWORD;
    }

    public String getStripeSecurityKey() {
        return STRIPE_SECURITY_KEY;
    }

    private String getSecretValue(KeyStore.PasswordProtection keyStorePP, KeyStore ks,
            SecretKeyFactory factory, String alias)
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, InvalidKeySpecException {
        KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry) ks.getEntry(alias, keyStorePP);
        PBEKeySpec keySpec = (PBEKeySpec) factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);
        char[] password = keySpec.getPassword();
        return new String(password);
    }

    public Token parse(String token) {
        if (token == null) {
            return new Token(false, new JSONObject().put("error", "Invalid Token format"));
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return new Token(false, new JSONObject().put("error", "Invalid Token format"));
        }
        if (!encodedHeader.equals(parts[0])) {
            return new Token(false, new JSONObject().put("error", "JWT Header is Incorrect"));
        }

        JSONObject payload = new JSONObject(decode(parts[1]));
        if (!payload.has(EXP)) {
            return new Token(false, new JSONObject().put("error", "Payload doesn't contain expiry"));
        }

        Instant expireInstant = Instant.ofEpochSecond(payload.getLong(EXP));
        if (expireInstant.isBefore(Instant.now())) {
            return new Token(false, payload);
        }

        String signature = parts[2];
        String beforeSha = encodedHeader + "." + encode(payload);
        String security = hmacSha256(beforeSha);
        return new Token(signature.equals(security), payload);
    }

    public String generateToken(JSONObject payload, long duration) {
        payload.put(EXP, Instant.now().plusSeconds(duration).getEpochSecond());
        payload.put(JTI, UUID.randomUUID().toString());
        String signature = hmacSha256(encodedHeader + "." + encode(payload));
        return encodedHeader + "." + encode(payload) + "." + signature;
    }

    public static JWebToken getInstance() throws IllegalArgumentException {
        if (instance == null) {
            synchronized (JWebToken.class) {
                if (instance == null) {
                    instance = new JWebToken();
                }
            }
        }
        return instance;
    }

    private static String encode(JSONObject obj) {
        return encode(obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    public String hmacSha256(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        try {
            Mac mac = (Mac) sha256Hmac.clone();
            byte[] signedBytes = mac.doFinal(dataBytes);
            return encode(signedBytes);
        } catch (CloneNotSupportedException e) {
            logger.log(Level.SEVERE, "failed to encode", e);
        }
        return null;
    }

    public String md5Hash(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(key.getBytes(StandardCharsets.UTF_8));
        byte[] bytes = md.digest();

        BigInteger no = new BigInteger(1, bytes);
        return no.toString(16);
    }

    public static class Token {
        private final boolean isValid;
        private final JSONObject payload;

        public Token(boolean isValid, JSONObject payload) {
            this.isValid = isValid;
            this.payload = payload;
        }

        public boolean isValid() {
            return isValid;
        }

        public JSONObject getPayload() {
            return payload;
        }
    }

}
