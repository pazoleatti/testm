package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Отчётный период.
 * В нормальной ситуации может быть только один активный отчётный период по каждому виду налога
 * исключения возможны в случае использования корректирующих периодов.
 * TODO: это класс-заглушка, необходимая для реализации прототипа, в будущем будет расширен и дополнен
 * @author dsultanbekov
 */
public class ReportPeriod implements Serializable {
	// Первичный ключ
	private int id;
	// Название периода
	private String name;
	// Вид налога
	private TaxType taxType;
	// Признак активности периода (активный - значит открыт в настоящий момент)
	private boolean active;

	/**
	 * Получить идентификатор отчётного периода
	 */
	public int getId() {
		return id;
	}
	/**
	 * Задать идентфикатор отчётного периода
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * Получить название периода
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * Задать название периода
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Получить вид налога, по которому ведётся отчётный период 
	 */
	public TaxType getTaxType() {
		return taxType;
	}
	/**
	 * Задать вид налога, по которому ведётся отчётный период
	 * @param taxType вид налога
	 */
	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
	/**
	 * Возвращает значение признака активности отчётного перида
	 * @return true - если период активен, false - в противном случае
	 */
	public boolean isActive() {
		return active;
	}
	/**
	 * Задать значение признака активности отчётного периода
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
}
