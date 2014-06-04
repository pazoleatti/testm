package com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;


public class EditCorrectionDialogView extends PopupViewWithUiHandlers<EditCorrectionDialogUiHandlers>
        implements EditCorrectionDialogPresenter.MyView{

    public interface Binder extends UiBinder<PopupPanel, EditCorrectionDialogView> {
    }

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    @UiField(provided = true)
    ListBoxWithTooltip<ReportPeriod> periodList;

    @UiField
    DateMaskBoxPicker term;

    @Inject
    public EditCorrectionDialogView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        periodList = new ListBoxWithTooltip<ReportPeriod>(new AbstractRenderer<ReportPeriod>() {
            @Override
            public String render(ReportPeriod object) {
                if (object == null) return "";
                return object.getName() + "(" + (1900 + object.getStartDate().getYear()) + ")";
            }
        });
        initWidget(uiBinder.createAndBindUi(this));



    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable) {
        departmentPicker.setAvalibleValues(departments, avalDepartments);
        departmentPicker.setEnabled(enable);
    }


    @Override
    public void setTaxType(TaxType taxType) {
//        period.setFilter(taxType.getCode() + "=1");
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
        data.setReportPeriodId(periodList.getValue() == null ? null : periodList.getValue().getId());
        data.setDepartmentId(departmentPicker.getValue().get(0));
        data.setCorrectionDate(term.getValue());
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
    public void setPeriodsList(List<ReportPeriod> reportPeriods) {
        periodList.setAcceptableValues(reportPeriods);
    }


}
