package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Стили офрмления ячеек в налоговой форме
 * @author dsultanbekov
 */
public class FormStyle implements Serializable {
	private static final long serialVersionUID = 7152539133796468066L;

	private static final char STYLE_BOLD = 'b';
	private static final char STYLE_ITALIC = 'i';
	public static final char COLOR_SEPARATOR = '-';
	private static final String STYLE_PARSING_ERROR_MESSAGE = "Ошибка чтения стилей ячейки ";

	public static final String NO_CHANGE_STYLE_ALIAS = "Корректировка-без изменений";
	public static final String INSERT_STYLE_ALIAS = "Корректировка-добавлено";
	public static final String DELETE_STYLE_ALIAS = "Корректировка-удалено";
	public static final String CHANGE_STYLE_ALIAS = "Корректировка-изменено";
	public static final String EDITABLE_STYLE_ALIAS = "Редактируемая";
	public static final String AUTO_FILL_STYLE_ALIAS = "Автозаполняемая";

	public static final String DEFAULT_STYLE_ALIAS = "По умолчанию";
	public static final String MANUAL_EDITABLE_STYLE_ALIAS = "Ручной ввод-редактируемая";
	public static final String MANUAL_READ_ONLY_STYLE_ALIAS = "Ручной ввод-только чтение";
	/**
	 * Стиль по умолчанию
	 */
	public static final FormStyle DEFAULT_STYLE = new FormStyle(DEFAULT_STYLE_ALIAS, Color.BLACK, Color.WHITE, false, false);
	/**
	 * Стиль для редактируемых ячеек в режиме ручного ввода
	 */
	public static final FormStyle MANUAL_EDITABLE_STYLE = new FormStyle(MANUAL_EDITABLE_STYLE_ALIAS, Color.BLACK, Color.LIGHT_BLUE, false, false);
	/**
	 * Стиль для НЕредактируемых ячеек в режиме ручного ввода
	 */
	public static final FormStyle MANUAL_READ_ONLY_STYLE = new FormStyle(MANUAL_READ_ONLY_STYLE_ALIAS, Color.BLACK, Color.WHITE, false, false);

	private String alias;
	private Color fontColor = Color.BLACK; // цвет шрифта по умолчанию
	private Color backColor = Color.WHITE; // цвет фона по умолчанию
	private boolean italic = false;
	private boolean bold = false;

	public FormStyle() {
		super();
	}

	public FormStyle(String alias, Color fontColor, Color backColor, boolean italic, boolean bold) {
		super();
		setAlias(alias);
		setFontColor(fontColor);
		setBackColor(backColor);
		setItalic(italic);
		setBold(bold);
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
		if (alias != null ? !alias.equals(formStyle.alias) : formStyle.alias != null) return false; //todo удалить
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

	/**
	 * Осуществляет разбор строки стиля, работает с кэшем стилей
	 * @param styleString
	 * @return
	 */
	public static final FormStyle valueOf(String styleString) {
		FormStyle formStyle = new FormStyle();
		if (styleString == null || styleString.isEmpty()) {
			return formStyle;
		}
		if (styleString.length() < 3) { // минимально возможное число символов = 3: "N-N"
			throw new IllegalArgumentException(STYLE_PARSING_ERROR_MESSAGE + '"' + styleString + '"');
		}
		StringBuilder fontColor = new StringBuilder();
		StringBuilder backColor = new StringBuilder();
		boolean fontScan = true; // флаг. true - поиск цвета шрифта, false - поиск цвета фона
		for (int i = 0; i < styleString.length(); i++) {
			char ch = styleString.charAt(i);
			switch (ch) {
				case STYLE_BOLD:
					formStyle.setBold(true);
					break;
				case STYLE_ITALIC:
					formStyle.setItalic(true);
					break;
				case COLOR_SEPARATOR:
					if (fontColor.length() == 0) {
						throw new IllegalArgumentException(STYLE_PARSING_ERROR_MESSAGE + '"' + styleString + '"');
					}
					formStyle.setFontColor(Color.getById(Integer.valueOf(fontColor.toString())));
					fontScan = false;
					break;
				default:
					if (fontScan) {
						fontColor.append(ch);
					} else {
						backColor.append(ch);
					}
			}
		}
		if (backColor.length() == 0) {
			throw new IllegalArgumentException(STYLE_PARSING_ERROR_MESSAGE + '"' + styleString + '"');
		}
		formStyle.setBackColor(Color.getById(Integer.valueOf(backColor.toString())));
		return formStyle;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isItalic()) {
			sb.append(STYLE_ITALIC);
		}
		if (isBold()) {
			sb.append(STYLE_BOLD);
		}
		sb.append(getFontColor().getId())
			.append(COLOR_SEPARATOR)
			.append(getBackColor().getId());
		return sb.toString();
	}
}