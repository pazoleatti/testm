package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetTableDataAction extends UnsecuredActionImpl<GetTableDataResult> {

    public GetTableDataAction() {
    }

    private Long depoId;
    private char taxType;
    private boolean isForm;


    public char getTaxType() {
        return taxType;
    }

    public void setTaxType(char taxType) {
        this.taxType = taxType;
    }

    public Long getDepoId() {
        return depoId;
    }

    public void setDepoId(Long depoId) {
        this.depoId = depoId;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean form) {
        isForm = form;
    }
}
