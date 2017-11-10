package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.SimpleTree;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Компонент дерево для отображения периодов
 * @author unknown
 */
public class PeriodPickerWidget extends SimpleTree implements PeriodPicker{

    public PeriodPickerWidget(boolean multiselect){
        super("Выберите период", multiselect);
        scrollPanel.addStyleName(style.hiddenXScroll());
    }

    @Override
    public void setAcceptableValues(Collection<List<Integer>> values) {
        // All value acceptable
    }

    @Override
    public void setPeriods(List<ReportPeriod> periods) {
        tree.clear();

        Map<Integer, MultiSelectTreeItem> periodYearsMap = new LinkedHashMap<Integer, MultiSelectTreeItem>();

        for(ReportPeriod reportPeriod : periods){

            if (!periodYearsMap.containsKey(reportPeriod.getTaxPeriod().getYear())){
                MultiSelectTreeItem taxPeriodItem = new MultiSelectTreeItem(String.valueOf(reportPeriod.getTaxPeriod().getYear()), null);
                periodYearsMap.put(reportPeriod.getTaxPeriod().getYear(), taxPeriodItem);
            }

            MultiSelectTreeItem reportPeriodItem = new MultiSelectTreeItem(reportPeriod.getId(), reportPeriod.getName(), isMultiSelection());
            reportPeriodItem.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    ValueChangeEvent.fire(PeriodPickerWidget.this, getValue());
                }

            });
            periodYearsMap.get(reportPeriod.getTaxPeriod().getYear()).addItem(reportPeriodItem);
        }

        for (MultiSelectTreeItem taxPeriodTreeItem : periodYearsMap.values()) {
            addTreeItem(taxPeriodTreeItem);
        }

    }

    @Override
    public void setTaxType(String taxType) {
        // Операция не поддерживается. Пока не нужна была.
        throw new UnsupportedOperationException();
    }
}
