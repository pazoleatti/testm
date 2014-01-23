package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;

import java.io.Serializable;
import java.util.Date;

/**
 * User: avanteev
 */
public class DeclarationTemplateExt implements Serializable {
    private DeclarationTemplate declarationTemplate;
    private Date endDate;

    public DeclarationTemplate getDeclarationTemplate() {
        return declarationTemplate;
    }

    public void setDeclarationTemplate(DeclarationTemplate declarationTemplate) {
        this.declarationTemplate = declarationTemplate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
