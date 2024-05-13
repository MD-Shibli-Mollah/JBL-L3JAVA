package com.temenos.t24;

import java.util.ArrayList;
import java.util.List;

import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.category.CategoryRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;

/*AREA             : 
EB.API             : GsimsNofileEnqRtn
STANDARD.SELECTION : NOFILE.GsimsNofileEnqRtn
ENQUIRY            : AC.API.ACCOUNTS.GSIMS.1.1.0
DEVELOPED BY       : MD Shibli Mollah*/

public class GsimsNofileEnqRtn extends Enquiry {

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {

        List<String> returnIds = new ArrayList<String>();

        String responseCode = "";
        String accountType = "";
        String accountTitle = "";
        String email = "";
        String bdMobile = "";
        String abroadMobile = "";
        // String idType2 = "";
        String cusLegalIdNameAll = "";
        String cusLegalIdName = "";
        // String documentId2 = "";
        String cusLegalIdNo = "";
        String accountNo = "";
        String idType = "";
        String documentId = "";
        String category = "";
        String cusId = "";

        try {
            accountNo = filterCriteria.get(0).getValue();
            idType = filterCriteria.get(1).getValue();
            documentId = filterCriteria.get(2).getValue();
        } catch (Exception e) {
        }

        DataAccess daAcc = new DataAccess(this);
        DataAccess daCat = new DataAccess(this);
        DataAccess daCus = new DataAccess(this);
        AccountRecord accRec = null;

        try {
            accRec = new AccountRecord(daAcc.getRecord("ACCOUNT", accountNo));
            // idType2 = accRec.getLocalRefField("LT.AC.ID.TYPE").getValue();
            // documentId2 = accRec.getLocalRefField("LT.AC.ID.NO").getValue();
            category = accRec.getCategory().getValue();
            cusId = accRec.getCustomer().getValue();
            accountTitle = accRec.getAccountTitle1(0).getValue();
        } catch (Exception e) {
        }

        if (cusId != "") {
            CategoryRecord catRec = new CategoryRecord(daCat.getRecord("CATEGORY", category));
            CustomerRecord cusRec = new CustomerRecord(daCus.getRecord("CUSTOMER", cusId));
            cusLegalIdNameAll = cusRec.getLegalIdDocName(0).getValue(); // 1946718499-NATIONAL.ID
            // Find the index of the hyphen
            int hyphenIndex = cusLegalIdNameAll.indexOf("-");
            // Extract the substring after the hyphen
            cusLegalIdName = cusLegalIdNameAll.substring(hyphenIndex + 1);
            
            cusLegalIdNo = cusRec.getLegalId(0).getLegalId().getValue();

            if (idType.equals(cusLegalIdName) && documentId.equals(cusLegalIdNo)) {
                responseCode = "Success";
                accountType = catRec.getDescription(0).getValue();

                email = cusRec.getPhone1(0).getEmail1().getValue();
                bdMobile = cusRec.getPhone1(0).getPhone1().getValue();
                abroadMobile = cusRec.getPhone1(0).getSms1().getValue();

            } else {
                responseCode = "7"; // If id doc mismatch, response code is 7
            }
        } else {
            responseCode = "6"; // If account is not found, response code is 6
        }

        returnIds.add(responseCode + "*" + accountType + "*" + accountNo + "*" + accountTitle + "*" + email + "*"
                + bdMobile + "*" + abroadMobile);

        return returnIds;
    }
}
