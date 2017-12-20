package com.aplana.sbrf.taxaccounting.service.impl.scheduler;

import com.aplana.sbrf.taxaccounting.cache.CacheManagerDecorator;
import com.aplana.sbrf.taxaccounting.dao.TaxEventDao;
import com.aplana.sbrf.taxaccounting.model.TaxChangesEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Обработчик событий из УН, полученных через view VW_LOG_TABLE_CHANGE
 *
 * @author dloshkarev
 */
@Component
public class TaxEventProcessor {
    private static final Log LOG = LogFactory.getLog(TaxEventProcessor.class);

    @Autowired
    private TaxEventDao taxEventDao;
    @Autowired
    private CacheManagerDecorator cacheManagerDecorator;

    /**
     * Проверяет появление новых необработанных событий из УН, выполняет их обработку и сохраняет отметку об обработке в БД НДФЛ
     */
    public void processTaxEvents() {
        List<TaxChangesEvent> newEvents = taxEventDao.getNewTaxEvents();
        if (!newEvents.isEmpty()) {
            boolean needToClearCache = false;
            for (TaxChangesEvent event : newEvents) {
                if (event.getTableName().equals(TaxChangesEvent.TaxTableNames.DEPARTMENT.name()) ||
                        event.getTableName().equals(TaxChangesEvent.TaxTableNames.SEC_USER.name())) {
                    //Изменилась таблица пользователей или подразделений - нужно сбросить серверный кэш в НДФЛ
                    needToClearCache = true;
                }
                taxEventDao.processTaxEvent(event);
            }

            if (needToClearCache) {
                cacheManagerDecorator.clearAll();
            }
        }
    }
}
