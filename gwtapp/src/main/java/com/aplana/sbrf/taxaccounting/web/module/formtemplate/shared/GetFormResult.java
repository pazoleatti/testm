package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public class GetFormResult implements Result {
    private FormTemplate form;
    private List<RefBook> refBookList;

    public FormTemplate getForm() {
        return form;
    }

    public void setForm(FormTemplate form) {
        this.form = form;
    }

    public List<RefBook> getRefBookList() {
        return refBookList;
    }

    public void setRefBookList(List<RefBook> refBookList) {
        this.refBookList = refBookList;
    }
}
