package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

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
    PagingResult<TransportMessage> findByFilter(TransportMessageFilter filter, @Nullable PagingParams pagingParams);

    /**
     * Получить первое найденное сообщение, совпадающее с параметрами фильтрации.
     *
     * @param filter       параметры фильтрации
     * @return список Транспортных сообщений, подходящих под параметры фильтрации с постраничным разбиением.
     */
    @Nullable
    TransportMessage findFirstByFilter(TransportMessageFilter filter);

    /**
     * Получить список сообщений с фильтрацией и пагинацией с учетом подразделений, доступных пользователю.
     *
     * @param filter       параметры фильтрации
     * @param pagingParams параметры пагинации
     * @return список Транспортных сообщений, подходящих под параметры фильтрации с постраничным разбиением.
     */
    PagingResult<TransportMessage> findByFilterWithUserDepartments(TransportMessageFilter filter, @Nullable PagingParams pagingParams);

    /**
     * Создает Транспортное сообщение
     *
     * @param transportMessage Транспортное сообщение
     */
    void create(TransportMessage transportMessage);

    /**
     * Обновляет существующее транспортное сообщение, за исключением следующих полей:
     * - дата и время сообщения;
     * - тип сообщения;
     * - тело сообщения.
     *
     * @param transportMessage транспортное сообщение
     */
    void update(TransportMessage transportMessage);

    /**
     * Отправка сообщения в ЖА с данными о транспортном сообщении
     *
     * @param noteFormat Текст сообщения
     * @param transportMessage ТС, для которого отправляется сообщение в ЖА
     */
    void sendAuditMessage(String noteFormat, TransportMessage transportMessage);

    /**
     * Запускает ассинхронную задачу на выгрузку транспортных сообщений в Excel
     *
     *
     * @param headerDescription
     * @param filter   фильтр, по которому выбираются формы
     * @param userInfo пользователь запустивший операцию
     * @return результат запуска задачи
     */
    InputStream export(String headerDescription, TransportMessageFilter filter, TAUserInfo userInfo) throws IOException;

}
