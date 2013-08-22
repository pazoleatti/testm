package com.aplana.sbrf.taxaccounting.migration.web.client.page;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Ivanov
 */
public class MainPageView extends ViewWithUiHandlers<MigrationUiHandlers> implements MainPagePresenter.MyView {

    @UiField
    CheckBox all;
    @UiField
    Button sendButton,
            clearButton;
    @UiField
    TextArea infoPanel;
    @UiField(provided = true)
    ListBox listRnu = new ListBox(true);

    interface Binder extends UiBinder<Widget, MainPageView> {
    }

    @Inject
    @UiConstructor
    public MainPageView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        listRnu.addItem("РНУ 25 (*.rnu)", "25");
        listRnu.addItem("РНУ 26 (*.rnu)", "26");
        listRnu.addItem("РНУ 27 (*.rnu)", "27");
        listRnu.addItem("РНУ 31 (*.rnu)", "31");
        listRnu.addItem("РНУ 51 (*.xml)", "51");
        listRnu.addItem("РНУ 53 (*.xml)", "53");
        listRnu.addItem("РНУ 54 (*.xml)", "54");
        listRnu.addItem("РНУ 59 (*.xml)", "59");
        listRnu.addItem("РНУ 60 (*.xml)", "60");
        listRnu.addItem("РНУ 64 (*.xml)", "64");
    }

    @UiHandler("sendButton")
    public void onStart(ClickEvent event) {
        if (listRnu.getSelectedIndex() != -1) {
            getUiHandlers().start(getSelectedList());
        } else {
            appendText("Не выделено ни одного вида рну");
        }
    }

    @UiHandler("all")
    public void onPickAll(ClickEvent event) {
        if (isAllSelected()) {
            listRnu.setSelectedIndex(-1);
        } else {
            for (int i = 0; i < listRnu.getItemCount(); i++) {
                if (!listRnu.isItemSelected(i)) {
                    listRnu.setItemSelected(i, true);
                }
            }
        }

    }

    @UiHandler("clearButton")
    public void onClear(ClickEvent event) {
        infoPanel.setText("");
    }

    @UiHandler("listRnu")
    public void onClickListBox(ClickEvent event) {
        all.setValue(false, true);
        if (isAllSelected()) {
            all.setValue(true, true);
        }
    }

    private boolean isAllSelected() {
        for (int i = 0; i < listRnu.getItemCount(); i++) {
            if (!listRnu.isItemSelected(i)) {
                return false;
            }
        }
        return true;
    }

    private List<Long> getSelectedList() {
        List<Long> rnuList = new ArrayList<Long>();
        for (int i = 0; i < listRnu.getItemCount(); i++) {
            if (listRnu.isItemSelected(i)) {
                rnuList.add(Long.valueOf(listRnu.getValue(i)));
            }
        }
        return rnuList;
    }

    @Override
    public void appendText(String msg) {
        infoPanel.setText(infoPanel.getText() + "\n\r" + msg);
    }

}