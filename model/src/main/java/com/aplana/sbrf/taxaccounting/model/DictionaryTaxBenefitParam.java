package com.aplana.sbrf.taxaccounting.model;





/*
 * Модель справочника "Параметры налоговых льгот"
 * @author auldanov
 */
public class DictionaryTaxBenefitParam {
	// код региона 
	private Integer dictRegionId;
	//Код налоговой льготы
	private String taxBenefitId ;
	//Основание - статья
	private String section;
	//Основание - пункт
	private String item;
	//Основание - подпункт
	private String subitem;
	//Уменьшающий процент, %
	private Double percent;
	//Пониженная ставка
	private Double rate;
	
	public DictionaryTaxBenefitParam() {
	
	}
	
	public DictionaryTaxBenefitParam(Integer dictRegionId, String taxBenefitId,
			String section, String item, String subitem, Double percent,
			Double rate) {
		super();
		this.dictRegionId = dictRegionId;
		this.taxBenefitId = taxBenefitId;
		this.section = section;
		this.item = item;
		this.subitem = subitem;
		this.percent = percent;
		this.rate = rate;
	}

	
	public Integer getDictRegionId() {
		return dictRegionId;
	}
	
	public void setDictRegionId(Integer dictRegionId) {
		this.dictRegionId = dictRegionId;
	}
	
	public String getTaxBenefitId() {
		return taxBenefitId;
	}
	
	public void setTaxBenefitId(String taxBenefitId) {
		this.taxBenefitId = taxBenefitId;
	}
	
	public String getSection() {
		return section;
	}
	
	public void setSection(String section) {
		this.section = section;
	}
	
	public String getItem() {
		return item;
	}
	
	public void setItem(String item) {
		this.item = item;
	}
	
	public String getSubitem() {
		return subitem;
	}
	
	public void setSubitem(String subitem) {
		this.subitem = subitem;
	}
	
	public Double getPercent() {
		return percent;
	}
	
	public void setPercent(Double percent) {
		this.percent = percent;
	}
	
	public Double getRate() {
		return rate;
	}
	
	public void setRate(Double rate) {
		this.rate = rate;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DictionaryTaxPrivilegeParam [dictRegionId=");
		builder.append(dictRegionId);
		builder.append(", taxBenefitId=");
		builder.append(taxBenefitId);
		builder.append(", section=");
		builder.append(section);
		builder.append(", item=");
		builder.append(item);
		builder.append(", subitem=");
		builder.append(subitem);
		builder.append(", percent=");
		builder.append(percent);
		builder.append(", rate=");
		builder.append(rate);
		builder.append("]");
		return builder.toString();
	}

	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DictionaryTaxBenefitParam other = (DictionaryTaxBenefitParam) obj;
		if (dictRegionId == null) {
			if (other.dictRegionId != null)
				return false;
		} else if (!dictRegionId.equals(other.dictRegionId))
			return false;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		if (percent == null) {
			if (other.percent != null)
				return false;
		} else if (!percent.equals(other.percent))
			return false;
		if (rate == null) {
			if (other.rate != null)
				return false;
		} else if (!rate.equals(other.rate))
			return false;
		if (section == null) {
			if (other.section != null)
				return false;
		} else if (!section.equals(other.section))
			return false;
		if (subitem == null) {
			if (other.subitem != null)
				return false;
		} else if (!subitem.equals(other.subitem))
			return false;
		if (taxBenefitId == null) {
			if (other.taxBenefitId != null)
				return false;
		} else if (!taxBenefitId.equals(other.taxBenefitId))
			return false;
		return true;
	}
	
}
