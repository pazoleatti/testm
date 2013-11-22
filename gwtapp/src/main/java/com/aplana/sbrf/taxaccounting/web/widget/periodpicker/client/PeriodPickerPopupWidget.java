package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

public class PeriodPickerPopupWidget extends Composite implements
		PeriodPickerPopup, IsEditor<TakesValueEditor<List<Integer>>>  {
	
	interface Binder extends UiBinder<Widget, PeriodPickerPopupWidget> {
	}
	private static Binder binder = GWT.create(Binder.class);

    @UiField
    FlowPanel wrappingPanel;

	@UiField(provided = true)
	PeriodPickerWidget periodPicker;
	
	@UiField
	Button selectButton;
	
	@UiField
	Button clearButton;
	
	@UiField
	PopupPanel popupPanel;
	
	@UiField
	Panel panel;
	
	@UiField
	HasText text;
	 
	
	private List<Integer> value;
	
	private TakesValueEditor<List<Integer>> editor;
	
	private Map<Integer, String> dereferenceValue;
	
	public PeriodPickerPopupWidget(){
		periodPicker = new PeriodPickerWidget();
		initWidget(binder.createAndBindUi(this));
	}
	
	@UiConstructor
	public PeriodPickerPopupWidget(boolean multiselect){
		periodPicker = new PeriodPickerWidget(multiselect);
		initWidget(binder.createAndBindUi(this));
	}

    @Override
    public void setPeriods(List<ReportPeriod> periods) {
        dereferenceValue = new HashMap<Integer, String>();
        for (ReportPeriod reportPeriod : periods) {
            dereferenceValue.put(reportPeriod.getId(), reportPeriod.getName());
        }
        periodPicker.setPeriods(periods);
    }

    @Override
    public void setAcceptableValues(Collection<List<Integer>> values) {
        periodPicker.setAcceptableValues(values);
    }

    @Override
    public List<Integer> getValue() {
        return this.value;
    }

    @Override
    public void setValue(List<Integer> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Integer> value, boolean fireEvents) {
        this.value = value;
        dereference(this.value);
        if (fireEvents){
            ValueChangeEvent.fire(this, this.value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<List<Integer>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public boolean isEnabled() {
        return selectButton.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        selectButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }

    @UiHandler("okButton")
    public void onOkButtonClick(ClickEvent event){
        this.setValue(periodPicker.getValue(), true);
        popupPanel.hide();
    }

    @UiHandler("selectButton")
    public void onSelectClick(ClickEvent event){
        periodPicker.setValue(this.value);
        popupPanel.setPopupPosition(panel.getAbsoluteLeft(),
                panel.getAbsoluteTop() + panel.getOffsetHeight());
        popupPanel.show();
    }

    @UiHandler("clearButton")
    public void onClearClick(ClickEvent event){
        this.setValue(null, true);
    }

    @Override
    public void setTaxType(String taxType) {
        // Операция не поддерживается. Пока не нужна была.
        throw new UnsupportedOperationException();
    }

    private void dereference(List<Integer> value){
        Collection<String> strings = new ArrayList<String>();
        if (value != null && dereferenceValue != null){
            for (Integer val : value) {
                String name = dereferenceValue.get(val);
                if (name != null){
                    strings.add(name);
                }
            }
        }
        String txt = joinListToString(strings);
        this.text.setText(txt);
        ((UIObject) this.text).setTitle(txt);
    }

    private String joinListToString(Collection<String> strings) {
        if ((strings == null) || strings.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (String name : strings) {
            text.append(name + "; ");
        }
        return text.toString();
    }

	@Override
	public TakesValueEditor<List<Integer>> asEditor() {
	    if (editor == null) {
	        editor = TakesValueEditor.of(this);
	    }
	    return editor;
	}

    public void setWidth(String width){
        wrappingPanel.setWidth(width);
    }
}
