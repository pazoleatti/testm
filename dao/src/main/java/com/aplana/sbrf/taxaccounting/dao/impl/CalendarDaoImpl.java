package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.CalendarDao;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 29.03.2017 20:46
 */
@Repository
public class CalendarDaoImpl extends AbstractDao implements CalendarDao {

	private static final String GET_NEXT_WORK_DAY_SQL =
			"select cdate from (select c.*, ROWNUM AS rn from (select cdate from ref_book_calendar where ctype = 1 and cdate = trunc(:startDate) union \n" +
					"  select cdate from ref_book_calendar where cdate >= trunc(:startDate) AND ctype = 0 ORDER BY cdate\n" +
					") c) where rn = :offset";
	private static final String GET_PREV_WORK_DAY_SQL =
			"select cdate from (select c.*, ROWNUM AS rn from (\n" +
					"  select cdate from ref_book_calendar where cdate <= trunc(:startDate) AND ctype = 0 ORDER BY cdate desc\n" +
					") c) where rn = :offset";

	// Количество нерабочих дней между датами
	private static final String HOLIDAY_COUNT_SQL =
			"SELECT COUNT(*) FROM ref_book_calendar " +
					"WHERE cdate >= :startDate AND cdate <= :endDate AND ctype = 1";

	/**
	 * Возвращает дату рабочего дня, смещенного относительно даты startDate.
	 *
	 * @param startDate начальная дата, может быть и рабочим днем и выходным
	 * @param offset на сколько рабочих дней необходимо сдвинуть начальную дату. Может быть меньше 0, тогда сдвигается в обратную сторону
	 * @return смещенная на offset рабочих дней дата
	 */
	@Override
	public Date getWorkDay(Date startDate, int offset) {
		if (startDate == null) {
			throw new NullPointerException();
		}

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("startDate", startDate);
		params.addValue("offset", Math.abs(offset) + 1);
		try {
			if (offset >= 0) {
				return getNamedParameterJdbcTemplate().queryForObject(GET_NEXT_WORK_DAY_SQL, params, Date.class);
			} else {
				return getNamedParameterJdbcTemplate().queryForObject(GET_PREV_WORK_DAY_SQL, params, Date.class);
			}
		} catch (EmptyResultDataAccessException e) {
			//Календарь не заполнен в нужном диапазоне - сдвигаем без учета производственного календаря
			return DateUtils.addDays(startDate, offset);
		}
	}

	/**
	 * Разница в днях между двумя датами
	 *
	 * @param startDate начальная дата
	 * @param endDate конечная дата
	 * @return
	 */
	public int getDateDif(Date startDate, Date endDate) {
		Days daysBetween = Days.daysBetween(new LocalDate(startDate), new LocalDate(endDate));
		Integer period = daysBetween.getDays();
		//Результат возвращаем по модулю, чтобы не зависеть от того, какая дата раньше startDate, или endDate
		return Math.abs(period);
	}

	/**
	 * Возвращает количество рабочих дней между двумя датами
	 *
	 * @param startDate начальная дата
	 * @param endDate конечная дата
	 * @return количество рабочих дней
	 */
	public int getWorkDayCount(Date startDate, Date endDate) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("startDate", startDate);
		params.addValue("endDate", endDate);
		int holidayCount = getNamedParameterJdbcTemplate().queryForObject(HOLIDAY_COUNT_SQL, params, int.class);

		return getDateDif(startDate, endDate) - holidayCount + 1;
	}
}