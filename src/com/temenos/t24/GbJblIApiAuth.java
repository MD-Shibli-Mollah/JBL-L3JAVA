package com.temenos.t24;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.tables.ebjblapiauthtable.EbJblApiAuthTableRecord;

/**
 * TODO: Encryption for BASIC Auth
 *
 * @author MD Shibli Mollah
 *
 */
public class GbJblIApiAuth extends RecordLifecycle {

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {

        EbJblApiAuthTableRecord apiAuthRec = new EbJblApiAuthTableRecord(currentRecord);

        String username = "";
        String password = "";
        String basicAuth = "";
        try {
            username = apiAuthRec.getUsername().getValue();
            password = apiAuthRec.getPassword().getValue();
        } catch (Exception e1) {
        }

        if (username == "" || password == "") {
            return apiAuthRec.getValidationResponse();
        }

        // Encode username and password in Base64
        String authString = username + ":" + password;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());

        // Encryption key (must be 16 characters for AES encryption)
        // String encryptionKey = "ThisIsASecretKey!";
        String encryptionKey = "MyKey$forJBLApi&";

        // Encrypt username and password
        try {
            // basicAuth = this.encryptCredentials(username + ":" + password,
            // encryptionKey);
            basicAuth = this.encryptCredentials(encodedAuthString, encryptionKey);
            // Tracer
            try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/ENCRYPT-" + currentRecordId + ".txt", true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw)) {
                out.println("MyAPI- encodedAuthString: " + encodedAuthString + "\n" + "Basic Auth: " + basicAuth);
            } catch (IOException e) {
            }
            // Tracer end
        } catch (Exception e) {
        }

        apiAuthRec.setBasicAuth(basicAuth);
        apiAuthRec.setUsername("");
        apiAuthRec.setPassword("");

        // Decrypt
        String encryptedBase64Credentials = basicAuth;
        String decryptedBase64 = decrypt(encryptedBase64Credentials, encryptionKey);
        apiAuthRec.setJwtToken(decryptedBase64);

        try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/DECRYPT-" + currentRecordId + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println("MyAPI- encryptedBase64Credentials: " + encryptedBase64Credentials + "\n" + "decryptedBase64: "
                    + decryptedBase64);
        } catch (IOException e) {
        }

        currentRecord.set(apiAuthRec.toStructure());

        return apiAuthRec.getValidationResponse();
    }

    // AES encryption method
    public String encryptCredentials(String credentials, String encryptionKey) throws Exception {
        byte[] key = encryptionKey.getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(credentials.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // AES decryption method
    public static String decrypt(String strToDecrypt, String secret) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(decryptedBytes);
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

}
