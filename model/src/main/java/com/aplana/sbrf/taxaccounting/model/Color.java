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
	BLACK(0, 0, 0, 0, "Чёрный"),
	/**
	 * Белый
	 */
	WHITE(4, 255, 255, 255, "Белый"),
	/**
	 * Красный цвет
	 */
	LIGHT_YELLOW(1, 255, 255, 153, "Светло-желтый"),
	/**
	 * Светло коричневый
	 */
	LIGHT_BROWN(2, 255, 204, 153, "Светло-коричневый"),
	/**
	 * Светло-синий
	 */
	LIGHT_BLUE(3, 204, 255, 255, "Светло-голубой"),
	/**
	 * Темно-серый
	 */
	DARK_GREY(5, 149, 149, 149, "Темно-серый"),
	/**
	 * Серый
	 */
	GREY(6, 192, 192, 192, "Серый"),
	/**
	 * Голубой
	 */
	BLUE(7, 153, 204, 255, "Голубой"),
	/**
	 * Розовый
	 */
	PINK(8, 251, 208, 208, "Розовый"),
	/**
	 * Светло-оранжевый
	 */
	LIGHT_ORANGE(9, 255, 220, 130, "Светло-оранжевый"),
	/**
	 * Красный
	 */
	RED(10, 255, 0, 0, "Красный"),
	/**
	 *  Синий
	 */
	DARK_BLUE(11, 0, 0, 255, "Синий"),
	/**
	 * Зеленый
	 */
	GREEN(12, 0, 255, 0, "Зеленый");

	private Color(int id, int red, int green, int blue, String title) {
		this.id = id;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.title = title;
	}
	
	private final int id;
	private final int red;
	private final int green;
	private final int blue;
	private final String title;
	
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
	 * Возвращает название цвета (используется в админке)
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Получить описание цвета по числовому идентификатору
	 * @param id идентификатор цвета
	 * @return объект Color, имеющий заданный идентификатор
	 * @throws IllegalArgumentException если передан идентификатор, для которого не задан цвет
	 */
	public static Color getById(int id) {
		for (Color c: values()) {
			if (c.id == id) {
				return c;
			}
		}
		throw new IllegalArgumentException("Wrong color id: " + id);
	}
}
