package com.aplana.sbrf.taxaccounting.web.module.userlist.client;

import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class UserListView extends ViewWithUiHandlers<UserListUiHandlers> implements UserListPresenter.MyView {

    interface Binder extends UiBinder<Widget, UserListView> {
    }

    private final Widget widget;

    @UiField
    CellTable<TAUserFull> taUserFullCellTable;

    @UiField
    Anchor printButton;

    @Inject
    public UserListView(Binder binder) {
        widget = binder.createAndBindUi(this);

        taUserFullCellTable.addColumn(new TextColumn<TAUserFull>(){
            @Override
            public String getValue(TAUserFull object) {
                return object.getUser().getLogin();
            }
        },"Логин");

        taUserFullCellTable.addColumn(new TextColumn<TAUserFull>() {
            @Override
            public String getValue(TAUserFull object) {
                return object.getUser().getName();
            }
        },"ФИО");

        taUserFullCellTable.addColumn(new TextColumn<TAUserFull>() {
            @Override
            public String getValue(TAUserFull object) {
                return object.getDepartment().getName();
            }
        },"Департамент");

        taUserFullCellTable.addColumn(new TextColumn<TAUserFull>() {
            @Override
            public String getValue(TAUserFull object) {
                return object.getUser().getEmail();
            }
        },"Почта");

        taUserFullCellTable.addColumn(new TextColumn<TAUserFull>() {
            @Override
            public String getValue(TAUserFull object) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < object.getUser().getRoles().size(); i++){
                    sb.append(object.getUser().getRoles().get(i).getName());
                    if(i != object.getUser().getRoles().size() - 1)
                        sb.append(", ");
                }
                return sb.toString();
            }
        },"Роли");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setTaUserFullCellTable(List<TAUserFull> userFullList) {
        taUserFullCellTable.setRowData(userFullList);
    }

    @UiHandler("printButton")
    public void onPrintButton(ClickEvent event){
        if(getUiHandlers() != null)
            getUiHandlers().onPrintClicked();
    }
}
