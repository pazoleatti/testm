package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

public class DeclarationFilter implements Serializable {

	private static final long serialVersionUID = -4400641153082281834L;

	private TaxType taxType;

	private List<Integer> reportPeriodIds;

	private List<Integer> departmentIds;

	/*Стартовый индекс списка записей */
	private int startIndex;

	/*Количество записей, которые нужно вернуть*/
	private int countOfRecords;

	private DeclarationSearchOrdering searchOrdering;

	/*true, если сортируем по возрастанию, false - по убыванию*/
	private boolean ascSorting;

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public List<Integer> getReportPeriodIds() {
		return reportPeriodIds;
	}

	public void setReportPeriodIds(List<Integer> reportPeriodIds) {
		this.reportPeriodIds = reportPeriodIds;
	}

	public List<Integer> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(List<Integer> departmentIds) {
		this.departmentIds = departmentIds;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getCountOfRecords() {
		return countOfRecords;
	}

	public void setCountOfRecords(int countOfRecords) {
		this.countOfRecords = countOfRecords;
	}

	public DeclarationSearchOrdering getSearchOrdering() {
		return searchOrdering;
	}

	public void setSearchOrdering(DeclarationSearchOrdering searchOrdering) {
		this.searchOrdering = searchOrdering;
	}

	public boolean isAscSorting() {
		return ascSorting;
	}

	public void setAscSorting(boolean ascSorting) {
		this.ascSorting = ascSorting;
	}

}
