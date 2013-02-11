package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;

/**
 * Dao для работы с информацией о {@link FormDataSigner подписантах} налоговых форм
 * @author dsultanbekov
 */
public interface FormDataSignerDao {
	/**
	 * Возвращает список подписантов по налоговой форме 
	 * @param formDataId идентификатор налоговой формы
	 * @return список подписантов, порядок соответствует порядку следования в налоговой форме
	 */
	List<FormDataSigner> fillSigners(long formDataId);
	
	/**
	 * Сохранить информацию о подписантах налоговой формы.
	 * При сохранении если у очередной записи {@link FormDataSigner} оказывается пустой {@link FormDataSigner#getId() идентификатор},
	 * создаётся новая запись и объекту присовится новое значение идентификатора.
	 * Если идентификатор уже был непуст, то производится попытка обновить существующую запись в БД.
	 * @param formData карточка налоговой формы
	 */
	void saveSigners(FormData formData);
}
