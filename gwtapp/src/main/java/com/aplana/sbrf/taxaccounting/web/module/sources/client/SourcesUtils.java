package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * Класс утилит для формы Источники/Приемники
 *
 * @author aivanov
 * @since 28.05.2014
 */
public class SourcesUtils {

    public static boolean isCorrectPeriod(PeriodsInterval periodsInterval) {
        PeriodInfo periodFrom = periodsInterval.getPeriodFrom();
        Integer yearFrom = periodsInterval.getYearFrom();
        PeriodInfo periodTo = periodsInterval.getPeriodTo();
        Integer yearTo = periodsInterval.getYearTo();

        return periodFrom != null &&
                (yearTo == null && periodTo == null ||
                        (yearTo != null && periodTo != null &&
                                (yearFrom < yearTo ||
                                        (yearFrom.equals(yearTo) && periodFrom.getStartDate().compareTo(periodTo.getStartDate()) < 1)
                                )
                        )
                );
    }

    public static Label getEmptyWidget(final DepartmentPicker picker) {
        final Label lab = new Label("Пусто. Для загрузки данных выберите подразделение.");
        lab.getElement().getStyle().setColor("#aeaeac");
        lab.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    if (picker.getSingleValue() == null) {
                        lab.setText("Пусто. Для загрузки данных выберите подразделение.");
                    } else {
                        lab.setText("Для выбранного подразделения назначений не было.");
                    }
                }
            }
        });
        return lab;
    }

    public static void setupPeriodTitle(ValueListBox<PeriodInfo> widget) {
        widget.setTitle(widget.getValue() != null ? widget.getValue().getName() : "");
    }
}
