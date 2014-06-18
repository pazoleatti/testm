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

    public AutoNumerationColumn(String name, Integer type) {
        this.typeName = name;
        this.type = type;
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
