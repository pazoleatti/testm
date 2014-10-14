package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;

import java.io.Serializable;
import java.util.Map;

public class TableRow implements Serializable{
    Integer rowNum;
    Map<String, TableCell> row;

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public Map<String, TableCell> getRow() {
        return row;
    }

    public void setRow(Map<String, TableCell> row) {
        this.row = row;
    }

}
