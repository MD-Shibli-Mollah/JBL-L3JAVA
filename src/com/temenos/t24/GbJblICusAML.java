package com.temenos.t24;

import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
// import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author nazihar
 *
 */
public class GbJblICusAML extends RecordLifecycle {

    @Override
    public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
       // DataAccess da = new DataAccess(this);
        CustomerRecord recordForCustomer = new CustomerRecord(currentRecord);
        
        recordForCustomer.setAmlCheck("SENT");
        currentRecord.set(recordForCustomer.toStructure());
    }

}
