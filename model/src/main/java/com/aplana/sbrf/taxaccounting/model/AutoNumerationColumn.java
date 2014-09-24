package com.aplana.sbrf.taxaccounting.model;

/**
 * Автонумеруемая графа
 *
 * @author Fail Mukhametdinov
 */
public class AutoNumerationColumn extends Column {

	private NumerationType numerationType;

    public AutoNumerationColumn() {
		setColumnType(ColumnType.AUTO);
    }

    public AutoNumerationColumn(NumerationType numerationType) {
		this();
        this.numerationType = numerationType;
    }

	public void setNumerationType(NumerationType numerationType) {
		this.numerationType = numerationType;
	}

	public NumerationType getNumerationType() {
		return numerationType;
	}
}
