package com.temenos.t24;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.system.DataAccess;
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
        // TODO Auto-generated method stub
        
        DataAccess da = new DataAccess(this);

       // EbJblApiAuthTableTable apiTable = new EbJblApiAuthTableTable(this);
        EbJblApiAuthTableRecord apiAuthRec = new EbJblApiAuthTableRecord(da.getRecord("EB.JBL.API.AUTH.TABLE", currentRecordId));

        String username = "";
        String password = "";
        String basicAuth = "";        
        try {
            username = apiAuthRec.getUsername().getValue();
            password = apiAuthRec.getPassword().getValue();
        } catch (Exception e1) {
        }

        // Encryption key (must be 16 characters for AES encryption)
       // String encryptionKey = "ThisIsASecretKey!";
        String encryptionKey = "MyKey$forJBLApi&!";
        encryptionKey.getBytes(StandardCharsets.UTF_8);
        
        // Encrypt username and password
        try {
            basicAuth = encryptCredentials(username + ":" + password, encryptionKey);
        } catch (Exception e) {
        }
        apiAuthRec.setBasicAuth(basicAuth);
       // return apiAuthRec.getValidationResponse();
         currentRecord.set(apiAuthRec.toStructure());
         
         return apiAuthRec.getValidationResponse();      
    }

 // AES encryption method
    public static String encryptCredentials(String credentials, String encryptionKey) throws Exception {
        byte[] key = encryptionKey.getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(credentials.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }   
}



