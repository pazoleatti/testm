package com.aplana.sbrf.taxaccounting.model;

import java.util.List;


/**
 * Модельный класс, содержащий информацию о значениях, которые допустимы
 * в фильтре по декларациям.
 * 
 * @author sgoryachkin
 */
public class DeclarationDataFilterAvailableValues {
	
	private List<Integer> departmentIds;
	
	private List<DeclarationType> declarationTypes;

	/**
	 * @return the departmentIds
	 */
	public List<Integer> getDepartmentIds() {
		return departmentIds;
	}

	/**
	 * @param departmentIds the departmentIds to set
	 */
	public void setDepartmentIds(List<Integer> departmentIds) {
		this.departmentIds = departmentIds;
	}

	/**
	 * @return the declarationTypes
	 */
	public List<DeclarationType> getDeclarationTypes() {
		return declarationTypes;
	}

	/**
	 * @param declarationTypes the declarationTypes to set
	 */
	public void setDeclarationTypes(List<DeclarationType> declarationTypes) {
		this.declarationTypes = declarationTypes;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeclarationDataFilterAvailableValues [departmentIds=");
		builder.append(departmentIds);
		builder.append(", declarationTypes=");
		builder.append(declarationTypes);
		builder.append("]");
		return builder.toString();
	}



}
