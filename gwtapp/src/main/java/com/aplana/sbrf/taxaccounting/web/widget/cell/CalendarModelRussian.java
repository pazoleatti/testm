package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.user.datepicker.client.CalendarModel;

public class CalendarModelRussian extends CalendarModel{

    private final static String[] DAYS_OF_WEEK = {"Вск","Пн","Вт","Ср","Чт","Пт","Сб"};
    private final static String[] MONTHS_OF_YEAR_RUS = {"Янв","Фев","Март","Апр","Май","Июнь","Июль","Авг","Сен","Окт","Нояб","Дек"};
    private final static String[] MONTHS_OF_YEAR_ENG = {"Jan","Feb","Mar", "Apr","May","Jun", "Jul", "Aug","Sep","Oct","Nov", "Dec"};

    @Override
    public String formatDayOfWeek(int dayInWeek) {
        return DAYS_OF_WEEK[dayInWeek];
    }

    @Override
    public String formatCurrentMonth() {
        final int YEAR_BEGIN_POSITION = 0;
        final int YEAR_END_POSITION = 4;
        final int MONTH_BEGIN_POSITION = 5;
        final int MONTH_END_POSITION = super.formatCurrentMonth().length();

        for(int i = 0; i < MONTHS_OF_YEAR_ENG.length; i++){
            if(MONTHS_OF_YEAR_ENG[i].equals(super.formatCurrentMonth().substring(MONTH_BEGIN_POSITION,
                    MONTH_END_POSITION))){
                return (super.formatCurrentMonth().substring(YEAR_BEGIN_POSITION, YEAR_END_POSITION) +
                        " " + MONTHS_OF_YEAR_RUS[i]);
            }
        }
        return "Неопр";
    }
}