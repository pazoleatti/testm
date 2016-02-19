package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.SubreportAttributeEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

public class DeclarationTemplateColumnView extends ViewWithUiHandlers<DeclarationTemplateColumnUiHandlers>
		implements DeclarationTemplateColumnPresenter.MyView {

    public interface Binder extends UiBinder<Widget, DeclarationTemplateColumnView> { }

	@UiField
	Button upColumn, downColumn, addColumn, removeColumn;

    @UiField
    ListBox subreportListBox;

    @UiField
    Panel attrPanel;

    @UiField
    SubreportAttributeEditor subreportAttributeEditor;

    List<DeclarationSubreport> subreports;

    @Inject
	@UiConstructor
	public DeclarationTemplateColumnView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
        init();
	}

	private void init() {
    }

    @Override
    public void setDeclarationTemplate(DeclarationTemplateExt declaration) {
        subreports = declaration.getDeclarationTemplate().getSubreports();
        if (subreportListBox.getSelectedIndex() >= 0) {
            setupColumns(subreportListBox.getSelectedIndex());
        } else {
            setupColumns(0);
        }
    }

    private void setupColumns(int index) {
        setColumnList();
        setAttributesPanel();
        if (subreports != null && !subreports.isEmpty()) {
            setSubreportAttributeEditor(index);
            subreportListBox.setSelectedIndex(index);
        }
    }

    private void setAttributesPanel() {
        attrPanel.setVisible(subreports != null && !subreports.isEmpty());
    }

    private void setColumnList() {
        if (subreports != null) {
            subreportListBox.clear();
            for (DeclarationSubreport subreport : subreports) {
                if (subreport.getOrder() < 10) {
                    subreportListBox.addItem("0" + subreport.getOrder() + " " + subreport.getName());
                } else {
                    subreportListBox.addItem(subreport.getOrder() + " " + subreport.getName());
                }
            }
        }
    }

    private void setSubreportAttributeEditor(int index) {
        flush();
        subreportAttributeEditor.setValue(subreports.get(index));
    }

    @Override
    public final void flush() {
        DeclarationSubreport subreport = subreportAttributeEditor.flush();
        if (subreport!=null){
            getUiHandlers().flushSubreport(subreport);
        }
    }

    @UiHandler("subreportListBox")
    public void onSelectColumn(ChangeEvent event){
        setSubreportAttributeEditor(subreportListBox.getSelectedIndex());
    }


    @UiHandler("upColumn")
    public void onUpColumn(ClickEvent event){
        int ind = subreportListBox.getSelectedIndex();
        DeclarationSubreport subreport = subreports.get(ind);
        flush();

        if (subreport != null) {
            if (ind > 0) {
                DeclarationSubreport exchange = subreports.get(ind - 1);
                exchange.setOrder(ind + 1);
                subreport.setOrder(ind);
                subreports.set(ind - 1, subreport);
                subreports.set(ind, exchange);
                setColumnList();
                subreportListBox.setSelectedIndex(ind - 1);
            }
        }
    }

    @UiHandler("downColumn")
    public void onDownColumn(ClickEvent event){
        int ind = subreportListBox.getSelectedIndex();
        DeclarationSubreport subreport = subreports.get(ind);
        flush();

        if (subreport != null) {
            if (ind < subreports.size() - 1) {
                DeclarationSubreport exchange = subreports.get(ind + 1);
                exchange.setOrder(ind + 1);
                subreport.setOrder(ind + 2);
                subreports.set(ind + 1, subreport);
                subreports.set(ind, exchange);
                setColumnList();
                subreportListBox.setSelectedIndex(ind + 1);
            }
        }
    }

    @UiHandler("addColumn")
    public void onAddColumn(ClickEvent event){
        DeclarationSubreport newSubreport = new DeclarationSubreport();
        newSubreport.setName("Новый отчет");
        newSubreport.setAlias("псевдоним");
        newSubreport.setOrder(subreports.size() + 1);
        newSubreport.setAlias("псевдоним");
        getUiHandlers().addSubreport(newSubreport);
        setupColumns(subreports.size() - 1);

    }

    @UiHandler("removeColumn")
    public void onRemoveColumn(ClickEvent event){
        int index = subreportListBox.getSelectedIndex();
        if (index < 0)
            return;

        getUiHandlers().removeSubreport(subreports.get(index));

        for (int i = index; i < subreports.size(); i++) {
            subreports.get(i).setOrder(subreports.get(i).getOrder() - 1);
        }

        if (index > 0) {
            setupColumns(index-1);
        } else {
            setupColumns(0);
        }
    }


}
