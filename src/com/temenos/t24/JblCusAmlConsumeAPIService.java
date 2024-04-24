package com.temenos.t24;

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
import java.io.PrintWriter;
import java.util.List;

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
        cusRec.getLocalRefField("LT.CUS.AML").setValue("SENT");
        
        try (FileWriter fw = new FileWriter("/Temenos/T24/UD/Tracer/CustomerRecord-" + id + ".txt",
                true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
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
    }
}
