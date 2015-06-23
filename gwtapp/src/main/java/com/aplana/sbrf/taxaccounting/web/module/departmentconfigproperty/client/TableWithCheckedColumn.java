package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client;

import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;

public interface TableWithCheckedColumn {

    public CheckBoxHeader getCheckBoxHeader();

    public boolean isNeedCheckedRow();

    public void setNeedCheckedRow(boolean needCheckedRow);
}
