package com.aplana.sbrf.taxaccounting.web.widget.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;

import java.util.Date;

/**
 * Общие методы-утилиты использующиеся многими виджетами
 * Специально не сокращал код условий что бы можно было легко проследить логику
 * @author aivanov
 */
public class WidgetUtils {

    public static DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd.MM.yyyy");
    public static String iconUrl = "resources/img/picker-icons/clear-icon.png";
    public static String dummyUrl = "resources/img/picker-icons/clear-icon-dummy.png";

    public static String PICK_ALL = "Выделить все";
    public static String UNPICK_ALL = "Снять выделение";

    /**
     * Проверка невхождения даты в ограничивающий период
     *
     * @param startDate наччало огр периода
     * @param endDate   коннец огр периода
     * @param current   дату которую проверяют
     * @return тру если не входит, иначе фолс
     */
    public static Boolean isInLimitPeriod(Date startDate, Date endDate, Date current) {
        if (current != null) {
            if (startDate == null && endDate == null) {
                return true;
            }
            if (startDate != null && endDate != null) {
                return compareDates(current, startDate) > -1 &&  compareDates(current, endDate) < 1;
            } else {
                return (startDate != null && compareDates(current, startDate) > -1) ||
                        (endDate != null && compareDates(current, endDate) < 1);
            }
        } else {
            return false;
        }
    }
    /**
     * Сравнение объектов
     * @param before до
     * @param after после
     * @return тру если даты разные, false - если одинаковые
     */
    public static boolean isWasChange(Object before, Object after) {
        if (before == null && after == null) {
            return false;
        }
        if (before != null && after != null) {
            return before instanceof Date && after instanceof Date ? ((Date) before).compareTo(((Date) after)) != 0 : !before.equals(after);
        }
        return true;
    }

    /**
     * Сравнение дат без учета времени
     * @param before до
     * @param after после
     * @return тру если даты разные, false - если одинаковые
     */
    public static boolean isDateWasChange(Date before, Date after){
        if (before == null && after == null) {
            return false;
        }
        if (before != null && after != null) {
            return compareDates(before, after) != 0;
        }
        return true;
    }

    /**
     * Сравнение дат исключая значения времени
     * нет проверки на null
     * @param thisDate дата с временем
     * @param anotherDate дата с временем
     * @return 0 если равны, 1 если вторая больше первой, -1 если первая меньше второй
     */
    public static int compareDates(Date thisDate, Date anotherDate) {
        Date thisDatewithoutTime = getDateWithOutTime(thisDate);
        Date anotherDatewithoutTime = getDateWithOutTime(anotherDate);
        return thisDatewithoutTime.compareTo(anotherDatewithoutTime);
    }

    /**
     * Убирает у даты значение времени выставляя в 0:0:0
     * @param date дата с временем
     * @return дата без времени
     */
    public static Date getDateWithOutTime(Date date) {
        return date != null ? dateTimeFormat.parse(dateTimeFormat.format(date)) : null;
    }

    /**
     * Возвращает строку с датой в виде dd.MM.yyyy
     * @param date дата
     * @return строка вида 23.12.2013
     */
    public static String getDateString(Date date) {
        return date != null ? dateTimeFormat.format(date) : null;
    }

    /**
     * Добавляет поведения для скрытия/появления clearButton при отведении/наведении курсора мыши на виджеты input и pickButton
     * Все виджеты должны реализовывать интерфейс HasMouseOutHandlers и HasMouseOverHandlers
     *
     * @param clearButton виджет который будет показываться при наведении мыши на anotherWidget
     */
    public static void setMouseBehavior(final Image clearButton, final TextBox input, final Image pickButton) {
        if (clearButton != null && input != null && pickButton != null) {

            final Element elementToHide = clearButton.getElement();
            MouseOverHandler mouseOverHandler = new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    if (input.getText() != null && !input.getText().isEmpty()) {
                        clearButton.setUrl(iconUrl);
                        clearButton.setTitle("Очистить выбор");
                        setPointerCursor(elementToHide, true);
                    }
                }
            };
            MouseOutHandler mouseOutHandler = new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    if (event.getRelatedTarget()!= null && !event.getRelatedTarget().equals(elementToHide)) {
                        clearButton.setUrl(dummyUrl);
                        clearButton.setTitle("");
                        setPointerCursor(elementToHide, false);
                    }
                }
            };

            ClickHandler clickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    clearButton.setUrl(dummyUrl);
                    clearButton.setTitle("");
                    setPointerCursor(elementToHide, false);
                }
            };

            input.addMouseOutHandler(mouseOutHandler);
            input.addMouseOverHandler(mouseOverHandler);
            pickButton.addMouseOutHandler(mouseOutHandler);
            pickButton.addMouseOverHandler(mouseOverHandler);

            clearButton.addMouseOverHandler(mouseOverHandler);
            clearButton.addMouseOutHandler(new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    if (event.getRelatedTarget()!= null && !event.getRelatedTarget().equals(input.getElement()) &&
                            !event.getRelatedTarget().equals(pickButton.getElement())) {
                        clearButton.setUrl(dummyUrl);
                        clearButton.setTitle("");
                        setPointerCursor(elementToHide, false);
                    }
                }
            });
            clearButton.addClickHandler(clickHandler);
            pickButton.addClickHandler(clickHandler);

        }
    }

    public static void setPointerCursor(Element element, boolean pointer) {
        if (pointer) {
            element.getStyle().setCursor(Style.Cursor.POINTER);
        } else {
            element.getStyle().clearCursor();
        }
    }



}
