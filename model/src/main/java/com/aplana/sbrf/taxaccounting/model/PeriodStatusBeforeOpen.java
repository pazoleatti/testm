package com.aplana.sbrf.taxaccounting.model;

/**
 * Статус периода
 */

public enum PeriodStatusBeforeOpen {
	OPEN, // Открыт
	CLOSE, // Закрыт
	NOT_EXIST, // Не существует
	BALANCE_STATUS_CHANGED // При создании был изменен статус ввода остатка
}
