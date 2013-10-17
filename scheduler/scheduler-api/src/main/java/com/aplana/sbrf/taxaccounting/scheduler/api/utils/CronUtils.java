package com.aplana.sbrf.taxaccounting.scheduler.api.utils;

/**
 * Утилитный класс для формирования выражения в формате IBM CRON
 * @author dloshkarev
 */
public class CronUtils {
    private enum CRON_ITEM {
        MINUTE(0), HOUR(1), DAY_OF_MONTH(2), MONTH(3), DAY_OF_WEEK(4);
        int position;

        private CRON_ITEM(Integer position) {
            this.position = position;
        }

        private int getPosition() {
            return position;
        }
    }

    public enum DAY_OF_WEEK {
        MON(0), TUE(1), WED(2), THU(3), FRI(4), SAT(5), SUN(6);

        private int value;

        private DAY_OF_WEEK(Integer value) {
            this.value = value;
        }

        public static DAY_OF_WEEK getDayByIntValue(Integer nDay) {
            for (DAY_OF_WEEK day : DAY_OF_WEEK.values()) {
                if (day.value == nDay) {
                    return day;
                }
            }
            throw new NumberFormatException("Неправильно указан день недели. Учитывайте, что понедельник = 0");
        }
    }
    public enum MONTH {
        JAN(1),FEB(2),MAR(3),APR(4),MAY(5),JUN(6),JUL(7),AUG(8),SEP(9),OCT(10),NOV(11),DEC(12);

        private int value;

        private MONTH(Integer value) {
            this.value = value;
        }

        public static MONTH getMonthByIntValue(Integer nMonth) {
            for (MONTH month : MONTH.values()) {
                if (month.value == nMonth) {
                    return month;
                }
            }
            throw new NumberFormatException("Неправильно указан месяц");
        }
    }

    /**
     * Преобразование стандартного CRON-выражения в IBM Scheduler формат
     * @param baseCron стандартное CRON-выражение
     * @return преобразованное CRON-выражение
     */
    public static String assembleIbmCronExpression(String baseCron) {
        try {
            String[] cronItems = baseCron.split(" ");
            String dayOfMonth = cronItems[CRON_ITEM.DAY_OF_MONTH.getPosition()];
            String month = cronItems[CRON_ITEM.MONTH.getPosition()];
            String dayOfWeek = cronItems[CRON_ITEM.DAY_OF_WEEK.getPosition()];

            //The day of week and day of month terms cannot be specified at the same time.
            // One must be a '?' and the other a term.
            if (dayOfMonth.equals(dayOfWeek) && dayOfMonth.equals("*")) {
                dayOfWeek = "?";
            }
            if ((!dayOfMonth.equals("*") && !dayOfMonth.equals("?"))
                    && dayOfWeek.equals("*")) {
                dayOfWeek = "?";
            }
            if ((!dayOfWeek.equals("*") && !dayOfWeek.equals("?"))
                    && dayOfMonth.equals("*")) {
                dayOfMonth = "?";
            }

            //Секунды не учитываем
            StringBuilder sb = new StringBuilder("0 ");

            //Минуты
            sb.append(cronItems[CRON_ITEM.MINUTE.getPosition()]).append(" ");

            //Часы
            sb.append(cronItems[CRON_ITEM.HOUR.getPosition()]).append(" ");

            //Дни
            sb.append(dayOfMonth).append(" ");

            //Месяцы
            if (isItemNumber(month)) {
                sb.append(MONTH.getMonthByIntValue(Integer.valueOf(month)));
            } else {
                sb.append(month);
            }
            sb.append(" ");

            //Дни недели
            if (isItemNumber(dayOfWeek)) {
                sb.append(DAY_OF_WEEK.getDayByIntValue(Integer.valueOf(dayOfWeek)));
            } else {
                sb.append(dayOfWeek);
            }

            return sb.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка обработки CRON-выражения в расписании выполнения задачи", e);
        }
    }

    /**
     * Метод для преобразования cron-выражения формата ibm в стандартный
     * @param ibmCron cron-выражения формата ibm
     * @return стандартное cron-выражение
     */
    public static String getBaseCron(String ibmCron) {
        return ibmCron.substring(2).replaceAll("\\?", "*");
    }

    private static boolean isItemNumber(String item) {
        return (!item.equals("*") && !item.equals("?"));
    }


}
