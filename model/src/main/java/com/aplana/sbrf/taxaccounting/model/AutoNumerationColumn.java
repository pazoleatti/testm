package com.aplana.sbrf.taxaccounting.model;

/**
 * Автонумеруемая графа
 *
 * @author Fail Mukhametdinov
 */
public class AutoNumerationColumn extends Column {

    private int type;
    private String typeName;

    public AutoNumerationColumn() {
    }

    public AutoNumerationColumn(String numerationName, Integer numerationRow) {
        this.typeName = numerationName;
        this.type = numerationRow;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
