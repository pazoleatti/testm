package com.aplana.sbrf.taxaccounting.web.module.members.client;

import com.aplana.gwt.client.MultiListBox;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.FilterValues;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */
public class MembersView extends ViewWithUiHandlers<MembersUiHandlers> implements MembersPresenter.MyView {

    interface Binder extends UiBinder<Widget, MembersView> {
    }

    private final Widget widget;

	private AsyncDataProvider<TAUserFullWithDepartmentPath> dataProvider = new  AsyncDataProvider<TAUserFullWithDepartmentPath>() {
		@Override
		protected void onRangeChanged(HasData<TAUserFullWithDepartmentPath> display) {
			if (getUiHandlers() != null){
				Range range = display.getVisibleRange();
				getUiHandlers().onRangeChange(range.getStart(), range.getLength());
			}
		}
	};

	@UiField(provided = true)
	ValueListBox<Boolean> isActiveBox;

	@UiField
    DepartmentPickerPopupWidget departmentPicker;

	@UiField(provided = true)
    MultiListBox<TARole> roleBox;

    @UiField
    CellTable<TAUserFullWithDepartmentPath> taUserFullCellTable;

    @UiField
    Anchor printButton;

	@UiField
	FlexiblePager pager;

	@UiField
	TextBox userName;

    @Inject
    public MembersView(Binder binder) {
	    isActiveBox = new ValueListBox<Boolean>(new AbstractRenderer<Boolean>() {
		    @Override
		    public String render(Boolean object) {
			    if (object == null) {
				    return "";
			    }
			    return object ? "Да" : "Нет";
		    }
	    });
	    roleBox = new MultiListBox<TARole>(new AbstractRenderer<TARole>() {
		    @Override
		    public String render(TARole object) {
			    return object == null ? "" : object.getName();
		    }
	    }, true, true);

        widget = binder.createAndBindUi(this);
	    taUserFullCellTable.addColumn(new TextColumn<TAUserFullWithDepartmentPath>() {
		    @Override
		    public String getValue(TAUserFullWithDepartmentPath object) {
			    return object.getUser().getName();
		    }
	    },"Полное имя пользователя");

        taUserFullCellTable.addColumn(new TextColumn<TAUserFullWithDepartmentPath>(){
            @Override
            public String getValue(TAUserFullWithDepartmentPath object) {
                return object.getUser().getLogin();
            }
        },"Логин");

	    taUserFullCellTable.addColumn(new TextColumn<TAUserFullWithDepartmentPath>() {
		    @Override
		    public String getValue(TAUserFullWithDepartmentPath object) {
			    return object.getUser().getEmail();
		    }
	    },"Электронная почта");

	    taUserFullCellTable.addColumn(new TextColumn<TAUserFullWithDepartmentPath>() {
		    @Override
		    public String getValue(TAUserFullWithDepartmentPath object) {
			    return object.getUser().isActive() ? "Да" : "Нет";
		    }
	    },"Признак активности");

        taUserFullCellTable.addColumn(new TextColumn<TAUserFullWithDepartmentPath>() {
            @Override
            public String getValue(TAUserFullWithDepartmentPath object) {
                return object.getFullDepartmentPath();
            }
        },"Подразделение");


        taUserFullCellTable.addColumn(new TextColumn<TAUserFullWithDepartmentPath>() {
            @Override
            public String getValue(TAUserFullWithDepartmentPath object) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < object.getUser().getRoles().size(); i++){
                    sb.append(object.getUser().getRoles().get(i).getName());
                    if(i != object.getUser().getRoles().size() - 1)
                        sb.append(", ");
                }
                return sb.toString();
            }
        },"Роль");


	    taUserFullCellTable.setPageSize(pager.getPageSize());
	    pager.setDisplay(taUserFullCellTable);
	    dataProvider.addDataDisplay(taUserFullCellTable);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setTaUserFullCellTable(PagingResult<TAUserFullWithDepartmentPath> userFullList, int startIndex) {
	    taUserFullCellTable.setRowCount(userFullList.getTotalCount());
        taUserFullCellTable.setRowData(startIndex, userFullList);


    }

	@Override
	public MembersFilterData getFilter() {
		MembersFilterData membersFilterData = new MembersFilterData();
		membersFilterData.setActive(isActiveBox.getValue());
		membersFilterData.setUserName(userName.getText());
		List<Integer> selectedRoleIds = new ArrayList<Integer>();
        for (TARole selectedRole : roleBox.getValue()) {
            selectedRoleIds.add(selectedRole.getId());
        }
		membersFilterData.setRoleIds(selectedRoleIds);
		Set<Integer> depIds = new HashSet<Integer>();
		for (DepartmentPair dep : departmentPicker.getDepartmentPairValues()) {
			depIds.add(dep.getDepartmentId());
		}
		membersFilterData.setDepartmentIds(depIds);
		return membersFilterData;
	}

	@UiHandler("printButton")
    public void onPrintButton(ClickEvent event){
        if(getUiHandlers() != null)
            getUiHandlers().onPrintClicked();
    }
	@UiHandler("applyButton")
	public void onApplyButton(ClickEvent event){
		if(getUiHandlers() != null) {
            getUiHandlers().applyFilter();
		}
	}

	@Override
	public void updateData() {
		taUserFullCellTable.setVisibleRangeAndClearData(taUserFullCellTable.getVisibleRange(), true);
	}

	@Override
	public void updateData(int pageNumber) {
		if (pager.getPage() == pageNumber){
			updateData();
		} else {
			pager.setPage(pageNumber);
		}
	}

	@Override
	public void setFilterData(FilterValues values) {
		isActiveBox.setAcceptableValues(Arrays.asList(new Boolean[]{Boolean.TRUE, Boolean.FALSE}));
        roleBox.setAvailableValues(values.getRoles());
		departmentPicker.setAvalibleValues(values.getDepartments(), null);
	}

	@Override
	public void getBlobFromServer(String uuid) {
		Window.open(GWT.getHostPageBaseURL() + "download/downloadBlobController/processLogDownload/" + uuid, "", "");
	}
}
