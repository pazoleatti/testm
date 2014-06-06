package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Autowired
    FormDataService formDataService;

    @Autowired
    DepartmentDao departmentDao;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    FormDataDao formDataDao;

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
    public List<Long> getDFTByPerformerDep(int performerDepId, TaxType taxType, List<FormDataKind> kinds) {
        return departmentFormTypeDao.getByPerformerId(performerDepId, taxType, kinds);
    }

    @Override
    public List<Long> getDFTFormTypeBySource(int performerDepId, TaxType taxType, List<FormDataKind> kinds) {
        return departmentFormTypeDao.getFormTypeBySource(performerDepId, taxType, kinds);
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
            //TODO dloshkarev: можно переделать на in запрос
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
            //TODO dloshkarev: можно переделать на in запрос
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
    public Map<String, List> getSourcesDestinations(int departmentId, int terrBankId, List<TaxType> taxTypes) {
        HashMap<String, List> map = new HashMap<String, List>();
        List<Pair<DepartmentFormType, DepartmentFormType>> destinationFT = departmentFormTypeDao.getFormDestinationsWithDepId(departmentId, terrBankId,taxTypes);
        map.put("destinationFTs", destinationFT);
        List<Pair<DepartmentFormType, DepartmentFormType>> sourceFTs = departmentFormTypeDao.getFormSourcesWithDepId(departmentId, terrBankId,taxTypes);
        map.put("sourceFTs", sourceFTs);
        List<Pair<DepartmentFormType, DepartmentDeclarationType>> destinationDTs = departmentFormTypeDao.getDeclarationDestinationsWithDepId(departmentId, terrBankId,taxTypes);
        map.put("destinationDTs", destinationDTs);
        List<Pair<DepartmentFormType, DepartmentDeclarationType>> sourceDTs = departmentFormTypeDao.getDeclarationSourcesWithDepId(departmentId, terrBankId,taxTypes);
        map.put("sourceDTs", sourceDTs);
        return map;
    }

    @Override
    public List<Pair<String, String>> existAcceptedDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Integer reportPeriodId) {
        return departmentFormTypeDao.existAcceptedDestinations(sourceDepartmentId, sourceFormTypeId, sourceKind, reportPeriodId);
    }

    @Override
	public List<DeclarationType> allDeclarationTypeByTaxType(TaxType taxType) {
		return declarationTypeDao.listAllByTaxType(taxType);
	}

    @Override
    public void updatePerformer(int id, Integer performerId){
        departmentFormTypeDao.updatePerformer(id, performerId);
    }

    @Override
    public List<FormToFormRelation> getRelations(int departmentId, int formTypeId, FormDataKind kind, boolean includeDestinations, boolean includeSources, boolean includeUncreated) {
        List<FormToFormRelation> formToFormRelations = new ArrayList<FormToFormRelation>();
        // включения источников
        if (includeSources){
            List<DepartmentFormType> sourcesForm = getDFTSourcesByDFT(departmentId, formTypeId, kind);
            formToFormRelations.addAll(createFormToFormRelationModel(sourcesForm, true, includeUncreated));
        }

        // включения приемников
        if (includeDestinations){
            List<DepartmentFormType> destinationsForm = getFormDestinations(departmentId, formTypeId, kind);
            formToFormRelations.addAll(createFormToFormRelationModel(destinationsForm, false, includeUncreated));
        }

        return formToFormRelations;
    }

    /**
     * Метод для составления списка с информацией об источниках приемниках
     * @param departmentFormTypes
     * @param isSource - true источник иначе приемник
     * @param includeUncreatedForms флаг включения не созданных нф в список
     * @return
     */
    private List<FormToFormRelation> createFormToFormRelationModel(List<DepartmentFormType> departmentFormTypes, boolean isSource, boolean includeUncreatedForms){
        List<FormToFormRelation> formToFormRelations = new ArrayList<FormToFormRelation>(departmentFormTypes.size());
        for (DepartmentFormType departmentFormType : departmentFormTypes) {
            FormToFormRelation formToFormRelation = new FormToFormRelation();
            /** источник/приемник */
            formToFormRelation.setSource(isSource);
            /** исполнитель */
            formToFormRelation.setPerformer(departmentDao.getDepartment(departmentFormType.getPerformerId()));
            /** Полнино название подразделения */
            formToFormRelation.setFullDepartmentName(departmentService.getParentsHierarchy(departmentFormType.getDepartmentId()));
            // TODO Добавить период
            FormData formData = formDataDao.find(departmentFormType.getFormTypeId(), departmentFormType.getKind(), departmentFormType.getDepartmentId(), 1);
            if (formData != null){
                /** Форма существует */
                formToFormRelation.setCreated(true);
                /** Установить статус */
                formToFormRelation.setState(formData.getState());
                /** вид формы */
                formToFormRelation.setFormType(formData.getFormType());
                /** тип нф */
                formToFormRelation.setFormDataKind(departmentFormType.getKind());
                /** установить id */
                formToFormRelation.setFormDataId(formData.getId());

                formToFormRelations.add(formToFormRelation);
            } else if (includeUncreatedForms){
                /** Формы не существует */
                formToFormRelation.setCreated(false);
                /** вид формы */
                formToFormRelation.setFormType(formTypeDao.get(departmentFormType.getFormTypeId()));
                /** тип нф */
                formToFormRelation.setFormDataKind(departmentFormType.getKind());

                formToFormRelations.add(formToFormRelation);
            }
        }

        return formToFormRelations;
    }
}
