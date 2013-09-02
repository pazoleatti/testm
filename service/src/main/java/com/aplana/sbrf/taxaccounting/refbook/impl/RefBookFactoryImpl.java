package com.aplana.sbrf.taxaccounting.refbook.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;

/**
 * Реализация фабрики провайдеров данных для справочников
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:22
 */
@Service("refBookFactory")
public class RefBookFactoryImpl implements RefBookFactory {

    @Autowired
    private RefBookDao refBookDao;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    RefBookScriptingService refBookScriptingService;

    @Override
    public RefBook get(Long refBookId) {
        return refBookDao.get(refBookId);
    }

    @Override
    public List<RefBook> getAll() {
        return refBookDao.getAll();
    }

    @Override
    public RefBook getByAttribute(Long attributeId) {
        return refBookDao.getByAttribute(attributeId);
    }

    @Override
    public RefBookDataProvider getDataProvider(Long refBookId) {
        RefBookDataProvider refBookDataProvider;
        if (refBookId == RefBookDepartment.REF_BOOK_ID) {
            refBookDataProvider = applicationContext.getBean("refBookDepartment", RefBookDataProvider.class);
        } else if (refBookId == RefBookIncome101.REF_BOOK_ID) {
            refBookDataProvider = applicationContext.getBean("refBookIncome101", RefBookDataProvider.class);
        } else if (refBookId == RefBookIncome102.REF_BOOK_ID) {
            refBookDataProvider = applicationContext.getBean("refBookIncome102", RefBookDataProvider.class);
        } else {
            refBookDataProvider = applicationContext.getBean("universal", RefBookDataProvider.class);   // Исправление Марата, надо сделать получать данные отдельно для конкретных провайдеров
            if (refBookDataProvider instanceof RefBookUniversal) {
                ((RefBookUniversal) refBookDataProvider).setRefBookId(refBookId);
            }
        }
        return refBookDataProvider;
    }


}
