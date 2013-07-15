package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentDeclarationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DepartmentDeclarationTypeServiceImpl implements DepartmentDeclarationTypeService {

    @Autowired
	DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

	@Override
	public List<DepartmentDeclarationType> getDepartmentDeclarationTypes(int departmentId) {
		return departmentDeclarationTypeDao.getDepartmentDeclarationTypes(departmentId);
	}

	@Override
	public List<DepartmentDeclarationType> getDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
		return departmentDeclarationTypeDao.getDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind);
	}

	@Override
	public Set<Integer> getDepartmentIdsByTaxType(TaxType taxType) {
		return departmentDeclarationTypeDao.getDepartmentIdsByTaxType(taxType);
	}

	@Override
	public List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType) {
		return departmentDeclarationTypeDao.getByTaxType(departmentId, taxType);
	}

	@Override
	public void save(int departmentId, List<DepartmentDeclarationType> departmentDeclarationTypes) {
		departmentDeclarationTypeDao.save(departmentId, departmentDeclarationTypes);
	}
}
