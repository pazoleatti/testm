package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.fixed.RefBookConfigurationParam;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

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
            PERSON_ADDRESS.getId(),
            ID_DOC.getId(),
            TAXPAYER_STATUS.getId(),
            PERSON.getId(),
            ID_TAX_PAYER.getId(),
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
            NDFL.getId(),
            NDFL_DETAIL.getId(),
            NDFL_REFERENCES.getId(),
            INCOME_KIND.getId(),

            // справочник ОКТМО отдельным списком идет, так как является версионируемым, но только для чтения
            // аналогично Справочник: "Признак кода вычета", реализован как нередактируемый
            OKTMO.getId(),
            DEDUCTION_MARK.getId(),

            // Ранее read_only справочники, но теперь нормально работают на simple-провайдере, который более производителен
            SEC_ROLE.getId(),
            ASNU.getId());

    private RefBookDataProvider getDataProviderInternal(Long refBookId) {
        RefBook refBook = commonRefBookService.get(refBookId);

        if (simpleEditableRefBooks.contains(refBookId)) {
            RefBookSimpleDataProvider dataProvider = (RefBookSimpleDataProvider) applicationContext.getBean("refBookSimpleDataProvider", RefBookDataProvider.class);
            dataProvider.setRefBook(refBook);
            return dataProvider;
        }

        if (DEPARTMENT.getId() == refBookId) {
            return applicationContext.getBean("refBookDepartment", RefBookDataProvider.class);
        }
        if (CONFIGURATION_PARAM.getId() == refBookId) {
            RefBookConfigurationParam dataProvider = applicationContext.getBean("refBookConfigurationParam", RefBookConfigurationParam.class);
            dataProvider.setRefBook(refBook);
            return dataProvider;
        }
        if (EMAIL_CONFIG.getId() == refBookId) {
            return applicationContext.getBean("refBookRefBookEmailConfig", RefBookEmailConfigProvider.class);
        }
        if (ASYNC_CONFIG.getId() == refBookId) {
            return applicationContext.getBean("refBookAsyncConfigProvider", RefBookAsyncConfigProvider.class);
        }
        if (refBook.getTableName() != null && !refBook.getTableName().isEmpty()) {
            RefBookSimpleReadOnly dataProvider = (RefBookSimpleReadOnly) applicationContext.getBean("refBookSimpleReadOnly", RefBookDataProvider.class);
            if (!refBook.getId().equals(RefBook.Id.CALENDAR.getId())) {
                dataProvider.setWhereClause("ID <> -1");
            }
            dataProvider.setRefBook(refBook);
            return dataProvider;
        } else {
            RefBookUniversal refBookUniversal = (RefBookUniversal) applicationContext.getBean("refBookUniversal", RefBookDataProvider.class);
            refBookUniversal.setRefBookId(refBookId);
            return refBookUniversal;
        }
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
