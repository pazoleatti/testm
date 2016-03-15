package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Стили офрмления ячеек в налоговой форме
 * @author dsultanbekov
 */
public class FormStyle implements Serializable {
	private static final long serialVersionUID = 7152539133796468066L;

	/**
	 * Стиль по умолчанию
	 */
	public static final FormStyle DEFAULT_STYLE = new FormStyle();
	/**
	 * Стиль для редактируемых ячеек в режиме ручного ввода
	 */
	public static final FormStyle MANUAL_EDITABLE_STYLE = new FormStyle() {{
		setBackColor(Color.LIGHT_BLUE);
	}};
	/**
	 * Стиль для НЕредактируемых ячеек в режиме ручного ввода
	 */
	public static final FormStyle MANUAL_READ_ONLY_STYLE = new FormStyle() {{
		setBackColor(Color.WHITE);
	}};

	private Integer id;
	private String alias;
	private Color fontColor = Color.BLACK; // цвет шрифта по умолчанию
	private Color backColor = Color.WHITE; // цвет фона по умолчанию
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
	 * Получить цвет шрифта, если null, то нужно использовать значение по-умолчанию 
	 * @return цвет шрифта
	 */
	public Color getFontColor() {
		return fontColor;
	}
	
	/**
	 * Задат цвет шрифта
	 * @param fontColor цвет шрифта, если null, то будет использоваться значение по-умолчанию
	 */
	public void setFontColor(Color fontColor) {
		if (fontColor == null) {
			throw new NullPointerException("Color must not be null");
		}
		this.fontColor = fontColor;
	}
	
	/**
	 * Получить цвет фона, если null, то нужно использовать значение по-умолчанию
	 * @return цвет фона
	 */
	public Color getBackColor() {
		return backColor;
	}
	
	/**
	 * Задать цвет фона
	 * @param backColor цвет фона, если null, то будет использоваться значение по-умолчанию
	 */
	public void setBackColor(Color backColor) {
		if (backColor == null) {
			throw new NullPointerException("Color must not be null");
		}
		this.backColor = backColor;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FormStyle formStyle = (FormStyle) o;

		if (italic != formStyle.italic) return false;
		if (bold != formStyle.bold) return false;
		if (alias != null ? !alias.equals(formStyle.alias) : formStyle.alias != null) return false;
		if (fontColor != formStyle.fontColor) return false;
		return backColor == formStyle.backColor;

	}

	@Override
	public int hashCode() {
		int result = alias != null ? alias.hashCode() : 0;
		result = 31 * result + fontColor.hashCode();
		result = 31 * result + backColor.hashCode();
		result = 31 * result + (italic ? 1 : 0);
		result = 31 * result + (bold ? 1 : 0);
		return result;
	}
}