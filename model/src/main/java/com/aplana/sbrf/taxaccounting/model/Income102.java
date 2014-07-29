package com.aplana.sbrf.taxaccounting.model;

/*
 * Отчет о прибылях и убытках  (Форма 0409102-СБ)
 */
public class Income102 {

	// Код записи
	private Long id;
	// Код ОПУ
	private String opuCode;
	// Сумма
	private Double totalSum;
    // Наименование статьи
    private String itemName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOpuCode() {
		return opuCode;
	}

	public void setOpuCode(String opuCode) {
		this.opuCode = opuCode;
	}

	public Double getTotalSum() {
		return totalSum;
	}

	public void setTotalSum(Double totalSum) {
		this.totalSum = totalSum;
	}

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
