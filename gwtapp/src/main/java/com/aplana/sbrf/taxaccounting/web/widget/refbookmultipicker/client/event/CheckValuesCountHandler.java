package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event;

/**
 * Интерфейя для хендлеров методы которых нужно вызвать после завершения асинхронный запросов
 *
 * @author aivanov
 * @since 14.05.2014
 */
public interface CheckValuesCountHandler {

    void onGetValuesCount(Integer count);
}
