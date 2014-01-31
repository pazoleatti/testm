package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.SourceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class SourceServiceImpl implements SourceService {

    @Autowired
    DepartmentFormTypeDao departmentFormTypeDao;
    
    @Autowired
    DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
    
    @Autowired
    FormTypeDao formTypeDao;
    
    @Autowired
    DeclarationTypeDao declarationTypeDao;


    @Override
    public List<DepartmentFormType> getDFTSourcesByDFT(int departmentId, int formTypeId, FormDataKind kind) {
        return departmentFormTypeDao.getFormSources(departmentId, formTypeId, kind);
    }

    @Override
    public void saveFormSources(Long departmentFormTypeId, List<Long> sourceDepartmentFormTypeIds) {
    	// TODO Добавить проверки на то что пользователь может сделать это назначение
        departmentFormTypeDao.saveFormSources(departmentFormTypeId, sourceDepartmentFormTypeIds);
    }

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
        return departmentFormTypeDao.getFormDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind);
    }

    @Override
    public List<DepartmentFormType> getDFTSourcesByDepartment(int departmentId, TaxType taxType) {
        return departmentFormTypeDao.getDepartmentSources(departmentId, taxType);
    }

    @Override
    public List<DepartmentFormType> getDFTByDepartment(int departmentId, TaxType taxType) {
        return departmentFormTypeDao.getByTaxType(departmentId, taxType);
    }

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
        return departmentFormTypeDao.getDeclarationDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind);
    }

    @Override
    public List<DepartmentFormType> getDFTSourceByDDT(int departmentId, int declarationTypeId) {
        return departmentFormTypeDao.getDeclarationSources(departmentId, declarationTypeId);
    }

    @Override
    public void saveDeclarationSources(Long declarationTypeId, List<Long> sourceDepartmentFormTypeIds) {
    	// TODO Добавить проверки на то что пользователь может сделать это назначение
        departmentFormTypeDao.saveDeclarationSources(declarationTypeId, sourceDepartmentFormTypeIds);
    }

    @Override
    public List<FormTypeKind> getFormAssigned(Long departmentId, char taxType) {
        return departmentFormTypeDao.getFormAssigned(departmentId, taxType);
    }

    @Override
    public List<FormTypeKind> getDeclarationAssigned(Long departmentId, char taxType) {
        return departmentFormTypeDao.getDeclarationAssigned(departmentId, taxType);
    }

    @Override
    public void saveDFT(Long departmentId, int typeId, int formId) {
        departmentFormTypeDao.save(departmentId.intValue(), typeId, formId);
    }

    @Override
    public void saveDFT(Long departmentId, int typeId, int formId, Integer performerId) {
        departmentFormTypeDao.save(departmentId.intValue(), typeId, formId, performerId);
    }

    @Override
    public void deleteDFT(Collection<Long> ids) {
    	for (Long id : ids) {
    		departmentFormTypeDao.delete(id);
    	}
    }

    @Override
    public void saveDDT(Long departmentId, int declarationId) {
        departmentDeclarationTypeDao.save(departmentId.intValue(), declarationId);
    }

    @Override
    public void deleteDDT(Collection<Long> ids) {
    	for (Long id : ids) {
    		departmentDeclarationTypeDao.delete(id);
		}
    }

	@Override
	public FormType getFormType(int formTypeId) {
		return formTypeDao.get(formTypeId);
	}

	@Override
	public List<FormType> listAllByTaxType(TaxType taxType) {
		return formTypeDao.getByTaxType(taxType);
	}

	@Override
	public List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType) {
		return departmentDeclarationTypeDao.getByTaxType(departmentId, taxType);
	}

	@Override
	public DeclarationType getDeclarationType(int declarationTypeId) {
		return declarationTypeDao.get(declarationTypeId);
	}

    @Override
    public boolean existAssignedForm(int departmentId, int typeId, FormDataKind kind) {
        return departmentFormTypeDao.existAssignedForm(departmentId, typeId, kind);
    }

	@Override
	public List<DeclarationType> allDeclarationTypeByTaxType(TaxType taxType) {
		return declarationTypeDao.listAllByTaxType(taxType);
	}

    @Override
    public void updatePerformer(int id, int performerId){
        departmentFormTypeDao.updatePerformer(id, performerId);
    }
}
