package com.aplana.sbrf.taxaccounting.dao.script;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.math.BigDecimal;

@ScriptExposed(taxTypes = TaxType.TRANSPORT)
public interface TransportTaxDao {
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
}
