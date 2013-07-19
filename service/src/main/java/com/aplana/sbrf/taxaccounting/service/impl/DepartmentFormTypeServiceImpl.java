package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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
    public void saveFormSources(Long departmentFormTypeId, List<Long> sourceDepartmentFormTypeIds) {
        departmentFormTypeDao.saveFormSources(departmentFormTypeId, sourceDepartmentFormTypeIds);
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

    @Override
    public void saveDeclarationSources(Long declarationTypeId, List<Long> sourceDepartmentFormTypeIds) {
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
    public void saveForm(Long departmentId, int typeId, int formId) {
        departmentFormTypeDao.createDepartmentFormType(departmentId,typeId,formId);
    }

    @Override
    public void deleteForm(Set<Long> ids) {
        departmentFormTypeDao.deleteDepartmentFormType(ids);
    }

    @Override
    public void saveDeclaration(Long departmentId, int declarationId) {
        departmentFormTypeDao.createDepartmentDeclType(departmentId, declarationId);
    }

    @Override
    public void deleteDeclaration(Set<Long> ids) {
        departmentFormTypeDao.deleteDepartmentDeclType(ids);
    }
}
