package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Столбец, служащий для сохранения данных типа {@link java.util.Date}
 * Дополнительных параметров настройки не содержит
 * @author dsultanbekov
 */
public class DateColumn extends Column implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer formatId = Formats.NONE.getId();

	public DateColumn() {
		columnType = ColumnType.DATE;
	}

	public Integer getFormatId() {
		return formatId;
	}

	public void setFormatId(Integer formatId) {
		this.formatId = formatId;
	}

}
