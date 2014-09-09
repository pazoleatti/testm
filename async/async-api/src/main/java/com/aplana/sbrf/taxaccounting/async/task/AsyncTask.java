package com.aplana.sbrf.taxaccounting.async.task;

import java.util.Map;

/**
 * Интерфейс задачи, выполняющей некую бизнес логику асинхронно
 * @author dloshkarev
 */
public interface AsyncTask {
    /**
     * Выполнение бизнес-логики задачи
     * @param params входные параметры, переданные при старте задачи. Все они должны поддерживать сериализацию
     */
    void execute(Map<String, Object> params);
}
