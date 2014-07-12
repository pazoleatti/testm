package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;

import java.io.Serializable;

/**
 * User: avanteev
 * Ксласс представляющий историю изменений шаблонов на визуальной форме.
 */
public class TemplateChangesExt implements Serializable {
    private TemplateChanges templateChanges;

    public TemplateChanges getTemplateChanges() {
        return templateChanges;
    }

    public void setTemplateChanges(TemplateChanges templateChanges) {
        this.templateChanges = templateChanges;
    }
}
