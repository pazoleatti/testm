package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;
import java.util.List;

/**
 * Налоговый период.
 * 
 * Налоговый период может содержать один или несколько {@link ReportPeriod отчётных периодов} 
 * @author dsultanbekov
 */
public class TaxPeriod extends IdentityObject<Integer> {
	private static final long serialVersionUID = 1L;
	
	private TaxType taxType;
	private Date startDate;
	private Date endDate;
	private List<DictionaryTaxPeriod> dictionaryTaxPeriod;
	
	/**
	 * @return вид налога
	 */
	public TaxType getTaxType() {
		return taxType;
	}
	
	/**
	 * Задать вид налога
	 * @param taxType вид налога
	 */
	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
	
	/**
	 * @return дата начала периода (включительно)
	 */
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * Задать дату начала периода (включительно)
	 * @param startDate дата начала периода
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * @return дата окончания периода (включительно)
	 */
	public Date getEndDate() {
		return endDate;
	}
	
	/**
	 * Задать дату окончания периода (включительно)
	 * @param endDate дата окончания периода
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public List<DictionaryTaxPeriod> getDictionaryTaxPeriod() {
		return dictionaryTaxPeriod;
	}

	public void setDictionaryTaxPeriod(List<DictionaryTaxPeriod> dictionaryTaxPeriod) {
		this.dictionaryTaxPeriod = dictionaryTaxPeriod;
	}
}
