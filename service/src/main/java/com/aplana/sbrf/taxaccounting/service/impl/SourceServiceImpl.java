package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SourceServiceImpl implements SourceService {
    private static final Log LOG = LogFactory.getLog(SourceServiceImpl.class);

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    private static final Date MIN_DATE = new Date(0);

    @Autowired
    private DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
    @Autowired
    private DeclarationTypeDao declarationTypeDao;
    @Autowired
    private SourceDao sourceDao;

    /**
     * Проверяет начало диапазона дат и если оно не задано, то возвращает значение по умолчанию
     *
     * @param date
     * @return
     */
    private Date checkMinDate(Date date) {
        return date != null ? date : MIN_DATE;
    }

    @Override
    public int getAssignedDeclarationsCount(List<Long> departmentsIds, char taxType) {
        return departmentDeclarationTypeDao.getAssignedDeclarationsCount(departmentsIds, taxType);
    }

    @Override
    public void addDeclarationConsolidationInfo(Long tgtDeclarationId, Collection<Long> srcFormDataIds) {
        sourceDao.addDeclarationConsolidationInfo(tgtDeclarationId, srcFormDataIds);
    }

    @Override
    public void deleteDeclarationConsolidateInfo(long targetDeclarationDataId) {
        sourceDao.deleteDeclarationConsolidateInfo(targetDeclarationDataId);
    }

    @Override
    public boolean isDeclarationSourceConsolidated(long declarationId, long sourceFormDataId) {
        return sourceDao.isDeclarationSourceConsolidated(declarationId, sourceFormDataId);
    }

    @Override
    public void updateDDConsolidation(long sourceFormId) {
        sourceDao.updateDDConsolidationInfo(sourceFormId);
    }

    @Override
    public boolean isDDConsolidationTopical(long ddTargetId) {
        return sourceDao.isDDConsolidationTopical(ddTargetId);
    }

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd) {
        return departmentDeclarationTypeDao.getDeclarationDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind, periodStart, periodEnd);
    }

    @Override
    public List<DeclarationTypeAssignment> getAllDeclarationAssigned(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams) {
        return departmentDeclarationTypeDao.getAllDeclarationAssigned(departmentIds, taxType, queryParams);
    }

    @Override
    public void updateDDTPerformers(int id, List<Integer> performerIds) {
        LOG.info(String.format("SourceServiceImpl.updateDDTPerformers. id: %s, performerIds: %s", id, performerIds));
        //Удаляем всех исполнителей и назначаем новых
        departmentDeclarationTypeDao.deletePerformers(id);
        if (performerIds != null && !performerIds.isEmpty()) {
            departmentDeclarationTypeDao.savePerformers(id, performerIds);
        }
    }

    @Override
    public void saveDDT(Long departmentId, int declarationId, List<Integer> performerIds) {
        LOG.info(String.format("SourceServiceImpl.saveDDT. departmentId: %s, declarationId: %s, performerIds: %s", departmentId, declarationId, performerIds));
        long ddtId = departmentDeclarationTypeDao.save(departmentId.intValue(), declarationId);
        departmentDeclarationTypeDao.savePerformers(ddtId, performerIds);
    }

    @Override
    public void deleteDDT(Collection<Long> ids) {
        LOG.info(String.format("SourceServiceImpl.deleteDDT. ids: %s", ids));
        for (Long id: ids) {
            //TODO dloshkarev: можно переделать на in запрос
            departmentDeclarationTypeDao.delete(id);
        }
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType, Date periodStart,
                                                              Date periodEnd) {
        QueryParams<SourcesSearchOrdering> queryParams = getSearchOrderingDefaultFilter();
        return getDDTByDepartment(departmentId, taxType, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd, QueryParams queryParams) {
        return departmentDeclarationTypeDao.getByTaxType(departmentId, taxType, periodStart, periodEnd, queryParams);
    }

    @Override
    public DeclarationType getDeclarationType(int declarationTypeId) {
        return declarationTypeDao.get(declarationTypeId);
    }

    @Override
    public List<DeclarationType> allDeclarationTypeByTaxType(TaxType taxType) {
        return declarationTypeDao.listAllByTaxType(taxType);
    }

    @Override
    public List<Pair<DepartmentDeclarationType, Pair<Date, Date>>> findDestinationDTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        return departmentDeclarationTypeDao.findDestinationDTsForFormType(typeId, checkMinDate(dateFrom), dateTo);
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDeclarationType(Integer declarationTypeId) {
        return departmentDeclarationTypeDao.getDDTByDeclarationType(declarationTypeId);
    }

    /**
     * Фильтр по умолчанию
     */
    private QueryParams<SourcesSearchOrdering> getSearchOrderingDefaultFilter() {
        QueryParams<SourcesSearchOrdering> queryParams = new QueryParams<SourcesSearchOrdering>();
        queryParams.setSearchOrdering(SourcesSearchOrdering.TYPE);
        queryParams.setAscending(true);
        return queryParams;
    }

    @Override
    public List<Relation> getDeclarationSourcesInfo(DeclarationData declaration, boolean light, boolean excludeIfNotExist, State stateRestriction, TAUserInfo userInfo, Logger logger) {
        return sourceDao.getSourcesInfo(declaration.getId());
    }

    @Override
    public List<Relation> getDeclarationDestinationsInfo(DeclarationData declaration, boolean light, boolean excludeIfNotExist, State stateRestriction, TAUserInfo userInfo, Logger logger) {
        return sourceDao.getDestinationsInfo(declaration.getId());
    }
}
