package com.aplana.sbrf.taxaccounting.service.impl.scheduler;

import com.aplana.sbrf.taxaccounting.dao.TaxEventDao;
import com.aplana.sbrf.taxaccounting.model.TaxChangesEvent;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.TaxChangesEvent.*;

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
    private PeriodService periodService;

    /**
     * Проверяет появление новых необработанных событий из УН, выполняет их обработку и сохраняет отметку об обработке в БД НДФЛ
     */
    public void processTaxEvents() {
        List<TaxChangesEvent> newEvents = taxEventDao.getNewTaxEvents();
        if (!newEvents.isEmpty()) {
            LOG.info("processTaxEvents started");
            for (TaxChangesEvent event : newEvents) {
                if (event.getTableName().equals(TaxTableNames.DEPARTMENT.name()) &&
                        event.getOperationName().equalsIgnoreCase(Operations.insert.name())) {
                    periodService.openForNewDepartment(event.getTableRowId().intValue());
                }
                taxEventDao.processTaxEvent(event);
            }
            LOG.info("processTaxEvents finished, " + newEvents.size() + " events processed");
        }
    }
}
