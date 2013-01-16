package com.aplana.sbrf.taxaccounting.model;


/**
 * Enum, представляющий цвета
 * 
 * Потребность в таком классе возникла из-за того, что задаваемые стили необходимо отображать с использованием
 * разных средств выражения: HTML, POI, возможно и в JasperReports
 * @author dsultanbekov
 */
public enum Color {
	/**
	 * Чёрный цвет
	 */
	BLACK(0, 0, 0, 0),
	/**
	 * Красный цвет
	 */
	RED(1, 255, 0, 0),
	/**
	 * Зелёный цвет
	 */
	GREEN(2, 0, 255, 0),
	/**
	 * Синий цвет
	 */
	BLUE(3, 0, 0, 255),
	/**
	 * Белый цвет
	 */
	WHITE(4, 255, 255, 255);
	
	private Color(int id, int red, int green, int blue) {
		this.id = id;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	private final int id;
	private final int red;
	private final int green;
	private final int blue;
	
	/**
	 * Возвращает код цвета (используется при записи в БД и сериализации в XML)
	 * @return код цвета
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Возвращает значение Red-компоненты RGB-представления цвета
	 */
	public int getRed() {
		return red;
	}
	
	/**
	 * Возвращает значение Green-компоненты RGB-представления цвета
	 */	
	public int getGreen() {
		return green;
	}
	
	/**
	 * Возвращает значение Blue-компоненты RGB-представления цвета
	 */
	public int getBlue() {
		return blue;
	}
	
	/**
	 * Получить описание цвета по числовому идентификатору
	 * @param id идентификатор цвета
	 * @return объект Color, имеющий заданный идентификатор
	 * @throws IllegalArgumentException если передан идентификатор, для которого не задан цвет
	 */
	public static Color fromId(int id) {
		for (Color c: values()) {
			if (c.id == id) {
				return c;
			}
		}
		throw new IllegalArgumentException("Wrong color id: " + id);
	}
}
