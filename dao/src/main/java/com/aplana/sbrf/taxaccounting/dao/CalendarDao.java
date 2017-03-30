package com.aplana.sbrf.taxaccounting.dao;

import java.util.Date;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 29.03.2017 20:44
 */

public interface CalendarDao {

	/**
	 * Возвращает дату рабочего дня, смещенного относительно даты startDate.
	 *
	 * @param startDate начальная дата, может быть и рабочим днем и выходным
	 * @param offset на сколько рабочих дней необходимо сдвинуть начальную дату. Может быть меньше 0, тогда сдвигается в обратную сторону
	 * @return смещенная на offset рабочих дней дата
	 */
	Date getWorkDay(Date startDate, int offset);

	/**
	 * Разница в днях между двумя датами
	 *
	 * @param startDate начальная дата
	 * @param endDate конечная дата
	 * @return
	 */
	int getDateDif(Date startDate, Date endDate);

	/**
	 * Возвращает количество рабочих дней между двумя датами
	 *
	 * @param startDate начальная дата
	 * @param endDate конечная дата
	 * @return количество рабочих дней
	 */
	int getWorkDayCount(Date startDate, Date endDate);

}