package com.aplana.sbrf.taxaccounting.async;

import org.springframework.stereotype.Component;

@Component
/**
 * Фабрика для получения объектов создающих асинхронную задачу по видам операций
 */
//TODO: проработать аргументы
public class StartupAsyncTaskHandlerFactory {
    /**
     * Получить обработчик
     * @return возвращает созданного обработчика
     */
    AbstractStartupAsyncTaskHandler getHandler() {
        return null;
    }
}
