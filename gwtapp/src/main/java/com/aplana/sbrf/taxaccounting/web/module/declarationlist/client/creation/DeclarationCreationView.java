package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;


public class DeclarationCreationView extends PopupViewWithUiHandlers<DeclarationCreationUiHandlers>
		implements DeclarationCreationPresenter.MyView {

	public interface Binder extends UiBinder<PopupPanel, DeclarationCreationView> {
	}

	private DeclarationDataFilter filter;
	private DeclarationDataFilterAvailableValues filterValues;

	@UiField
	PeriodPicker periodPicker;

	@UiField
	DepartmentPicker departmentPicker;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;

	@UiField(provided = true)
	ListBoxWithTooltip<DeclarationType> declarationType;

	@Inject
	public DeclarationCreationView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

		declarationType = new ListBoxWithTooltip<DeclarationType>(new AbstractRenderer<DeclarationType>() {
			@Override
			public String render(DeclarationType object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});
		
		initWidget(uiBinder.createAndBindUi(this));
		
	}

	@Override
	public void setDeclarationFilter(DeclarationDataFilter filter) {
		this.filter = filter;
		if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()){
			periodPicker.setValue(Arrays.asList(filter.getReportPeriodIds().iterator().next()));
		} else {
			periodPicker.setValue(null);
		}
		
	}

	@Override
	public void setDeclarationFilterValues(DeclarationDataFilterAvailableValues filterValues) {
		this.filterValues = filterValues;
		setDeclarationType();
	}

	private void setDeclarationType() {
		declarationType.setValue(null);
		declarationType.setAcceptableValues(filterValues.getDeclarationTypes());

		if (filter.getDeclarationTypeId() != null) {
			for (DeclarationType availableType : filterValues.getDeclarationTypes()) {
				if (availableType.getId() == filter.getDeclarationTypeId()) {
					declarationType.setValue(availableType);
					return;
				}
			}
		}
	}
	
	@Override
	public void setDepartments(List<Department> departments, Set<Integer> departmentsIds) {
		departmentPicker.setAvalibleValues(departments, departmentsIds);
		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
			Integer departmentId = filter.getDepartmentIds().iterator().next();
			for (Department department : departments) {
				if (department.getId() == departmentId) {
					departmentPicker.setValue(Arrays.asList(departmentId));
					return;
				}
			}
		}
	}

	@Override
	public void setSelectedReportPeriod(Integer reportPeriodId){
		periodPicker.setValue(Arrays.asList(reportPeriodId));
	}

	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		periodPicker.setPeriods(reportPeriods);
	}

	@Override
	public DeclarationDataFilter updateAndGetDeclarationFilter() {
		filter.setReportPeriodIds(periodPicker.getValue());
		if (departmentPicker.getValue() != null && departmentPicker.getValue().iterator().hasNext() ) {
			filter.setDepartmentIds(departmentPicker.getValue().isEmpty() ? new ArrayList<Integer>() :
					Arrays.asList(departmentPicker.
							getValue().iterator().next()));
		}
        filter.setDeclarationTypeId(declarationType.getValue()!= null?declarationType.getValue().getId() :
                null);
		return filter;
	}

    @UiHandler("continueButton")
	public void onContinue(ClickEvent event) {
		getUiHandlers().onContinue();
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		hide();
	}
}
