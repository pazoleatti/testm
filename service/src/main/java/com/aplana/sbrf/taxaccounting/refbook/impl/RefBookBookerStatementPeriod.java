package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookBookerStatementPeriodDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.impl.fixed.AbstractPermanentRefBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Провайдер для справочника "Периоды БО" (id = 108).
 */

@Service("refBookBookerStatementPeriod")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookBookerStatementPeriod extends AbstractPermanentRefBook {

    public static final Long REF_BOOK_ID = RefBookBookerStatementPeriodDao.REF_BOOK_ID;

    @Autowired
    RefBookBookerStatementPeriodDao bookerStatementPeriodDao;

    @Override
    protected PagingResult<Map<String, RefBookValue>> getRecords(String filter) {
        return bookerStatementPeriodDao.getRecords();
    }
}
