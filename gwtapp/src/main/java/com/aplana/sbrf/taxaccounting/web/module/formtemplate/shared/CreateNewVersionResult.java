package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class CreateNewVersionResult implements Result {
    private int formTemplateId;
    private String uuid;
    private List<Column> columns;

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public int getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(int formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
