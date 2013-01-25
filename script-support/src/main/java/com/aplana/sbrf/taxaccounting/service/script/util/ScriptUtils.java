package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;

import java.math.BigDecimal;

/**
 * Библиотека функций для вызова из скриптов
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 22.01.13 16:34
 */

public class ScriptUtils {
	/**
	 * TODO: Добавить комментарий и юнит-тест
	 * @param formData
	 * @param columnAlias
	 * @param rowAliasPrefix
	 * @return
	 */
	public static double sumByColumnAndRowAliasPrefix(FormData formData, String columnAlias, String rowAliasPrefix) {
		double sum = 0;
		for (DataRow row: formData.getDataRows()) {
			if (row.getAlias() != null && row.getAlias().startsWith(rowAliasPrefix)) {
				BigDecimal val = (BigDecimal)row.get(columnAlias);
				if (val != null) {
					sum += val.doubleValue();
				}
			}
		}
		return sum;
	}

}
