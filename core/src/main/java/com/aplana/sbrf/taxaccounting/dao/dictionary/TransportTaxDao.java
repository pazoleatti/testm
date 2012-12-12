package com.aplana.sbrf.taxaccounting.dao.dictionary;

import java.math.BigDecimal;

import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

public interface TransportTaxDao extends ScriptExposed {
	/**
	 * Возращает имя муниципального отделения по коду ОКАТО
	 *
	 * @param okato код ОКАТО
	 * @return имя региона, или null, если такого ОКАТО нет в справочнике
	 */
	String getRegionName(String okato);

	/**
	 * Возвращает значение ставки транспортного налога
	 *
	 * @param code  код типа транспортного средства
	 * @param age   срок использования в годах
	 * @param power мощность в л.с.
	 * @return значение ставки налога в рублях
	 */
	int getTaxRate(String code, BigDecimal age, BigDecimal power);

	/**
	 * Возвращает название вида транспортного средства
	 *
	 * @return название вида транспортного средства или null, если передан несуществующий код
	 */
	String getTsTypeName(String tsTypeCode);

	/**
	 * Проверяет существование кода ОКАТО
	 *
	 * @param okato код ОКАТО
	 * @return true - если код ОКАТО существует, false - если не существует
	 */
	public boolean validateOkato(String okato);

	/**
	 * Проверяет существование кода транспортного средства
	 *
	 * @param code код типа транспортного средства
	 * @return true - если код существует, false - если не существует
	 */
	public boolean validateTransportTypeCode(String code);

	/**
	 * Проверяет существование кода единицы измерения
	 *
	 * @param code код
	 * @return true - если код существует, false - если не существует
	 */
	public boolean validateTaxBaseUnit(BigDecimal code);

	/**
	 * Проверяет существование кода экологического класса
	 *
	 * @param code код экологического класса
	 * @return true - если код существует, false - если нет
	 */
	boolean validateEcoClass(BigDecimal code);
}
