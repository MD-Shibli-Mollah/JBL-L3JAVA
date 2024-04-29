package com.temenos.t24;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebjblapiauthtable.EbJblApiAuthTableRecord;

/**
 * TODO: check decryption for JWT Auth API
 *
 * @author MD Shibli Mollah
 *
 */
public class GbJblICusTestBase64 extends RecordLifecycle {

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {

        DataAccess da = new DataAccess(this);
        CustomerRecord recordForCustomer = new CustomerRecord(currentRecord);
        
        String id = "";
        id = "AML";
        EbJblApiAuthTableRecord apiAuthRec = new EbJblApiAuthTableRecord(da.getRecord("EB.JBL.API.AUTH.TABLE", id));

        String basicAuth = "";
        try {
            basicAuth = apiAuthRec.getBasicAuth().getValue();
        } catch (Exception e1) {
        }

        // Decryption key (must be 16 characters for AES encryption)
        String decryptionKey = "MyKey$forJBLApi&";

        // Decrypt
        String encryptedBase64Credentials = basicAuth;
        String decryptedBase64 = decrypt(encryptedBase64Credentials, decryptionKey);
        // Tracer
        try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/DECRYPT-" + currentRecordId + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println("MyAPI- encryptedBase64Credentials: " + encryptedBase64Credentials + "\n" + "decryptedBase64: "
                    + decryptedBase64);
        } catch (IOException e) {
        }
        // Tracer END

        // API Call
        String POST_URL_TP = "";
        String POST_PARAMS_TP = "";

        POST_URL_TP = "http://localhost:9089/irf-auth-token-generation-container-21.0.59/api/v1.0.0/generateauthtoken";
        
        POST_PARAMS_TP = "";

        StringBuilder jwtResponse = this.makeRestCall(POST_URL_TP, POST_PARAMS_TP);

        JSONObject jsonSms = null;

        try {
            jsonSms = new JSONObject(jwtResponse.toString());
        } catch (JSONException e) {
        }

        try {
            if (jsonSms.getJSONObject("header").get("status").toString().equals("success")) {
                
             // Tracer
                try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/DECRYPT-" + currentRecordId + ".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw)) {
                    out.println("MyAPI- encryptedBase64Credentials: " + encryptedBase64Credentials + "\n" + "decryptedBase64: "
                            + decryptedBase64);
                } catch (IOException e) {
                }
                // Tracer END
                
                recordForCustomer.setAmlCheck("SENT");
            }
        } catch (JSONException e) {
        }        
        
        return recordForCustomer.getValidationResponse();
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
    
    // Method for API Call (Generate JWT Token)
    public StringBuilder makeRestCall(String POST_URL, String POST_PARAMS) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = null;
        try {
            URL url = new URL(POST_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Basic SU5QVVRUOjEyMzQ1Ng==");
            con.setDoOutput(true);
            try {
                OutputStream os = con.getOutputStream();
                byte[] input = POST_PARAMS.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                System.out.println(con);
                System.out.println("Waiting for REST call response");
                try {
                    Thread.sleep(3000);
                    if (!(con.getResponseCode() == HttpURLConnection.HTTP_OK)) {
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Rest Call Falied");
                }
            } catch (IOException e) {
                System.out.println("Connection establish failed");
                System.exit(0);
            }

            try {
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.out.println("Rest call encountered an error");
            }
            con.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}




