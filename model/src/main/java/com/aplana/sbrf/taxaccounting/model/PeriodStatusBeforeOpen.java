package com.aplana.sbrf.taxaccounting.model;

/**
 * Статус периода
 */

public enum PeriodStatusBeforeOpen {
	OPEN, // Открыт
	CLOSE, // Закрыт
	NOT_EXIST, // Не существует
    CORRECTION_PERIOD_ALREADY_EXIST, // Существуют корректирующие периоды
    INVALID,
    CORRECTION_PERIOD_LAST_OPEN,//есть более поздний открытый корректирующий период
    CORRECTION_PERIOD_NOT_CLOSE,//текущий не закрыт
    CORRECTION_PERIOD_BEFORE_OPEN
}
