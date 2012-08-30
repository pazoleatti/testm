package com.aplana.sbrf.taxaccounting.script;

import java.util.List;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;

public interface FormDataScriptingService {
	/**
	 * Возвращает список скриптов, которые нужно выполнять над строками налоговой формы
	 * @param formId идентификатор описания налоговой формы
	 * @return список скриптов, порядок записей соответствует порядку, в котором они должны исполняться
	 */
	List<RowScript> getFormRowScripts(int formId);
	
	/**
	 * Выполнить обработку формы в соответстии со скриптами
	 * @param logger объект для логирования информации и ходе обработки
	 * @param formData данные по налоговой форме для обработки
	 */	
	void processFormData(Logger logger, FormData formData);
}
