package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client;

import com.aplana.gwt.client.DoubleStateComposite;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * Компонент для выбора периода из списка в всплывающем окне
 * @author sgorychkin
 */
public class PeriodPickerPopupWidget extends DoubleStateComposite implements
        PeriodPickerPopup, IsEditor<TakesValueEditor<List<Integer>>> {

    interface Binder extends UiBinder<Widget, PeriodPickerPopupWidget> {
    }

    private static Binder binder = GWT.create(Binder.class);

    @UiField
    HTMLPanel wrappingPanel;

    @UiField(provided = true)
    PeriodPickerWidget periodPicker;

    @UiField
    Image selectButton,
            clearButton;

    @UiField
    ModalWindow popupPanel;

    @UiField
    Panel panel;

    @UiField
    TextBox text;

    private List<Integer> value = new LinkedList<Integer>();
    /* Разименованные значения.   */
    private List<String> valueDereference = new LinkedList<String>();

    private TakesValueEditor<List<Integer>> editor;

    private Map<Integer, String> dereferenceValue;
    private Map<Integer, Pair<Date, Date>> reportPeriodDates;
    private Map<Integer, Integer> reportPeriodYears;
    private Set<Integer> reportPeriodIds;
    private String type;
    private boolean isSetDefaultValue = false;

    @UiConstructor
    public PeriodPickerPopupWidget(boolean multiselect) {
        periodPicker = new PeriodPickerWidget(multiselect);
        initWidget(binder.createAndBindUi(this));
        periodPicker.setHeaderVisible(false);
        WidgetUtils.setMouseBehavior(clearButton, text, selectButton);
    }

    @Override
    public void setPeriods(List<ReportPeriod> periods) {
        dereferenceValue = new HashMap<Integer, String>();
        reportPeriodDates = new HashMap<Integer, Pair<Date, Date>>();
        reportPeriodYears = new HashMap<Integer, Integer>();
        reportPeriodIds = new HashSet<Integer>();
        for (ReportPeriod reportPeriod : periods) {
            reportPeriodIds.add(reportPeriod.getId());
            dereferenceValue.put(reportPeriod.getId(), reportPeriod.getName());
            reportPeriodDates.put(reportPeriod.getId(), new Pair<Date, Date>(reportPeriod.getStartDate(), reportPeriod.getEndDate()));
            reportPeriodYears.put(reportPeriod.getId(), reportPeriod.getTaxPeriod().getYear());
        }
        periodPicker.setPeriods(periods);
    }

    @Override
    public void setAcceptableValues(Collection<List<Integer>> values) {
        periodPicker.setAcceptableValues(values);
    }

    @Override
    public List<Integer> getValue() {
        Collections.sort(value);
        return new ArrayList<Integer>(value);
    }

    @Override
    public void setValue(List<Integer> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Integer> value, boolean fireEvents) {
        this.value.clear();
        if (value != null) {
            this.value.addAll(value);
            setDefaultReportPeriod(this.value);
        }
        dereference(this.value);
        if (fireEvents) {
            ValueChangeEvent.fire(this, this.value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Integer>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Pair<Date, Date> getPeriodDates(Integer reportPeriodId) {
        return reportPeriodDates.get(reportPeriodId);
    }

    @Override
    public String getText() {
        return text.getText();
    }

    @UiHandler("okButton")
    public void onOkButtonClick(ClickEvent event) {
        this.setValue(periodPicker.getValue(), true);
        popupPanel.hide();
    }

    @UiHandler("selectButton")
    public void onSelectClick(ClickEvent event) {
        if (isSetDefaultValue && (this.value == null || this.value.isEmpty())) {
            Integer reportPeriodId = getDefaultReportPeriod();
            if (reportPeriodId != null && reportPeriodIds.contains(reportPeriodId)) {
                periodPicker.setValue(Arrays.asList(reportPeriodId));
            } else if (reportPeriodDates != null && !reportPeriodDates.isEmpty()) {
                Map.Entry<Integer, Pair<Date, Date>>[] rpDates = (Map.Entry<Integer, Pair<Date, Date>>[])reportPeriodDates.entrySet().toArray();
                Date maxDate = rpDates[0].getValue().getSecond();
                Integer rpId = rpDates[0].getKey();
                for (Map.Entry<Integer, Pair<Date, Date>> per : rpDates) {
                    if (per.getValue().getSecond().after(maxDate)) {
                        rpId = per.getKey();
                    }
                }
                setValue(Arrays.asList(rpId));
            }
        } else {
            periodPicker.setValue(this.value);
        }
        popupPanel.center();
    }

    @UiHandler("clearButton")
    public void onClearClick(ClickEvent event) {
        this.setValue(null, true);
    }

    @Override
    public void setTaxType(String taxType) {
        // Операция не поддерживается. Пока не нужна была.
        throw new UnsupportedOperationException();
    }

    private void dereference(List<Integer> value) {
        valueDereference.clear();
        valueDereference = new ArrayList<String>();
        if (value != null && dereferenceValue != null) {
            for (Integer val : value) {
                String name = dereferenceValue.get(val);
                if (name != null) {
                    valueDereference.add(reportPeriodYears.get(val) + ":" + name);
                }
            }
        }
        String txt = TextUtils.joinListToString(valueDereference);
        this.text.setText(txt);
        this.text.setTitle(TextUtils.generateTextBoxTitle(txt));
    }

    @Override
    public TakesValueEditor<List<Integer>> asEditor() {
        if (editor == null) {
            editor = TakesValueEditor.of(this);
        }
        return editor;
    }

    @Override
    public void setWidth(String width) {
        wrappingPanel.setWidth(width);
    }

    @Override
    protected void updateLabelValue() {
        setLabelValue(TextUtils.joinListToString(valueDereference));
    }

    public Set<Integer> getReportPeriodIds() {
        return reportPeriodIds;
    }

    public Integer getDefaultReportPeriod() {
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            String value = storage.getItem("tax-reportPeriod_" + type);
            if (value != null && !"".equals(value)) {
                return Integer.valueOf(value);
            }
        }
        return null;
    }

    private void setDefaultReportPeriod(List<Integer> values) {
        if (type != null) {
            Integer value = null;
            if (values.size() > 0) {
                value = values.get(0);
                Date maxDate = reportPeriodDates.get(values.get(0)).getSecond();
                for (Integer v : values) {
                    if (reportPeriodDates.get(v).getSecond().after(maxDate)) {
                        value = v;
                        maxDate = reportPeriodDates.get(v).getSecond();
                    }
                }
            }
            if (value != null) {
                Storage storage = Storage.getLocalStorageIfSupported();
                if (storage != null) {
                    storage.setItem("tax-reportPeriod_" + type, String.valueOf(value));
                }
            }
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSetDefaultValue(boolean isSetDefaultValue) {
        this.isSetDefaultValue = isSetDefaultValue;
    }
}
