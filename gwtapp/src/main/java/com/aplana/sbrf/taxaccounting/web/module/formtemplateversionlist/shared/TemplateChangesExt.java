package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;

import java.io.Serializable;

/**
 * User: avanteev
 */
public class TemplateChangesExt implements Serializable {
    private TemplateChanges templateChanges;
    private int edition;

    public TemplateChanges getTemplateChanges() {
        return templateChanges;
    }

    public void setTemplateChanges(TemplateChanges templateChanges) {
        this.templateChanges = templateChanges;
    }

    public int getEdition() {
        return edition;
    }

    public void setEdition(int edition) {
        this.edition = edition;
    }
}
