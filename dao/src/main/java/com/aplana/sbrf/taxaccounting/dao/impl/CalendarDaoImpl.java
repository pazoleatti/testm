package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.CalendarDao;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
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
    private static final String GET_NEXT_WORK_DAY_ZERO_OFFSET_SQL =
            "select cdate from (select c.*, ROWNUM AS rn from (\n" +
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

    @Override
    public Date getWorkDay(Date startDate, int offset) {
        if (startDate == null) {
            throw new NullPointerException();
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("offset", Math.abs(offset) + 1);
        try {
            if (offset >= 1) {
                return new LocalDate(getNamedParameterJdbcTemplate().queryForObject(GET_NEXT_WORK_DAY_SQL, params, Date.class)).toDate();
            } else if (offset == 0) {
                return new LocalDate(getNamedParameterJdbcTemplate().queryForObject(GET_NEXT_WORK_DAY_ZERO_OFFSET_SQL, params, Date.class)).toDate();
            } else {
                return new LocalDate(getNamedParameterJdbcTemplate().queryForObject(GET_PREV_WORK_DAY_SQL, params, Date.class)).toDate();
            }
        } catch (EmptyResultDataAccessException e) {
            //Календарь не заполнен в нужном диапазоне - сдвигаем без учета производственного календаря
            return DateUtils.addDays(startDate, offset);
        }
    }

    @Override
    public Date getLastWorkDayByYear(int year) {
        try {
            return getJdbcTemplate().queryForObject("select max(cdate) from (\n" +
                    "  select extract(year from cdate) year, cal.* from ref_book_calendar cal\n" +
                    ") cal\n" +
                    "where ctype = 0 and year = ?\n" +
                    "group by year", Date.class, year);
        } catch (EmptyResultDataAccessException e) {
            return new LocalDate(year, 12, 31).toDate();
        }
    }

    public int getDateDif(Date startDate, Date endDate) {
        Days daysBetween = Days.daysBetween(new LocalDate(startDate), new LocalDate(endDate));
        Integer period = daysBetween.getDays();
        //Результат возвращаем по модулю, чтобы не зависеть от того, какая дата раньше startDate, или endDate
        return Math.abs(period);
    }

    public int getWorkDayCount(Date startDate, Date endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);
        int holidayCount = getNamedParameterJdbcTemplate().queryForObject(HOLIDAY_COUNT_SQL, params, int.class);

        return getDateDif(startDate, endDate) - holidayCount + 1;
    }
}