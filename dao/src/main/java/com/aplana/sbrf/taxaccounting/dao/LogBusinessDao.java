package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.dto.LogBusinessDTO;

import java.util.Date;
import java.util.List;

/**
 * DAO-Интерфейс для работы с историей событий деклараций
 */
public interface LogBusinessDao {

    /**
     * Возвращяет список записей историй событий для ФЛ
     *
     * @param declarationId идентификатор формы
     * @param pagingParams  данные для сортировки
     * @return страница историй событий
     */
    List<LogBusinessDTO> findAllByDeclarationId(long declarationId, PagingParams pagingParams);

    /**
     * Возвращяет страницу записей историй событий для ФЛ
     *
     * @param personId     идентификатор ФЛ
     * @param pagingParams данные для сортировки и пагинации
     * @return страница историй событий
     */
    PagingResult<LogBusinessDTO> findAllByPersonId(long personId, PagingParams pagingParams);

    /**
     * Получить дату создания налоговой формы
     *
     * @param formId идентификатор пользователя
     * @return объект, представляющий дату принятия нф
     */
    Date getFormCreationDate(long formId);

    /**
     * Получить имя пользователя, загрузившего ТФ
     *
     * @param declarationDataId код декларации
     */
    String getFormCreationUserName(long declarationDataId);

    /**
     * Создаёт запись истории событий
     *
     * @param logBusiness запись истории событий
     */
    void create(LogBusiness logBusiness);
}
