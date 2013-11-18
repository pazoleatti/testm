package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.*;

/**
 * User: avanteev
 * Модельный класс, содержащий информацию о значениях, которые допустимы
 * в фильтре по журналу аудита.
 */
public class LogSystemFilterAvailableValues implements Serializable {

    /**
     * Набор подразделений
     * В случае, если у пользователя есть доступ ко всем подразделениям, нужно присваивать null
     */
    private List<Department> departments;

    /**
     * Набор видов налоговых форм
     */
    private List<FormType> formTypes;

    /**
     * Набор видов деклараций
     */
    private List<DeclarationType> declarationTypes;

    private List<FormDataKind> formDataKinds;
    private List<TaxType> taxTypes;

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<FormType> getFormTypes() {
        return formTypes;
    }

    public void setFormTypes(List<FormType> formTypes) {
        this.formTypes = formTypes;
    }

    public List<DeclarationType> getDeclarationTypes() {
        return declarationTypes;
    }

    public void setDeclarationTypes(List<DeclarationType> declarationTypes) {
        this.declarationTypes = declarationTypes;
    }

    public Set<Integer> getDepartmentIds(){
        Set<Integer> result = new HashSet<Integer>();
        for(Department department : departments){
            result.add(department.getId());
        }
        return result;
    }

    public Map<Integer, String> getFormTypeMapIds(){
        Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();
        formTypesMap.put(null, "");
        for(FormType formType : formTypes){
            formTypesMap.put(formType.getId(), formType.getName());
        }
        return formTypesMap;
    }

    public Map<Integer, String> getDeclarationMapIds(){
        Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();
        formTypesMap.put(null, "");
        for(DeclarationType formType : declarationTypes){
            formTypesMap.put(formType.getId(), formType.getName());
        }
        return formTypesMap;
    }

    public List<FormDataKind> getFormDataKinds() {
        formDataKinds = new LinkedList<FormDataKind>();
        Collections.addAll(formDataKinds, FormDataKind.values());
        return formDataKinds;
    }

    public List<TaxType> getTaxTypes() {
        taxTypes = new LinkedList<TaxType>();
        Collections.addAll(taxTypes, TaxType.values());
        return taxTypes;
    }
}
