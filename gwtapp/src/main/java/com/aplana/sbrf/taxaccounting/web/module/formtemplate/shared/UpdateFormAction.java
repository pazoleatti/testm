package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

/**
 * @author Vitalii Samolovskikh
 */
public class UpdateFormAction extends UnsecuredActionImpl<UpdateFormResult> {
    private FormTemplate form;
    private Date versionEndDate;

    public FormTemplate getForm() {
        return form;
    }

    public void setForm(FormTemplate form) {
        this.form = form;
    }

    public Date getVersionEndDate() {
        return versionEndDate;
    }

    public void setVersionEndDate(Date versionEndDate) {
        this.versionEndDate = versionEndDate;
    }
}
