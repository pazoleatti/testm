package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentFormTypeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service("departmentFormTypeService")
@Transactional(readOnly=true)
public class DepartmentFormTypeServiceImpl extends AbstractDao implements DepartmentFormTypeService {

    @Autowired
    DepartmentFormTypeDao departmentFormTypeDao;

    @Override
    public List<DepartmentFormType> getDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
        return getFormDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind);
    }

    @Override
	public List<DepartmentFormType> getSources(int departmentId, int formTypeId, FormDataKind kind) {
		return getFormSources(departmentId, formTypeId, kind);
	}

    @Override
    public List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind) {
        return getFormSources(departmentId, formTypeId, kind, null, null);
    }

    @Override
    public List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind, Date periodStart, Date periodEnd) {
        // TODO (Ramil Timerbaev) При periodStart, periodEnd == null метод должен работать по-старому
        return departmentFormTypeDao.getFormSources(departmentId, formTypeId, kind);
    }

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
        return getFormDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind, null, null);
    }

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd) {
        // TODO (Ramil Timerbaev) При periodStart, periodEnd == null метод должен работать по-старому
        return departmentFormTypeDao.getFormDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind);
    }

    @Override
    public List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType) {
        return getDepartmentSources(departmentId, taxType, null, null);
    }

    @Override
    public List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        // TODO (Ramil Timerbaev) При periodStart, periodEnd == null метод должен работать по-старому
        return departmentFormTypeDao.getDepartmentSources(departmentId, taxType);
    }

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
        return getDeclarationDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind, null, null);
    }

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd) {
        // TODO (Ramil Timerbaev) При periodStart, periodEnd == null метод должен работать по-старому
        return departmentFormTypeDao.getDeclarationDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind);
    }

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId) {
        return getDeclarationSources(departmentId, declarationTypeId, null, null);
    }

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd) {
        // TODO (Ramil Timerbaev) При periodStart, periodEnd == null метод должен работать по-старому
        return departmentFormTypeDao.getDeclarationSources(departmentId, declarationTypeId);
    }
}
