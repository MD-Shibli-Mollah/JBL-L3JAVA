package com.temenos.t24;

import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.party.Limit;
import com.temenos.t24.api.system.DataAccess;
/**
 * TODO: Document me!
 *
 * @author nazihar
 *
 */
public class myTest extends Enquiry {

    
     @Override
    public void setRecord(String value, String currentId, TStructure currentRecord, List<FilterCriteria> filterCriteria,
            EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
         
         DataAccess da = new DataAccess();
         
         Limit li = new Limit(this);
         

        super.setRecord(value, currentId, currentRecord, filterCriteria, enquiryContext);
    }

    
    
       
}
