package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class DeleteFormTypeAction extends UnsecuredActionImpl<DeleteFormTypeResult> {
    private int formTypeId;

    public int getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }
}
