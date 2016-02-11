package com.aplana.sbrf.taxaccounting.model;

/**
 * Автонумеруемая графа
 *
 * @author Fail Mukhametdinov
 */
public class AutoNumerationColumn extends Column {

	private NumerationType numerationType;

    public AutoNumerationColumn() {
		columnType = ColumnType.AUTO;
        setNumerationType(NumerationType.SERIAL);
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
