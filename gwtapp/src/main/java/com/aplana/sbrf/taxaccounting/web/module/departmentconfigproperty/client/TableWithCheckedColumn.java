package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client;

import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;

public interface TableWithCheckedColumn {

    CheckBoxHeader getCheckBoxHeader();

    boolean isNeedCheckedRow();

    void setNeedCheckedRow(boolean needCheckedRow);
}
