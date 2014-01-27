package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.formDestinationsDialog;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FormDestinationsView extends PopupViewWithUiHandlers<FormDestinationsUiHandlers>
        implements FormDestinationsPresenter.MyView,Editor<FormDataFilter> {

    public interface Binder extends UiBinder<PopupPanel, FormDestinationsView> {
	}

    @UiField(provided = true)
    ValueListBox<FormDataKind> formDataKind;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> formTypeId;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    DepartmentPickerPopupWidget performersPickerWidget;

    private Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();

	@Inject
	public FormDestinationsView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

        formDataKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
            @Override
            public String render(FormDataKind object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        formTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return formTypesMap.get(object);
            }
        });

		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("continueButton")
	public void onSave(ClickEvent event){
		getUiHandlers().onConfirm();
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		hide();
	}

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartment) {
        departmentPicker.setAvalibleValues(departments, availableDepartment);
    }

    @Override
    public void setFormDataKinds(List<FormDataKind> formDataKinds) {
        formDataKind.setAcceptableValues(formDataKinds);
    }

    @Override
    public void setFormTypesMap(List<FormType> formTypes){
        formTypesMap.clear();
        for (FormType formType : formTypes) {
            formTypesMap.put(formType.getId(), formType.getName());
        }

        formTypeId.setValue(null);
        formTypeId.setAcceptableValues(formTypesMap.keySet());
    }

    @Override
    public void setPerformers(List<Department> performers, Set<Integer> availablePerformers) {
        performersPickerWidget.setAvalibleValues(performers, availablePerformers);
    }

    @Override
    public void resetForm(){
        performersPickerWidget.setValue(null);
        departmentPicker.setValue(null);
        formTypeId.setValue(null);
        formDataKind.setValue(null);
    }
}
