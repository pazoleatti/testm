package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;

/**
 * Сервис для работы с Транспортными сообщениями, предназначенными для обмена между подсистемами АС Учет Налогов.
 */
public interface TransportMessageService {

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
}
