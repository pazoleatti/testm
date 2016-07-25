package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFormAttributesResult implements Result {
    List<RefBookAttribute> attributes;
    List<Column> tableColumns;

    public List<RefBookAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<RefBookAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<Column> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<Column> tableColumns) {
        this.tableColumns = tableColumns;
    }
}
