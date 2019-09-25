package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;

import java.util.List;

/**
 * Доступ к таблице TRANSPORT_MESSAGE.
 */
public interface TransportMessageDao {

    /**
     * Получить Транспортное сообщение по идентификатору.
     *
     * @param id идентификатор сообщения.
     * @return объект из базы / null, если отсутствует
     */
    TransportMessage findById(Long id);

    /**
     * Получить тело Транспортного сообщения по идентификатору.
     *
     * @param id идентификатор сообщения.
     * @return строковое представление тела сообщения / null, если по такому id отсутствует сообщение либо его тело.
     */
    String findMessageBodyById(Long id);

    /**
     * Подсчет количества Транспортных сообщений по номеру формы и направлению движения сообщения
     *
     * @param declarationId идентефикатор формы
     * @param type          Направление движения сообщения (0 - исходящее, 1 - входящее)
     * @return Транспортное сообщение, подходящее под условия
     */
    Integer countByDeclarationIdAndType(Long declarationId, int type);

    /**
     * Получить список сообщений с фильтрацией и пагинацией.
     *
     * @param filter       параметры фильтрации
     * @param pagingParams параметры пагинации
     * @return список Транспортных сообщений, подходящих под параметры фильтрации с постраничным разбиением.
     */
    PagingResult<TransportMessage> findByFilter(TransportMessageFilter filter, PagingParams pagingParams);

    /**
     * Создает Транспортное сообщение
     *
     * @param transportMessage Транспортное сообщение
     */
    void create(TransportMessage transportMessage);

    /**
     * Обновляет Транспортное сообщение за исключением следующих полей:
     * - дата и время сообщения;
     * - тип сообщения;
     * - тело сообщения.
     *
     * @param transportMessage Транспортное сообщение
     */
    void update(TransportMessage transportMessage);



    /**
     * Получить список идентификаторов сообщений по фильтру.
     *
     * @param filter       параметры фильтрации*
     * @return список Идентификаторов транспортных сообщений, подходящих под параметры фильтрации.
     */
    List<Long> findIdsByFilter(TransportMessageFilter filter);

    /**
     * Получить список сообщений согласно списку (если список пустой, то возвращает все сообщения)
     *
     * @param ids Список идентификаторов сообщений (может быть пустым null или size=0)
     * @return список Транспортных сообщений, согласно указоному списку идентификаторов
     */
    List<TransportMessage> findByIds(List<Long> ids);
}
