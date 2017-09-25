package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeclarationTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса для работы со справочником Виды форм
 */
@Service
public class RefBookDeclarationTypeServiceImpl implements RefBookDeclarationTypeService {
    private RefBookDeclarationTypeDao refBookDeclarationTypeDao;
    private PeriodService periodService;

    public RefBookDeclarationTypeServiceImpl(RefBookDeclarationTypeDao refBookDeclarationTypeDao, PeriodService periodService) {
        this.refBookDeclarationTypeDao = refBookDeclarationTypeDao;
        this.periodService = periodService;
    }

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefBookDeclarationType> fetchAllDeclarationTypes() {
        return refBookDeclarationTypeDao.fetchAll();
    }

    /**
     * Получение значений справочника для создания налоговой формы
     *
     * @param declarationKind Вид налоговой формы
     * @param departmentId    Подразделение
     * @param periodId        ID отчетного периода
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefBookDeclarationType> fetchDeclarationTypesForCreate(Long declarationKind, Integer departmentId, Integer periodId) {
        ReportPeriod reportPeriod = periodService.getReportPeriod(periodId);
        return refBookDeclarationTypeDao.fetchDeclarationTypesForCreate(declarationKind, departmentId, reportPeriod.getCalendarStartDate());
    }
}
