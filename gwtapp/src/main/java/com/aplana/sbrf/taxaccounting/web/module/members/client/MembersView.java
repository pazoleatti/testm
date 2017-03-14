package com.aplana.sbrf.taxaccounting.web.module.members.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.FilterValues;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
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

    @UiField
    HTML addButtonSeparator;

    @UiField
    Button cancelEdit;

    @UiField
    LinkButton edit, addButton;

    @UiField
	TextBox name, userLogin, userMail;

    @UiField
    CheckBox userActive;

    @UiField
    DepartmentPickerPopupWidget userDepartmentPicker;

    @UiField
    RefBookPickerWidget userRole, userAsnu;

    @UiField
    HorizontalPanel buttonBlock;

    @UiField
    Button save, cancel;

    public enum FormMode {
        EDIT, READ, CREATE
    }

    private List<Long> defaultRoleIds = new ArrayList<Long>();

    SingleSelectionModel<TAUserView> selectionModel = new SingleSelectionModel<TAUserView>();

    private FormMode mode;
    private boolean canEdit = false;

    private final Widget widget;

    private AsyncDataProvider<TAUserView> dataProvider = new AsyncDataProvider<TAUserView>() {
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
        taUserFullCellTable.setColumnWidth(roleColumn, 50, Style.Unit.PCT);
        roleColumn.setDataStoreName(MembersFilterData.SortField.ROLE.name());

        TextColumn<TAUserView> asnuColumn = new TextColumn<TAUserView>() {
            @Override
            public String getValue(TAUserView object) {
                return object.getAsnu();
            }
        };
        taUserFullCellTable.addResizableColumn(asnuColumn,"Наименование АСНУ");
        taUserFullCellTable.setColumnWidth(asnuColumn, 40, Style.Unit.PCT);
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
        userRole.setPeriodDates(current, current);
        userAsnu.setPeriodDates(current, current);

        taUserFullCellTable.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                getUiHandlers().setSelectUser(selectionModel.getSelectedObject());
                setUser(selectionModel.getSelectedObject());
            }
        });
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setTaUserFullCellTable(PagingResult<TAUserView> userFullList, int startIndex) {
	    taUserFullCellTable.setRowCount(userFullList.getTotalCount());
        taUserFullCellTable.setRowData(startIndex, userFullList);

        TAUserView selectedItem = getUiHandlers().getSelectUser();
        boolean selected = false;
        if (selectedItem != null) {
            for (TAUserView item : userFullList) {
                if (item.getLogin().equals(selectedItem.getLogin())) {
                    selectionModel.setSelected(item, true);
                    taUserFullCellTable.setKeyboardSelectedRow(taUserFullCellTable.getVisibleItems().indexOf(item));
                    selected = true;
                    break;
                }
            }
        }

        //Если не было среди записей необходимой, то выставляем на первую
        if (!selected) {
            if (!userFullList.isEmpty()) {
                selectionModel.setSelected(userFullList.get(0), true);
                taUserFullCellTable.setKeyboardSelectedRow(0);
            }
        }
    }

	@Override
	public MembersFilterData getFilter() {
		MembersFilterData membersFilterData = new MembersFilterData();
		membersFilterData.setActive(isActiveBox.getValue());
		membersFilterData.setUserName(userName.getText());
		membersFilterData.setRoleIds(role.getValue()!= null && !role.getValue().isEmpty() ? role.getValue() : defaultRoleIds);
        if (departmentPicker.getValue().isEmpty()) {
            membersFilterData.setDepartmentIds(new HashSet<Integer>(departmentPicker.getAvalibleValues()));
        } else {
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
    public void onPrintButton(ClickEvent event) {
        if(getUiHandlers() != null)
            getUiHandlers().onPrintClicked();
    }

	@UiHandler("addButton")
	public void onAddButton(ClickEvent event) {
        TAUserView userView = new TAUserView();
        userView.setDepId(null);
        userView.setTaRoleIds(new ArrayList<Long>());
        setNewUser(userView);
        setMode(FormMode.CREATE);
        getUiHandlers().setIsFormModified(true);
	}

    private void setNewUser(TAUserView taUserView) {
        name.setText(taUserView.getName());
        userLogin.setText(taUserView.getLogin());
        userMail.setText(taUserView.getEmail());
        userActive.setValue(taUserView.getActive());
        userDepartmentPicker.setValue(new ArrayList<Integer>());
        userRole.setValue(taUserView.getTaRoleIds());
        userAsnu.setValue(taUserView.getAsnuIds());
    }

    @UiHandler("applyButton")
	public void onApplyButton(ClickEvent event) {
		if (getUiHandlers() != null) {
            getUiHandlers().applyFilter();
		}
	}

    @UiHandler("userName")
    public void onUserNamePressEnter(KeyPressEvent event) {
        if (KeyCodes.KEY_ENTER == event.getUnicodeCharCode()){
            onApplyButton(null);
        }
    }

    @UiHandler("edit")
    public void onEditButton(ClickEvent event) {
        if (getUiHandlers() != null) {
            setMode(FormMode.EDIT);
        }
    }

    @UiHandler("cancelEdit")
    public void onCancelEditButton(ClickEvent event) {
        if (getUiHandlers() != null) {
            if (mode.equals(FormMode.CREATE) || getUiHandlers().isFormModified()) {
                Dialog.confirmMessage("Вы подтверждаете отмену изменений?", new DialogHandler() {
                    @Override
                    public void yes() {
                        getUiHandlers().setIsFormModified(false);
                        setUser(selectionModel.getSelectedObject());
                        setMode(FormMode.READ);
                    }

                    @Override
                    public void no() {
                        super.no();
                    }
                });
            } else {
                getUiHandlers().setIsFormModified(false);
                setUser(selectionModel.getSelectedObject());
                setMode(FormMode.READ);
            }
        }
    }

    @UiHandler("save")
    public void onSaveButton(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onSave();
        }
    }

    @UiHandler("cancel")
    public void onCancelButton(ClickEvent event) {
        if (mode.equals(FormMode.CREATE) || getUiHandlers().isFormModified()) {
            Dialog.confirmMessage("Вы подтверждаете отмену изменений?", new DialogHandler() {
                @Override
                public void yes() {
                    getUiHandlers().setIsFormModified(false);
                    setUser(selectionModel.getSelectedObject());
                    if (mode.equals(FormMode.CREATE)) {
                        setMode(FormMode.EDIT);
                    }
                }

                @Override
                public void no() {
                    super.no();
                }
            });
        } else {
            getUiHandlers().setIsFormModified(false);
            setUser(selectionModel.getSelectedObject());
            if (mode.equals(FormMode.CREATE)) {
                setMode(FormMode.EDIT);
            }
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
        userDepartmentPicker.setAvalibleValues(values.getUserDepartments(), values.getUserDepIds());
        if (values.getRoleFilter() != null) {
            role.setFilter(values.getRoleFilter());
            userRole.setFilter(values.getRoleFilter());
        }
        for (TARole taRole : values.getRoles()) {
            defaultRoleIds.add((long) taRole.getId());
        }
    }

	@Override
	public void getBlobFromServer(String uuid) {
		Window.open(GWT.getHostPageBaseURL() + "download/downloadBlobController/processLogDownload/" + uuid, "", "");
	}

    @Override
    public void setMode(FormMode mode) {
        this.mode = mode;
        switch (mode) {
            case EDIT:
                save.setVisible(true);
                cancel.setVisible(true);
                save.setEnabled(true);
                cancel.setEnabled(true);

                taUserFullCellTable.setEnabled(true);
                edit.setVisible(false);
                cancelEdit.setVisible(true);
                addButton.setVisible(true);
                addButtonSeparator.setVisible(true);

                buttonBlock.setVisible(true);

                name.setEnabled(true);
                userLogin.setEnabled(false);
                userMail.setEnabled(true);
                userActive.setEnabled(true);
                userDepartmentPicker.setEnabled(true);
                userRole.setEnabled(true);
                userAsnu.setEnabled(true);
                break;
            case CREATE:
                save.setVisible(true);
                cancel.setVisible(true);
                save.setEnabled(true);
                cancel.setEnabled(true);

                taUserFullCellTable.setEnabled(false);
                edit.setVisible(false);
                cancelEdit.setVisible(true);
                addButton.setVisible(true);
                addButtonSeparator.setVisible(true);

                buttonBlock.setVisible(true);

                name.setEnabled(true);
                userLogin.setEnabled(true);
                userMail.setEnabled(true);
                userActive.setEnabled(true);
                userDepartmentPicker.setEnabled(true);
                userRole.setEnabled(true);
                userAsnu.setEnabled(true);
                break;
            case READ:
                save.setVisible(false);
                cancel.setVisible(false);

                taUserFullCellTable.setEnabled(true);
                edit.setVisible(canEdit);
                cancelEdit.setVisible(false);
                addButton.setVisible(false);
                addButtonSeparator.setVisible(false);

                buttonBlock.setVisible(false);

                name.setEnabled(false);
                userLogin.setEnabled(false);
                userMail.setEnabled(false);
                userActive.setEnabled(false);
                userDepartmentPicker.setEnabled(false);
                userRole.setEnabled(false);
                userAsnu.setEnabled(false);
                break;
        }
    }

    public void setUser(TAUserView taUserView) {
        name.setText(taUserView.getName());
        userLogin.setText(taUserView.getLogin());
        userMail.setText(taUserView.getEmail());
        userActive.setValue(taUserView.getActive());
        userDepartmentPicker.setValue(Arrays.asList(taUserView.getDepId()));
        userRole.setValue(taUserView.getTaRoleIds());
        userAsnu.setValue(taUserView.getAsnuIds());
    }

    @Override
    public TAUserView getTAUserView() {
        TAUserView taUserView = new TAUserView();
        if (!FormMode.CREATE.equals(mode)) {
            taUserView.setId(selectionModel.getSelectedObject().getId());
        }
        taUserView.setName(name.getValue());
        taUserView.setLogin(userLogin.getValue());
        taUserView.setEmail(userMail.getValue());
        taUserView.setActive(userActive.getValue());
        taUserView.setDepId(userDepartmentPicker.getSingleValue());
        taUserView.setTaRoleIds(userRole.getValue());
        taUserView.setAsnuIds(userAsnu.getValue());
        return taUserView;
    }

    @Override
    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
}
