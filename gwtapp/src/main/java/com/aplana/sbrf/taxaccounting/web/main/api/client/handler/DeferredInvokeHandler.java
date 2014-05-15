package com.aplana.sbrf.taxaccounting.web.main.api.client.handler;

/**
 * Интерфейя для хендлеров методы которых нужно вызвать после завершения асинхронный запросов
 *
 * @author aivanov
 * @since 14.05.2014
 */
public interface DeferredInvokeHandler {

    void onInvoke();
}
