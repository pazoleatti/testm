package com.aplana.sbrf.taxaccounting.model;

/**
 * Автонумеруемая графа
 *
 * @author Fail Mukhametdinov
 */
public class AutoNumerationColumn extends Column {

    private int type; //TODO: (2014-09-23 MFayzullin) избавиться от дубликатов полей
    private String typeName; //TODO: (2014-09-23 MFayzullin) избавиться от дубликатов полей
	private AutoNumerationColumnType numerationType;

    public AutoNumerationColumn() {
		setColumnType(ColumnType.AUTO);
    }

    public AutoNumerationColumn(AutoNumerationColumnType numerationType) {
		this();
        this.numerationType = numerationType;
		setType(numerationType.getType());
        setTypeName(numerationType.getName());
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
