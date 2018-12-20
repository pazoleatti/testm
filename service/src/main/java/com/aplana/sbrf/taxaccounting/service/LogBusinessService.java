package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.dto.LogBusinessDTO;

import java.util.Date;
import java.util.List;

/**
 * Сервис для работы с историей событий налоговых форм/деклараций
 */
@ScriptExposed
public interface LogBusinessService {

    /**
     * Добавить информацию об логировании
     *
     * @param declarationId идентификатор формы
     * @param event         событие
     * @param note          текст
     * @param userInfo      информация о пользователе, инициирующего событие
     */
    void logFormEvent(Long declarationId, FormDataEvent event, String note, TAUserInfo userInfo);

    /**
     * Добавить информацию об логировании
     *
     * @param personId идентификатор ФЛ
     * @param event    событие
     * @param note     текст
     * @param userInfo информация о пользователе, инициирующего событие
     */
    void logPersonEvent(Long personId, FormDataEvent event, String note, TAUserInfo userInfo);

    /**
     * Возвращяет дату создания формы (по событию {@link FormDataEvent#CREATE})
     *
     * @param declarationDataId идентификатор формы
     * @return дата созданий формы
     */
    Date getFormCreationDate(long declarationDataId);

    /**
     * Получить имя пользователя, загрузившего ТФ
     *
     * @param declarationDataId идентификатор формы
     */
    String getFormCreationUserName(long declarationDataId);

    /**
     * Получить информацию об истории событий налоговой формы
     *
     * @param declarationId идентификатор формы
     * @param pagingParams  данные для сортировки (пагинации нет)
     * @return список  истории событий для налоговой формы
     */
    List<LogBusinessDTO> findAllByDeclarationId(long declarationId, PagingParams pagingParams);

    /**
     * Возвращяет страницу историй событий для ФЛ
     *
     * @param personId     идентификатор ФЛ
     * @param pagingParams данные для сортировки и пагинации
     * @return страница историй событий
     */
    PagingResult<LogBusinessDTO> findAllByPersonId(long personId, PagingParams pagingParams);
}
