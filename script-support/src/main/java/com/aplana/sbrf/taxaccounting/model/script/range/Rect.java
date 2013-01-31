package com.aplana.sbrf.taxaccounting.model.script.range;

/**
 * Описывает прямоугольник
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 31.01.13 13:50
 */

public class Rect {

	private static final String WRONG_BOUNDS = "Неверно указан диапазон: (%d; %d) - (%d; %d)";

	public int x1, y1, x2, y2;

	public Rect() {
	}

	public Rect(int x1, int y1, int x2, int y2) {
		if (x2 < x1 || x1 < 0 || y2 < y1 || y1 < 0)
			throw new IllegalArgumentException(String.format(WRONG_BOUNDS, x1, y1, x2, y2));
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public int getWidth() {
		return x2 - x1 + 1;
	}

	public int getHeight() {
		return y2 - y1 + 1;
	}

	/**
	 * Сравнивает размеры текущего прямоугольника с указанным
	 *
	 * @param rect
	 * @return
	 */
	public boolean isSameSize(Rect rect) {
		return getWidth() == rect.getWidth() && getHeight() == rect.getHeight();
	}

}
