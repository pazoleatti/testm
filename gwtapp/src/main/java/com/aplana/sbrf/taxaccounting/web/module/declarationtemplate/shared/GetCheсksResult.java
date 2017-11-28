package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateCheck;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetCheсksResult implements Result {

    /**
     * Список проверок формы
     */
	private List<DeclarationTemplateCheck> checks;

    public List<DeclarationTemplateCheck> getChecks() {
        return checks;
    }

    public void setChecks(List<DeclarationTemplateCheck> checks) {
        this.checks = checks;
    }
}