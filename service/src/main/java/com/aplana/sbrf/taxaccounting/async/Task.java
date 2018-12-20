package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Map;

/**
 * Интерфейс для действий с задачами.
 * Здесь находятся методы которые могут быть общими как для асинхронных, так и для синхронных задач.
 * Этот интерфейс создан как задел на будущее, когда будет готова постановка по работе с синхронными задачами. До указанного момента
 * напрямую этот интерфейс классы не имплементят. И все же если он окажется ненужным от него легко будет избавиться, копипастнув объявленные методы
 * в интерфейс который наследуется от этого.
 *
 */
public interface Task {

    /**
     * Создание блокировки на объекте выполнения задачи
     *
     * @param lockKey  ключ блокировки, по которому она будет установлена
     * @param user     пользователь, который инициировал операцию
     * @return null, если была установлена новая блокировка, иначе возвращается ранее установленная блокировка на этом объъекте
     */
    LockData establishLock(String lockKey, TAUserInfo user, Map<String, Object> params);

    /**
     * Проверка существования блокировок, которые мешают постановке в очередь текущей задачи
     * @param params    произвольные параметры для выполнения задачи
     * @param logger    логгер
     * @return true, если задачи существуют
     */
    boolean prohibitiveLockExists(Map<String, Object> params, Logger logger);

    /**
     * Создать сообщение об ошибке при существовании блокировок не дающих создать задачу
     * @return  строка сообщения
     */
    String createLockExistErrorMessage(String objectName, String lockKey);
}
