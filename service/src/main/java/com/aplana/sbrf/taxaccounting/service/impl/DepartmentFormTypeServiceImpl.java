package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentFormTypeServiceImpl implements DepartmentFormTypeService {

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
        return departmentFormTypeDao.getFormSources(departmentId, formTypeId, kind);
    }

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
        return departmentFormTypeDao.getFormDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind);
    }

    @Override
    public List<DepartmentFormType> getDepartmentFormSources(int departmentId, TaxType taxType) {
        return departmentFormTypeDao.getDepartmentSources(departmentId, taxType);
    }

	@Override
	public List<DepartmentFormType> getDepartmentFormDestinations(int departmentId, TaxType taxType) {
		return departmentFormTypeDao.getByTaxType(departmentId, taxType);
	}

	@Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
        return departmentFormTypeDao.getDeclarationDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind);
    }

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId) {
        return departmentFormTypeDao.getDeclarationSources(departmentId, declarationTypeId);
    }
}
