package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * @author auldanov.
 */
public class DeleteFormsSourseAction extends UnsecuredActionImpl<DeleteFormsSourceResult> implements ActionName {
    List<FormTypeKind> kind;

    public List<FormTypeKind> getKind() {
        return kind;
    }

    public void setKind(List<FormTypeKind> kind) {
        this.kind = kind;
    }

    @Override
    public String getName() {
        return "Удаление назначенных форм";
    }
}
