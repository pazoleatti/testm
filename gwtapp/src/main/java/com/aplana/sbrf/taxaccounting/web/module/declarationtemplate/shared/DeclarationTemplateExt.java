package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateCheck;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * User: avanteev
 */
public class DeclarationTemplateExt implements Serializable {
    private DeclarationTemplate declarationTemplate;
    private Date endDate;
    private List<DeclarationTemplateCheck> checks;

    public List<DeclarationTemplateCheck> getChecks() {
        return checks;
    }

    public void setChecks(List<DeclarationTemplateCheck> checks) {
        this.checks = checks;
    }

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
