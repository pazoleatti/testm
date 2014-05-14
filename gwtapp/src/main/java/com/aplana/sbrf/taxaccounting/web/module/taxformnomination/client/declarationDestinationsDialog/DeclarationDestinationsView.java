package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.gwt.client.ListBoxWithTooltip;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueListBox;
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

    @UiField(provided = true)
    ValueListBox<DeclarationType> declarationTypeId;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    Button continueButton;

    @Inject
    public DeclarationDestinationsView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);

        declarationTypeId = new ListBoxWithTooltip<DeclarationType>(new AbstractRenderer<DeclarationType>() {
            @Override
            public String render(DeclarationType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        initWidget(uiBinder.createAndBindUi(this));

        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> listValueChangeEvent) {
                updateCreateButtonStatus();
            }
        });

        declarationTypeId.addValueChangeHandler(new ValueChangeHandler<DeclarationType>() {
            @Override
            public void onValueChange(ValueChangeEvent<DeclarationType> declarationTypeValueChangeEvent) {
                updateCreateButtonStatus();
            }
        });
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
	public List<Integer> getSelectedDeclarationTypes() {
		if (declarationTypeId.getValue() == null) {
			return Collections.EMPTY_LIST;
		} else {
			return Arrays.asList(declarationTypeId.getValue().getId());
		}
	}

	@Override
	public void setDepartments(List<Department> departments, Set<Integer> availableValues) {
		departmentPicker.setAvalibleValues(departments, availableValues);
		departmentPicker.setValue(null);
	}

	@Override
	public void setDeclarationTypes(List<DeclarationType> declarationTypes) {
		declarationTypeId.setValue(null);
		declarationTypeId.setAcceptableValues(declarationTypes);
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


}
