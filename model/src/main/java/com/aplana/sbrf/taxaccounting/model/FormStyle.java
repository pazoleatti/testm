package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Стили офрмления ячеек в налоговой форме
 * @author dsultanbekov
 */
public class FormStyle implements Serializable {
	private static final long serialVersionUID = 7152539133796468066L;

	private Integer id;
	private String alias;
	private boolean italic = false;
	private boolean bold = false;

	/**
	 * Идентификатор записи в БД, если null, то стиль еще не был сохранён в БД
	 * @return идентификатор записи в БД или null, если стиль еще не сохранялся в БД
	 */
    @XmlTransient
	public Integer getId() {
		return id;
	}
	/**
	 * Задать идентификатор стиля в БД
	 * @param id значение идентификатора (если объект еще не сохранялся в БД, то null)
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Получить алиас стиля (используется в скриптах при указании стиля ячеек)
	 * @return алас стиля
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Задать алиас стиля
	 * @param alias значение алиаса
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	/**
	 * Возвращает флаг необходимости использовать курсив 
	 * @return флаг необходимости использовать курсив
	 */
	public boolean isItalic() {
		return italic;
	}
	
	/**
	 * Задаёт флаг необходимости использования курсива
	 * @param italic true - использовать курсив, false - не использовать
	 */
	public void setItalic(boolean italic) {
		this.italic = italic;
	}
	
	/**
	 * Возвращает флаг необходимости использовать жирный шрифт 
	 * @return флаг необходимости использовать жирный шрифт
	 */	
	public boolean isBold() {
		return bold;
	}
	
	/**
	 * Задаёт флаг необходимости использования жирный шрифт
	 * @param bold true - использовать жирный шрифт, false - не использовать
	 */	
	public void setBold(boolean bold) {
		this.bold = bold;
	}

}
