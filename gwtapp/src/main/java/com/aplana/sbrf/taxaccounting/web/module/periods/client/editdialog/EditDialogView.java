package com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog;

import com.aplana.gwt.client.Spinner;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;


public class EditDialogView extends PopupViewWithUiHandlers<EditDialogUiHandlers>
		implements EditDialogPresenter.MyView{

	public interface Binder extends UiBinder<PopupPanel, EditDialogView> {
	}

	@UiField
	DepartmentPickerPopupWidget departmentPicker;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;

	@UiField
	Spinner yearBox;

	@UiField
	CheckBox balancePeriod;

	@UiField
    RefBookPickerWidget period;

    @UiField
    Label periodLbl;

    @UiField
    Panel yearPnl;

	@Inject
	public EditDialogView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);
		initWidget(uiBinder.createAndBindUi(this));

	}

	@Override
	public void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable) {
		departmentPicker.setAvalibleValues(departments, avalDepartments);
		departmentPicker.setEnabled(enable);
	}

	@Override
	public void setYear(int year) {
		yearBox.setValue(year);
	}

	@Override
	public void setTaxType(TaxType taxType) {
		period.setFilter(taxType.getCode() + "=1");
	}

    @Override
    public void setSelectedDepartment(Integer departmentId){
        List<Integer> depId = new ArrayList<Integer>();
        depId.add(departmentId);
        departmentPicker.setValue(depId);
    }

	@UiHandler("continueButton")
	public void onContinue(ClickEvent event) {
        EditDialogData data = new EditDialogData();
        data.setBalance(balancePeriod.getValue());
        data.setYear(yearBox.getValue());
        data.setReportPeriodId(period.getSingleValue() == null ? null : period.getSingleValue().intValue());
        data.setDepartmentId(departmentPicker.getValue().isEmpty() ? null : departmentPicker.getValue().get(0));
        data.setDictTaxPeriodId(period.getSingleValue());
		getUiHandlers().onContinue(data);
    }

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
        Dialog.confirmMessage("Отмена операции редактирования периода", "Отменить операцию редактирования периода?", new DialogHandler() {
            @Override
            public void yes() {
                hide();
                super.yes();    //To change body of overridden methods use File | Settings | File Templates.
            }
        });
	}

	@Override
	public void setCanChangeDepartment(boolean canChange) {
		departmentPicker.setEnabled(canChange);
	}

    @Override
    public void setReportPeriod(long reportPeriodId, String periodName) {
        period.setPeriodDates(new Date(), new Date());
        period.setDereferenceValue(periodName);
        period.setSingleValue(reportPeriodId);

    }

    @Override
    public void setBalancePeriod(boolean isBalance) {
        balancePeriod.setValue(isBalance);
    }
}
