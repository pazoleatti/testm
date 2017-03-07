package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.RegionSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RegionSecurityServiceImpl implements RegionSecurityService {

    private final static Long DEPARTMENT_REF_BOOK_ID = RefBookDepartmentDao.REF_BOOK_ID;

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    DepartmentService departmentService;

    @Override
    public boolean checkDelete(TAUser user, Long refBookId, Long uniqueRecordId, boolean isDeleteVersion) {
        return check(user, refBookId, uniqueRecordId, null, null, null, null, isDeleteVersion);
    }

    @Override
    public boolean check(TAUser user, Long refBookId, Long uniqueRecordId, Long recordCommonId,
                         Map<String, RefBookValue> values, Date start, Date end) {
        return check(user, refBookId, uniqueRecordId, recordCommonId, values, start, end, null);
    }

    /** Общий метод для проврки. */
    private boolean check(TAUser user, Long refBookId, Long uniqueRecordId, Long recordCommonId,
                         Map<String, RefBookValue> values, Date start, Date end, Boolean isDeleteVersion) {
        // если роль пользователя "контролер УНП", то завершить разрешив изменения
        if (user.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            return true;
        }

        // получить справочник и проверить региональный атрибут
        RefBook refBook = refBookFactory.get(refBookId);

        // если справочник не региональный или пользователь не "контролел НС", то завершить запретив изменения
        if (refBook.getRegionAttribute() == null || !user.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS)) {
            return false;
        }

        // получить подразделение пользователя, получить регион подразделения
        RefBookDataProvider departmentProvider = refBookFactory.getDataProvider(DEPARTMENT_REF_BOOK_ID);
        Map<String, RefBookValue> department = departmentProvider.getRecordData((long) user.getDepartmentId());
        Set<Long> availableRegions = new HashSet<Long>();
        for (Department dep : departmentService.getBADepartments(user, TaxType.NDFL)) {
            availableRegions.add(dep.getRegionId());
        }

        // проверить новые значения
        if (values != null && !values.isEmpty()) {
            RefBookValue newRecordRegion = values.get(refBook.getRegionAttribute().getAlias());
            if (newRecordRegion == null || newRecordRegion.getReferenceValue() == null) {
                // не заполнен регион записи, завершить разрешив изменения, далее при проверках обязательных атрибутов выдаст ошибку
                return false;
            }
            // если новый регион не соотвествует региону пользователя, то завершить запретив изменения
            Long newRecordRegionId = newRecordRegion.getReferenceValue();
            if (!availableRegions.contains(newRecordRegionId)) {
                return false;
            }
        }

        // если новая версия то проверить предыдущую версию относительно даты актуальности новой
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        if (uniqueRecordId == null && recordCommonId != null && start != null) {
            // проверка при добавлении новой версии, новая версия может быть не в конец записана,
            // а где нибудь между существующими версиями (от дат актуальности зависит),
            // одна из существующих версии может принадлежать региону пользователя, другая нет, или наоборот

            // получить все записи справочника на дату
            List<Pair<Long, Long>> pairs = provider.getRecordIdPairs(refBookId, start, false, null);
            // найти версии редактируемой записи
            List<Long> list = new ArrayList<Long>();
            for (Pair<Long, Long> pair : pairs) {
                if (recordCommonId.equals(pair.getSecond())) {
                    list.add(pair.getFirst());
                }
            }
            if (!list.isEmpty()) {
                // если есть версии у добавляемой записи, то проверить регион у предыдущей версии
                Map<Long, Date> versionDateMap = provider.getRecordsVersionStart(list);
                Long preRecordId = getActualRecordId(versionDateMap, start);
                Long preRecordRegionId = getRegionId(preRecordId, refBook.getRegionAttribute().getAlias(), provider);
                if (!availableRegions.contains(preRecordRegionId)) {
                    return false;
                }
            }
        }

        // если запись редактируется или удаляется, получить старые значения и проверить регион
        if (uniqueRecordId != null) {
            Long recordRegionId = getRegionId(uniqueRecordId, refBook.getRegionAttribute().getAlias(), provider);
            if (recordRegionId == null) {
                return false;
            }

            // проверить соответствие региона старой записи региону пользователя
            boolean isAllowed = availableRegions.contains(recordRegionId);

            if (isDeleteVersion != null && !isDeleteVersion) {
                // удаление записи - проверить все версии записи
                if (!isAllowed) {
                    return false;
                }
                PagingResult<Map<String, RefBookValue>> versions = provider.getRecordVersionsById(uniqueRecordId, null, null, null);
                for (Map<String, RefBookValue> version : versions) {
                    RefBookValue region = version.get(refBook.getRegionAttribute().getAlias());
                    // если регион версии не указан или он не равен региону пользователя, то проверка не проходит
                    if (region == null || region.getReferenceValue() == null || !availableRegions.contains(region.getReferenceValue())) {
                        return false;
                    }
                }
                return true;
            } else {
                // редактрирование или удаление версии - вернуть результат
                return isAllowed;
            }
        } else {
            // запись новая, не имеет версии
            return true;
        }
    }

    /**
     * Получить идентификатор региона у записи по уникальному идентификатору записи и алиасу регионального атрибута.
     *
     * @param uniqueRecordId уникальный идентификатор записи
     * @param alias алиас записи, являющийся региональным
     * @param provider провайдер
     */
    private Long getRegionId(Long uniqueRecordId, String alias, RefBookDataProvider provider) {
        Map<String, RefBookValue> record = provider.getRecordData(uniqueRecordId);
        RefBookValue recordRegion = record.get(alias);
        if (recordRegion == null || recordRegion.getReferenceValue() == null) {
            return null;
        }
        return recordRegion.getReferenceValue();
    }


    /**
     * Получить идентификатор актуальной записи из мапы со всеми версиями записи.
     *
     * @param versionDateMap мапа со всеми версиями записи
     * @param date дата на которую надо получить данные
     * @return идентификатор записи (ref_book_record.id)
     */
    Long getActualRecordId(Map<Long, Date> versionDateMap, Date date) {
        Long actualRecordId = null;

        // поиск id актуальной записи на дату date
        Long minDays = null;
        for (Long key : versionDateMap.keySet()) {
            Date value = versionDateMap.get(key);
            if (minDays == null || minDays != 0L) {
                if (date.equals(value) || (date.after(value) && (minDays == null || date.getTime() - value.getTime() < minDays))) {
                    minDays = date.getTime() - value.getTime();
                    actualRecordId = key;
                }
            }
        }

        // если нет актуальной записи, то брать следующую за ней
        if (actualRecordId == null) {
            minDays = null;
            for (Long key : versionDateMap.keySet()) {
                Date value = versionDateMap.get(key);
                if (date.before(value) && (minDays == null || date.getTime() - value.getTime() < minDays)) {
                    minDays = date.getTime() - value.getTime();
                    actualRecordId = key;
                }
            }
        }
        return actualRecordId;
    }
}
