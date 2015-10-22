package com.aplana.sbrf.taxaccounting.web.module.members.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.FilterValues;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
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

	@UiField(provided = true)
	ValueListBox<Boolean> isActiveBox;

	@UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    RefBookPickerWidget role;

    @UiField
    GenericDataGrid<TAUserView> taUserFullCellTable;

    @UiField
    Anchor printButton;

	@UiField
	FlexiblePager pager;

	@UiField
	TextBox userName;

    List<Long> defaultRoleIds = new ArrayList<Long>();

    private final Widget widget;
    private AsyncDataProvider<TAUserView> dataProvider = new  AsyncDataProvider<TAUserView>() {
        @Override
        protected void onRangeChanged(HasData<TAUserView> display) {
            if (getUiHandlers() != null){
                Range range = display.getVisibleRange();
                getUiHandlers().onRangeChange(range.getStart(), range.getLength());
            }
        }
    };

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

        widget = binder.createAndBindUi(this);

        // формирование колонок
        TextColumn<TAUserView> nameColumn = new TextColumn<TAUserView>() {
            @Override
            public String getValue(TAUserView object) {
                return object.getName();
            }
        };
        taUserFullCellTable.addResizableColumn(nameColumn, "Полное имя пользователя");
        taUserFullCellTable.setColumnWidth(nameColumn, 20, Style.Unit.PCT);
        nameColumn.setDataStoreName(MembersFilterData.SortField.NAME.name());

        TextColumn<TAUserView> loginColumn = new TextColumn<TAUserView>() {
            @Override
            public String getValue(TAUserView object) {
                return object.getLogin();
            }
        };
        taUserFullCellTable.addResizableColumn(loginColumn,"Логин");
        taUserFullCellTable.setColumnWidth(loginColumn, 150, Style.Unit.PX);
        loginColumn.setDataStoreName(MembersFilterData.SortField.LOGIN.name());

        TextColumn<TAUserView> mailColumn = new TextColumn<TAUserView>() {
            @Override
            public String getValue(TAUserView object) {
                return object.getEmail();
            }
        };
        taUserFullCellTable.addResizableColumn(mailColumn,"Электронная почта");
        taUserFullCellTable.setColumnWidth(mailColumn, 20, Style.Unit.PCT);
        mailColumn.setDataStoreName(MembersFilterData.SortField.MAIL.name());

        TextColumn<TAUserView> activeColumn = new TextColumn<TAUserView>() {
            @Override
            public String getValue(TAUserView object) { return object.getActive() ? "Да" : "Нет"; }
        };
        taUserFullCellTable.addResizableColumn(activeColumn,"Признак активности");
        taUserFullCellTable.setColumnWidth(activeColumn, 70, Style.Unit.PX);
        activeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        activeColumn.setDataStoreName(MembersFilterData.SortField.ACTIVE.name());

        TextColumn<TAUserView> depColumn = new TextColumn<TAUserView>() {
            @Override
            public String getValue(TAUserView object) {
                return object.getDepName();
            }
        };
        taUserFullCellTable.addResizableColumn(depColumn,"Подразделение");
        taUserFullCellTable.setColumnWidth(depColumn, 30, Style.Unit.PCT);
        depColumn.setDataStoreName(MembersFilterData.SortField.DEPARTMENT.name());

        TextColumn<TAUserView> roleColumn = new TextColumn<TAUserView>() {
            @Override
            public String getValue(TAUserView object) {
                return object.getRoles();
            }
        };
        taUserFullCellTable.addResizableColumn(roleColumn,"Роль");
        taUserFullCellTable.setColumnWidth(roleColumn, 30, Style.Unit.PCT);
        roleColumn.setDataStoreName(MembersFilterData.SortField.ROLE.name());

        for (int i = 0; i < taUserFullCellTable.getColumnCount(); i++) {
            taUserFullCellTable.getColumn(i).setSortable(true);
        }

	    taUserFullCellTable.setPageSize(pager.getPageSize());
	    pager.setDisplay(taUserFullCellTable);
	    dataProvider.addDataDisplay(taUserFullCellTable);
        taUserFullCellTable.getColumnSortList().setLimit(1);

        taUserFullCellTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(taUserFullCellTable));

	    Date current = new Date();
	    role.setPeriodDates(current, current);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setTaUserFullCellTable(PagingResult<TAUserView> userFullList, int startIndex) {
	    taUserFullCellTable.setRowCount(userFullList.getTotalCount());
        taUserFullCellTable.setRowData(startIndex, userFullList);
    }

	@Override
	public MembersFilterData getFilter() {
		MembersFilterData membersFilterData = new MembersFilterData();
		membersFilterData.setActive(isActiveBox.getValue());
		membersFilterData.setUserName(userName.getText());
		membersFilterData.setRoleIds(role.getValue()!= null && !role.getValue().isEmpty() ? role.getValue() : defaultRoleIds);
        if (departmentPicker.getValue().isEmpty()){
            membersFilterData.setDepartmentIds(new HashSet<Integer>(departmentPicker.getAvalibleValues()));
        } else{
            membersFilterData.setDepartmentIds(new HashSet<Integer>(departmentPicker.getValue()));
        }
        if (taUserFullCellTable.getColumnSortList().size() > 0) {
            ColumnSortList.ColumnSortInfo columnSortInfo = taUserFullCellTable.getColumnSortList().get(0);
            membersFilterData.setSortField(MembersFilterData.SortField.valueOf(columnSortInfo.getColumn().getDataStoreName()));
            membersFilterData.setAsc(columnSortInfo.isAscending());
        }

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

    @UiHandler("userName")
    public void onUserNamePressEnter(KeyPressEvent event){
        if(KeyCodes.KEY_ENTER == event.getUnicodeCharCode()){
            onApplyButton(null);
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
		isActiveBox.setAcceptableValues(Arrays.asList(Boolean.TRUE, Boolean.FALSE));
        Set<Integer> departmentIds = new HashSet<Integer>();
        for (Department d: values.getDepartments()){
            departmentIds.add(d.getId());
        }
        departmentPicker.setAvalibleValues(values.getDepartments(), departmentIds);
        if (values.getRoleFilter() != null) {
            role.setFilter(values.getRoleFilter());
        }
        for (TARole taRole : values.getRoles()) {
            defaultRoleIds.add((long) taRole.getId());
        }
    }

	@Override
	public void getBlobFromServer(String uuid) {
		Window.open(GWT.getHostPageBaseURL() + "download/downloadBlobController/processLogDownload/" + uuid, "", "");
	}
}
