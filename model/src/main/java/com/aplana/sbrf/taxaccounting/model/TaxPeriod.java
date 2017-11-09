package com.aplana.sbrf.taxaccounting.model;

/**
 * Налоговый период.
 * 
 * Налоговый период может содержать один или несколько {@link ReportPeriod отчётных периодов} 
 * @author dsultanbekov
 */
public class TaxPeriod extends IdentityObject<Integer> {
	private static final long serialVersionUID = 1L;

	/** Тип налогового периода {@link TaxType}*/
	private TaxType taxType;
	/** Год налогового периода */
	private Integer year;

	public TaxPeriod(Integer year, Integer id, TaxType taxType) {
		this.taxType = taxType;
		this.year = year;
		this.id = id;
	}

	public TaxPeriod() {
	}

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
	 * Задать год налогового периода
	 * @return
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * Вернуть год, к которому относится налоговый период
	 * @param year
	 */
	public void setYear(Integer year) {
		this.year = year;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("TaxPeriod{");
		sb.append("taxType=").append(taxType);
		sb.append(", year=").append(year);
		sb.append('}');
		return sb.toString();
	}
}
