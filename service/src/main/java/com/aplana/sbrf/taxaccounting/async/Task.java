package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.util.Map;

/**
 * Интерфейс создаваемой задачи
 */
public interface Task {

    /**
     * Создание блокировки на объекте выполнения задачи
     *
     * @param lockKey  ключ блокировки, по которому она будет установлена
     * @param user     пользователь, который инициировал операцию
     * @return null, если была установлена новая блокировка, иначе возвращается ранее установленная блокировка на этом объъекте
     */
    LockData lockObject(String lockKey, TAUserInfo user, Map<String, Object> params);

    /**
     * Проверка существования блокировок, которые мешают постановке в очередь текущей задачи
     *
     * @return true, если задачи существуют
     */
    boolean checkLocks(Map<String, Object> params);

    /**
     * Получить сообщение об ошибке при существовании блокировок не дающих создать задачу
     * @return  строка сообщения
     */
    String getLockExistErrorMessage();
}
