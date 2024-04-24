package com.temenos.t24;

import java.io.BufferedReader;

/*AREA      : http://192.168.1.9:9080/BrowserWeb
EB.API      : JblCusAmlConsumeAPIService, JblCusAmlConsumeAPIService.SELECT
PGM.FILE    : JblCusAmlConsumeAPIService
BATCH       : BNK/CUS.AML.SERVICE
TSA.SERVICE : BNK/CUS.AML.SERVICE
VERSION     : CUSTOMER,AML
LOCAL TABLE : LT.CUS.AML
DEVELOPED BY: MD FARID HOSSAIN*/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.complex.eb.servicehook.SynchronousTransactionData;
import com.temenos.t24.api.complex.eb.servicehook.TransactionControl;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;

public class JblCusAmlConsumeAPIService extends ServiceLifecycle {
    @Override
    public List<String> getIds(ServiceData serviceData, List<String> controlList) {

        DataAccess da = new DataAccess(this);
        List<String> recordIDs = da.selectRecords("", "CUSTOMER", "$NAU", "WITH LT.CUS.AML EQ ''");
        int cnt = recordIDs.size();
        System.out.println(cnt + " Are Selected");

        return recordIDs;
    }

    @Override
    public void updateRecord(String id, ServiceData serviceData, String controlItem,
            TransactionControl transactionControl, List<SynchronousTransactionData> transactionData,
            List<TStructure> records) {

        DataAccess da = new DataAccess(this);
        CustomerRecord cusRec = new CustomerRecord(da.getRecord("", "CUSTOMER", "$NAU", id));

        // COSUMING THE API FROM AML & CREATE LOGIC BASED ON THE RESPONSE
        String POST_URL_TP = "";
        String POST_PARAMS_TP = "";

        POST_URL_TP = "";
        /*
         * POST_PARAMS_TP = "{\n" +
         * "  \"body\":                               {\n" +
         * "            \"messageContent\": " + '"' + smsContent + '"' + ",\n" +
         * "            \"smsNumber\": " + '"' + phone + '"' + " \n" +
         * "            \"phoneNumber\": " + '"' + email + '"' + " \n" +
         * "                                                        }\n" + "}";
         */
        System.out.println("Calling AML Api");
        StringBuilder amlResponse = this.makeRestCall(POST_URL_TP, POST_PARAMS_TP);

        // UPDATE HERE
        JSONObject jsonAml = null;

        try {
            jsonAml = new JSONObject(amlResponse.toString());
        } catch (JSONException e) {
        }

        try {
            if (jsonAml.getJSONObject("header").get("status").toString().equals("success")) {
                cusRec.getLocalRefField("LT.CUS.AML").setValue("SENT");

                try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/CustomerRecord-" + id + ".txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw)) {
                    out.println("Customer Record- " + id + "\n" + cusRec);
                } catch (IOException e) {
                }

                SynchronousTransactionData txnData = new SynchronousTransactionData();
                txnData.setFunction("INPUTT");
                txnData.setNumberOfAuthoriser("1");
                txnData.setSourceId("BULK.OFS");
                txnData.setTransactionId(id);
                txnData.setVersionId("CUSTOMER,AML");

                transactionData.add(txnData);
                records.add(cusRec.toStructure());
            } else {
            }

        } catch (JSONException e) {
        }
    }

    // END OF MAIN METHOD
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
