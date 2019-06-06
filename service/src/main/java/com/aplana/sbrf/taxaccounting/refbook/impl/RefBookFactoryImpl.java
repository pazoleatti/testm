package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id.*;

@Service("refBookFactory")
public class RefBookFactoryImpl implements RefBookFactory {

    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private ApplicationContext applicationContext;

    // Кэш провайдеров
    private WeakHashMap<Long, RefBookDataProvider> providers = new WeakHashMap<>();

    // Список простых редактируемых версионируемых справочников
    private static final List<Long> simpleEditableRefBooks = Arrays.asList(
            TAX_PLACE_TYPE_CODE.getId(),
            COUNTRY.getId(),
            DETACH_TAX_PAY.getId(),
            MAKE_CALC.getId(),
            MARK_SIGNATORY_CODE.getId(),
            DOCUMENT_CODES.getId(),
            TB_PERSON.getId(),
            TAXPAYER_STATUS.getId(),
            PERSON.getId(),
            DEDUCTION_TYPE.getId(),
            INCOME_CODE.getId(),
            REGION.getId(),
            PRESENT_PLACE.getId(),
            OKVED.getId(),
            REORGANIZATION.getId(),
            FILL_BASE.getId(),
            TARIFF_PAYER.getId(),
            HARD_WORK.getId(),
            PERIOD_CODE.getId(),
            KBK.getId(),
            PERSON_CATEGORY.getId(),
            NDFL_DETAIL.getId(),
            NDFL_REFERENCES.getId(),
            INCOME_KIND.getId(),
            REPORT_PERIOD_IMPORT.getId(),

            // справочник ОКТМО отдельным списком идет, так как является версионируемым, но только для чтения
            // аналогично Справочник: "Признак кода вычета", реализован как нередактируемый
            OKTMO.getId(),
            DEDUCTION_MARK.getId());

    private RefBookDataProvider getDataProviderInternal(Long refBookId) {
        RefBook refBook = commonRefBookService.get(refBookId);

        if (simpleEditableRefBooks.contains(refBookId)) {
            RefBookSimpleDataProvider dataProvider = (RefBookSimpleDataProvider) applicationContext.getBean("refBookSimpleDataProvider", RefBookDataProvider.class);
            dataProvider.setRefBook(refBook);
            return dataProvider;
        }
        Assert.isTrue(!Strings.isNullOrEmpty(refBook.getTableName()));
        RefBookSimpleReadOnly dataProvider = (RefBookSimpleReadOnly) applicationContext.getBean("refBookSimpleReadOnly", RefBookDataProvider.class);
        dataProvider.setRefBook(refBook);
        return dataProvider;
    }

    @Override
    public RefBookDataProvider getDataProvider(Long refBookId) {
        if (providers.containsKey(refBookId)) {
            return providers.get(refBookId);
        } else {
            RefBookDataProvider provider = getDataProviderInternal(refBookId);
            providers.put(refBookId, provider);
            return provider;
        }
    }

}
