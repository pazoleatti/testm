package com.aplana.sbrf.taxaccounting.dao.script;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.math.BigDecimal;

@ScriptExposed(taxTypes = TaxType.TRANSPORT)
public interface TransportTaxDao {

	/**
	 * Возвращает значение ставки транспортного налога
	 *
	 * @param code  код типа транспортного средства
	 * @param age   срок использования в годах
	 * @param power мощность в л.с.
	 * @param regionId Код региона субъекта РФ
	 * 
	 * @return значение ставки налога в рублях
	 */
	int getTaxRate(String code, BigDecimal age, BigDecimal power, String regionId);

	/**
	 * Возвращает название вида транспортного средства
	 *
	 * @return название вида транспортного средства или null, если передан несуществующий код
	 */
	String getTsTypeName(String tsTypeCode);

	/**
	 * Проверяет существование кода транспортного средства
	 *
	 * @param code код типа транспортного средства
	 * @return true - если код существует, false - если не существует
	 */
	boolean validateTransportTypeCode(String code);

	/**
	 * Проверяет существование кода единицы измерения
	 *
	 * @param code код
	 * @return true - если код существует, false - если не существует
	 */
	boolean validateTaxBaseUnit(BigDecimal code);

	/**
	 * Проверяет существование кода экологического класса
	 *
	 * @param code код экологического класса
	 * @return true - если код существует, false - если нет
	 */
	boolean validateEcoClass(BigDecimal code);
}
