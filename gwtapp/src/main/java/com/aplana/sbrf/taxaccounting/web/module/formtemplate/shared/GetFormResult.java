package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public class GetFormResult implements Result {
    private FormTemplateExt form;
    private List<RefBook> refBookList;

    public FormTemplateExt getForm() {
        return form;
    }

    public void setForm(FormTemplateExt form) {
        this.form = form;
    }

    public List<RefBook> getRefBookList() {
        return refBookList;
    }

    public void setRefBookList(List<RefBook> refBookList) {
        this.refBookList = refBookList;
    }
}
