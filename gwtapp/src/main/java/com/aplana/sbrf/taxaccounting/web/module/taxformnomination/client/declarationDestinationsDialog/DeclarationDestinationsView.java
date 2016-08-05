package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

/**
 * @author auldanov
 */
public class DeclarationDestinationsView extends PopupViewWithUiHandlers<DeclarationDestinationsUiHandlers>
        implements DeclarationDestinationsPresenter.MyView, Editor<FormDataFilter>{

	public interface Binder extends UiBinder<PopupPanel, DeclarationDestinationsView> {
    }

    public static final String DECLARATION_TYPE_TITLE = "Вид декларации";
    public static final String DECLARATION_TYPE_TITLE_D = "Вид уведомления";
    public static final String MODAL_WINDOW_TITLE = "Создание назначения декларации";
    public static final String MODAL_WINDOW_TITLE_D = "Создание назначения уведомления";

    @UiField
    ModalWindow modalWindowTitle;

    @UiField
    @Ignore
    Label declarationTypeTitle;

    @UiField
    RefBookPickerWidget declarationTypeId;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    Button continueButton;

    @Inject
    public DeclarationDestinationsView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);

        initWidget(uiBinder.createAndBindUi(this));

        declarationTypeId.setPeriodDates(new Date(), new Date());

        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> listValueChangeEvent) {
                updateCreateButtonStatus();
            }
        });

        declarationTypeId.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                updateCreateButtonStatus();
            }
        });

        init();
    }

    @Override
    public void init() {
        departmentPicker.setValue(null);
        declarationTypeId.setValue(null);
    }

	@UiHandler("cancelButton")
	void onCancelButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCancel();
		}
	}

	@UiHandler("continueButton")
	void onContinueButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onConfirm();
		}
	}

	@Override
	public List<Integer> getSelectedDepartments() {
		return departmentPicker.getValue();
	}

	@Override
	public List<Long> getSelectedDeclarationTypes() {
		if (declarationTypeId.getValue() == null || declarationTypeId.getValue().isEmpty()) {
			return Collections.EMPTY_LIST;
		} else {
			return new ArrayList<Long>(declarationTypeId.getValue());
		}
	}

	@Override
	public void setDepartments(List<Department> departments, Set<Integer> availableValues) {
		departmentPicker.setAvalibleValues(departments, availableValues);
		departmentPicker.setValue(null);
	}

    @Override
    public void updateLabel(TaxType taxType) {
        if (!taxType.equals(TaxType.DEAL)) {
            declarationTypeTitle.setText(DECLARATION_TYPE_TITLE);
            modalWindowTitle.setTitle(MODAL_WINDOW_TITLE);
        } else {
            declarationTypeTitle.setText(DECLARATION_TYPE_TITLE_D);
            modalWindowTitle.setTitle(MODAL_WINDOW_TITLE_D);
        }
    }

    @Override
    public void updateCreateButtonStatus() {
        if (!getSelectedDepartments().isEmpty() && !getSelectedDeclarationTypes().isEmpty()){
            continueButton.setEnabled(true);
        } else {
            continueButton.setEnabled(false);
        }
    }

    @Override
    public void setDeclarationTypeFilter(TaxType taxType) {
        declarationTypeId.setValue(null);
        declarationTypeId.setDereferenceValue(null);

        if (taxType == null) {
            declarationTypeId.setFilter(null);
            return;
        }
        declarationTypeId.setFilter("TAX_TYPE like '" + taxType.getCode() + "'");
    }
}