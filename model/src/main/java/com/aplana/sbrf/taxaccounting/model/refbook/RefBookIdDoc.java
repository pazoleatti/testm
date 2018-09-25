package com.aplana.sbrf.taxaccounting.model.refbook;

public class RefBookIdDoc extends RefBookVersioned<Long> {

    private RefBookDocType docType;
    private String docNumber;


    public RefBookDocType getDocType() {
        return docType;
    }

    public void setDocType(RefBookDocType docType) {
        this.docType = docType;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }
}
