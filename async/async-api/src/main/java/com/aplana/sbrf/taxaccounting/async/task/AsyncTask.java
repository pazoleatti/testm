package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;

import java.util.Date;
import java.util.Map;

/**
 * Интерфейс задачи, выполняющей некую бизнес логику асинхронно
 * @author dloshkarev
 */
public interface AsyncTask {

    /**
     * Предопределенные имена для входного списка параметров метода execute
     * Сюда внесены только параметры, используемые в общей логике
     */
    enum RequiredParams {
        /** Название параметра для передачи идентификатора объекта, заблокированного для выполнения над ним бизнес-логики */
        LOCKED_OBJECT(String.class),
        /** Дата начала действия блокировки объекта */
        LOCK_DATE(Date.class),
        /** Идентификатор текущего пользователя */
        USER_ID(Integer.class);

        /** Тип значения параметра */
        private Class clazz;

        RequiredParams(Class clazz) {
            this.clazz = clazz;
        }

        public Class getClazz() {
            return clazz;
        }
    }

    /**
     * Выполнение бизнес-логики задачи
     * @param params входные параметры, переданные при старте задачи. Все они должны поддерживать сериализацию
     */
    void execute(Map<String, Object> params);

    /**
     * Выполняет проверку возможности выполения задачи,
     * если выполнение задачи возможно, то возвращает в какой очереди выполнять
     * иначе
     * @param params
     * @return
     */
    BalancingVariants checkTaskLimit(Map<String, Object> params) throws AsyncTaskException;
}
