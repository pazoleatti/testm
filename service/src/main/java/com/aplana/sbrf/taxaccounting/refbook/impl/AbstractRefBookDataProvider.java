package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.FormLink;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Абстрактный класс провайдера данных для справочников
 * Содержит общие и базовые методы всех провайдеров
 */
public abstract class AbstractRefBookDataProvider implements RefBookDataProvider{

    @Autowired
    protected RefBookDao refBookDao;

    @Autowired
    protected RefBookFactory refBookFactory;

    @Override
    public List<FormLink> isVersionUsedInForms(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, Boolean restrictPeriod) {
        Set<FormLink> results = new HashSet<FormLink>();
        results.addAll(refBookDao.isVersionUsedInForms(refBookId, uniqueRecordIds, versionFrom, versionTo, restrictPeriod));
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);

        // ищет использование версии в формах для всех составных справочников
        List<Long> refBookIds = provider.usedInRefBookIds();
        for (Long compositeRefBookId : refBookIds) {
            RefBookDataProvider compositeProvider = refBookFactory.getDataProvider(compositeRefBookId);
            List<Long> ids = compositeProvider.convertIds(refBookId, uniqueRecordIds);
            results.addAll(refBookDao.isVersionUsedInForms(compositeRefBookId, ids, versionFrom, versionTo, restrictPeriod));
        }
        return new ArrayList<FormLink>(results);
    }

    @Override
    public List<Long> usedInRefBookIds() {
        return Collections.emptyList();
    }

    @Override
    public List<Long> convertIds(Long refBookId, List<Long> ids) {
        return ids;
    }
}
