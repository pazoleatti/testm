package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeclarationTemplateSubreportView extends ViewWithUiHandlers<DeclarationTemplateSubreportUiHandlers>
		implements DeclarationTemplateSubreportPresenter.MyView {

    public interface Binder extends UiBinder<Widget, DeclarationTemplateSubreportView> { }

	@UiField
	Button upColumn, downColumn, addColumn, removeColumn;

    @UiField
    ListBox subreportListBox;

    @UiField
    Panel attrPanel;

    @UiField
    TextBox nameBox, aliasBox;

    @UiField
    CheckBox selectRecordBox;

    @UiField
    LinkAnchor file;

    @UiField
    FormPanel formPanel;

    @UiField
    FileUpload uploader;

    @UiField
    LinkAnchor downloadFile;
    @UiField
    LinkButton deleteFile;

    @UiField
    Panel paramPanel;

    @UiField
    ListBox subreportParamListBox;

    @UiField
    TextBox nameParamBox, aliasParamBox;

    @UiField
    CheckBox requiredCheckBox;

    @UiField(provided = true)
    ValueListBox<String> typeColumnDropBox;

    @UiField
    HorizontalPanel refBookPanel, refBookAttrPanel, refBookAttrFilterPanel;

    @UiField(provided = true)
    ValueListBox<RefBook> refBookBox;

    @UiField(provided = true)
    ValueListBox<RefBookAttribute> refBookAttrBox;

    @UiField
    TextArea refBookAttrFilterArea;

    private List<DeclarationSubreport> subreports;

    private static String actionUrl = "upload/uploadController/pattern/";
    private static String respPattern = "(<pre.*?>|<PRE.*?>)(.+?)(</pre>|</PRE>)(.*)";
    // Типы Параметров
    private static final String STRING_TYPE = DeclarationSubreportParamType.STRING.getTitle();
    private static final String NUMERIC_TYPE = DeclarationSubreportParamType.NUMBER.getTitle();
    private static final String DATE_TYPE = DeclarationSubreportParamType.DATE.getTitle();
    private static final String REFBOOK_TYPE = DeclarationSubreportParamType.REFBOOK.getTitle();

    private static final List<String> COLUMN_TYPE_NAME_LIST = Arrays.asList(STRING_TYPE, NUMERIC_TYPE, DATE_TYPE,
            REFBOOK_TYPE);

    @Inject
	@UiConstructor
	public DeclarationTemplateSubreportView(Binder binder) {
        initField();
        initWidget(binder.createAndBindUi(this));
        init();
	}

    private void initField() {
        typeColumnDropBox = new ValueListBox<String>(new AbstractRenderer<String>() {
            @Override
            public String render(String object) {
                if (object == null) {
                    return "";
                }
                return object;
            }
        });
        typeColumnDropBox.addHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                DeclarationSubreportParam selectedSubreportParam = getSelectedSubreportParam();
                if (STRING_TYPE.equals(typeColumnDropBox.getValue())){
                    selectedSubreportParam.setType(DeclarationSubreportParamType.STRING);
                    selectedSubreportParam.setFilter(null);
                    selectedSubreportParam.setRefBookAttributeId(null);
                } else if (NUMERIC_TYPE.equals(typeColumnDropBox.getValue())){
                    selectedSubreportParam.setType(DeclarationSubreportParamType.NUMBER);
                    selectedSubreportParam.setFilter(null);
                    selectedSubreportParam.setRefBookAttributeId(null);
                } else if (DATE_TYPE.equals(typeColumnDropBox.getValue())){
                    selectedSubreportParam.setType(DeclarationSubreportParamType.DATE);
                    selectedSubreportParam.setFilter(null);
                    selectedSubreportParam.setRefBookAttributeId(null);
                } else if (REFBOOK_TYPE.equals(typeColumnDropBox.getValue())){
                    selectedSubreportParam.setType(DeclarationSubreportParamType.REFBOOK);
                }
                setUniqueParameters();
                setSubreportAttributeEditor(subreportParamListBox.getSelectedIndex());
            }
        }, ValueChangeEvent.getType());

        // Справочники
        refBookBox = new ValueListBox<RefBook>(new AbstractRenderer<RefBook>() {
            @Override
            public String render(RefBook refBook) {
                return refBook == null ? "" : refBook.getName();
            }
        });

        // Атрибуты справочника
        refBookAttrBox = new ValueListBox<RefBookAttribute>(new AbstractRenderer<RefBookAttribute>() {
            @Override
            public String render(RefBookAttribute refBookAttribute) {
                return refBookAttribute == null ? "" : refBookAttribute.getName();
            }
        });
    }

	private void init() {
        formPanel.setAction(GWT.getHostPageBaseURL() + actionUrl);

        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String result = event.getResults().replaceAll(respPattern, "$2");
                JSONObject answer = JSONParser.parseLenient(result).isObject();
                if (answer.get(UuidEnum.UUID.toString()) != null) {
                    subreports.get(subreportListBox.getSelectedIndex()).setBlobDataId(answer.get(UuidEnum.UUID.toString()).isString().stringValue());
                    setUniqueParameters();
                }
                String uuid = null;
                boolean isErrors = false;
                if (answer.get(UuidEnum.SUCCESS_UUID.toString()) != null) {
                    uuid = answer.get(UuidEnum.SUCCESS_UUID.toString()).isString().stringValue();
                } else if (answer.get(UuidEnum.ERROR_UUID.toString()) != null) {
                    uuid = answer.get(UuidEnum.ERROR_UUID.toString()).isString().stringValue();
                    isErrors = true;
                }
                getUiHandlers().onEndLoad(new EndLoadFileEvent(uuid, isErrors));
                formPanel.reset();
            }
        });

        uploader.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                getUiHandlers().onStartLoad(new StartLoadFileEvent(uploader.getFilename()));
                formPanel.submit();
            }
        });
    }

    @UiHandler("file")
    void onUploadClick(ClickEvent event) {
        uploader.getElement().<InputElement>cast().click();
    }

    @UiHandler("downloadFile")
    void onDownloadClick(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().downloadFile();
            onSubreportChanged();
        }
    }

    @UiHandler("deleteFile")
    void onDeleteFile(ClickEvent event) {
        subreports.get(subreportListBox.getSelectedIndex()).setBlobDataId(null);
        downloadFile.setEnabled(false);
        deleteFile.setEnabled(false);
        onSubreportChanged();
    }

    @UiHandler("nameBox")
    public void onNameTextBoxKeyPressed(KeyUpEvent event){
        changeNameColumn();
        onSubreportChanged();
    }
    @UiHandler("nameBox")
    public void onNameTextBoxClicked(ClickEvent event){
        changeNameColumn();
    }
    private void changeNameColumn() {
        int index = subreportListBox.getSelectedIndex();
        DeclarationSubreport column = subreports.get(index);
        column.setName(nameBox.getValue());
        setColumnList();
        subreportListBox.setSelectedIndex(index);
    }

    @UiHandler("aliasBox")
    public void onAliasTextBoxKeyPressed(KeyUpEvent event){
        changeAliasColumn();
        onSubreportChanged();
    }
    @UiHandler("aliasBox")
    public void onAliasTextBoxClicked(ClickEvent event){
        changeAliasColumn();
    }
    private void changeAliasColumn() {
        int index = subreportListBox.getSelectedIndex();
        DeclarationSubreport column = subreports.get(index);
        column.setAlias(aliasBox.getValue());
    }

    @UiHandler("selectRecordBox")
    public void onSelectRecordBoxKeyPressed(KeyUpEvent event){
        changeSelectRecordColumn();
        onSubreportChanged();
    }
    @UiHandler("selectRecordBox")
    public void onSelectRecordBoxClicked(ClickEvent event){
        changeSelectRecordColumn();
    }
    private void changeSelectRecordColumn() {
        int index = subreportListBox.getSelectedIndex();
        DeclarationSubreport column = subreports.get(index);
        column.setSelectRecord(selectRecordBox.getValue());
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
            setUniqueParameters();
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
    }

    @Override
    public final void flush() {
    }

    @UiHandler("subreportListBox")
    public void onSelectSubreport(ChangeEvent event){
        setSubreportAttributeEditor(subreportListBox.getSelectedIndex());
        setUniqueParameters();
    }

    private void setUniqueParameters() {
        DeclarationSubreport subreport = subreports.get(subreportListBox.getSelectedIndex());
        nameBox.setValue(subreport.getName());
        aliasBox.setValue(subreport.getAlias());
        selectRecordBox.setValue(subreport.isSelectRecord());
        if (subreport.getBlobDataId() != null) {
            downloadFile.setEnabled(true);
            deleteFile.setEnabled(true);
        } else {
            downloadFile.setEnabled(false);
            deleteFile.setEnabled(false);
        }
        if (subreportParamListBox.getSelectedIndex() >= 0) {
            setupParams(subreportParamListBox.getSelectedIndex());
        } else {
            setupParams(0);
        }
    }

    void onSubreportChanged(){
        if (getUiHandlers() != null) {
            getUiHandlers().onSubreportChanged();
        }
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
                onSubreportChanged();
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
                onSubreportChanged();
            }
        }
    }

    @UiHandler("addColumn")
    public void onAddColumn(ClickEvent event){
        DeclarationSubreport newSubreport = new DeclarationSubreport();
        newSubreport.setName("Новый отчет");
        newSubreport.setAlias("псевдоним");
        newSubreport.setOrder(subreports.size() + 1);
        newSubreport.setDeclarationSubreportParams(new ArrayList<DeclarationSubreportParam>());
        getUiHandlers().addSubreport(newSubreport);
        setupColumns(subreports.size() - 1);
        onSubreportChanged();
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
        onSubreportChanged();
    }


    @Override
    public DeclarationSubreport getSelectedSubreport() {
        int ind = subreportListBox.getSelectedIndex();
        DeclarationSubreport subreport = subreports.get(ind);
        return subreport;
    }

    public DeclarationSubreportParam getSelectedSubreportParam() {
        int ind = subreportParamListBox.getSelectedIndex();
        return getSelectedSubreport().getDeclarationSubreportParams().get(ind);
    }

    private void setupParams(int index) {
        setParamList();
        setParamsPanel();
        if (subreports != null && !subreports.isEmpty() && !getSelectedSubreport().getDeclarationSubreportParams().isEmpty()) {
            setSubreportAttributeEditor(index);
            subreportParamListBox.setSelectedIndex(index);
            setUniqueParameters2();
        }
    }

    private void setParamsPanel() {
        paramPanel.setVisible(subreports != null && !subreports.isEmpty() && !getSelectedSubreport().getDeclarationSubreportParams().isEmpty());
    }

    private void setParamList() {
        if (subreports != null && !subreports.isEmpty()) {
            subreportParamListBox.clear();
            for (DeclarationSubreportParam subreportParam: getSelectedSubreport().getDeclarationSubreportParams()) {
                if (subreportParam.getOrder() < 10) {
                    subreportParamListBox.addItem("0" + subreportParam.getOrder() + " " + subreportParam.getName());
                } else {
                    subreportParamListBox.addItem(subreportParam.getOrder() + " " + subreportParam.getName());
                }
            }
        }
    }

    private void setUniqueParameters2() {
        DeclarationSubreportParam subreportParam = getSelectedSubreportParam();
        switch (subreportParam.getType()) {
            case STRING:
                typeColumnDropBox.setValue(STRING_TYPE);
                break;
            case NUMBER:
                typeColumnDropBox.setValue(NUMERIC_TYPE);
                break;
            case DATE:
                typeColumnDropBox.setValue(DATE_TYPE);
                break;
            case REFBOOK:
                typeColumnDropBox.setValue(REFBOOK_TYPE);
                break;
            default:
                throw new IllegalStateException();
        }
        populateUniqueParameters();
        typeColumnDropBox.setAcceptableValues(COLUMN_TYPE_NAME_LIST);
    }

    private void populateUniqueParameters() {
        DeclarationSubreportParam subreportParam = getSelectedSubreportParam();
        nameParamBox.setValue(subreportParam.getName());
        aliasParamBox.setValue(subreportParam.getAlias());
        requiredCheckBox.setValue(subreportParam.isRequired());
        if (subreportParam.getType().equals(DeclarationSubreportParamType.REFBOOK)) {
            refBookPanel.setVisible(true);
            refBookAttrPanel.setVisible(true);
            refBookAttrFilterPanel.setVisible(true);
            refBookBox.setValue(getUiHandlers().getRefBookByAttributeId(subreportParam.getRefBookAttributeId()));
            refBookAttrBox.setValue(getUiHandlers().getRefBookAttributeAttributeId(subreportParam.getRefBookAttributeId()));
            refBookAttrFilterArea.setValue(subreportParam.getFilter());
        } else {
            refBookPanel.setVisible(false);
            refBookAttrPanel.setVisible(false);
            refBookAttrFilterPanel.setVisible(false);
        }
    }

    @UiHandler("subreportParamListBox")
    public void onSelectColumn(ChangeEvent event){
        setSubreportAttributeEditor(subreportParamListBox.getSelectedIndex());
        setUniqueParameters2();
    }

    @UiHandler("upParam")
    public void onUpParam(ClickEvent event){
        int ind = subreportParamListBox.getSelectedIndex();
        DeclarationSubreportParam subreportParam = getSelectedSubreportParam();

        if (subreportParam != null) {
            if (ind > 0) {
                List<DeclarationSubreportParam> declarationSubreportParams = getSelectedSubreport().getDeclarationSubreportParams();
                DeclarationSubreportParam exchange = declarationSubreportParams.get(ind - 1);
                exchange.setOrder(ind + 1);
                subreportParam.setOrder(ind);
                declarationSubreportParams.set(ind - 1, subreportParam);
                declarationSubreportParams.set(ind, exchange);
                setParamList();
                subreportParamListBox.setSelectedIndex(ind - 1);
                onSubreportChanged();
            }
        }
    }

    @UiHandler("downParam")
    public void onDownParam(ClickEvent event){
        int ind = subreportParamListBox.getSelectedIndex();
        DeclarationSubreportParam subreportParam = getSelectedSubreportParam();

        if (subreportParam != null) {
            List<DeclarationSubreportParam> declarationSubreportParams = getSelectedSubreport().getDeclarationSubreportParams();
            if (ind < declarationSubreportParams.size() - 1) {
                DeclarationSubreportParam exchange = declarationSubreportParams.get(ind + 1);
                exchange.setOrder(ind + 1);
                subreportParam.setOrder(ind + 2);
                declarationSubreportParams.set(ind + 1, subreportParam);
                declarationSubreportParams.set(ind, exchange);
                setParamList();
                subreportParamListBox.setSelectedIndex(ind + 1);
                onSubreportChanged();
            }
        }
    }

    @UiHandler("addParam")
    public void onAddParam(ClickEvent event){
        List<DeclarationSubreportParam> declarationSubreportParams = getSelectedSubreport().getDeclarationSubreportParams();
        DeclarationSubreportParam newSubreportParam = new DeclarationSubreportParam();
        newSubreportParam.setName("Новый отчет");
        newSubreportParam.setAlias("псевдоним");
        newSubreportParam.setOrder(declarationSubreportParams.size() + 1);
        newSubreportParam.setType(DeclarationSubreportParamType.STRING);
        declarationSubreportParams.add(newSubreportParam);
        setupParams(declarationSubreportParams.size() - 1);
        onSubreportChanged();
    }

    @UiHandler("removeParam")
    public void onRemoveParam(ClickEvent event){
        List<DeclarationSubreportParam> declarationSubreportParams = getSelectedSubreport().getDeclarationSubreportParams();

        int index = subreportParamListBox.getSelectedIndex();
        if (index < 0)
            return;

        declarationSubreportParams.remove(index);

        for (int i = index; i < declarationSubreportParams.size(); i++) {
            declarationSubreportParams.get(i).setOrder(declarationSubreportParams.get(i).getOrder() - 1);
        }

        if (index > 0) {
            setupParams(index - 1);
        } else {
            setupParams(0);
        }
        onSubreportChanged();
    }

    @UiHandler("nameParamBox")
    public void onNameParamTextBoxKeyPressed(KeyUpEvent event){
        changeNameParam();
        onSubreportChanged();
    }
    @UiHandler("nameParamBox")
    public void onNameParamTextBoxClicked(ClickEvent event){
        changeNameParam();
    }
    private void changeNameParam() {
        int index = subreportParamListBox.getSelectedIndex();
        DeclarationSubreportParam selectedSubreportParam = getSelectedSubreportParam();
        selectedSubreportParam.setName(nameParamBox.getValue());
        setParamList();
        subreportParamListBox.setSelectedIndex(index);
    }

    @UiHandler("aliasParamBox")
    public void onAliasParamTextBoxKeyPressed(KeyUpEvent event){
        changeAliasParam();
        onSubreportChanged();
    }
    @UiHandler("aliasParamBox")
    public void onAliasParamTextBoxClicked(ClickEvent event){
        changeAliasParam();
    }
    private void changeAliasParam() {
        DeclarationSubreportParam selectedSubreportParam = getSelectedSubreportParam();
        selectedSubreportParam.setAlias(aliasParamBox.getValue());
    }

    @UiHandler("requiredCheckBox")
    public void onRequiredCheckBoxKeyPressed(KeyUpEvent event){
        changeRequiredCheck();
        onSubreportChanged();
    }
    @UiHandler("requiredCheckBox")
    public void onRequiredCheckBoxClicked(ClickEvent event){
        changeRequiredCheck();
    }
    private void changeRequiredCheck() {
        DeclarationSubreportParam selectedSubreportParam = getSelectedSubreportParam();
        selectedSubreportParam.setRequired(requiredCheckBox.getValue());
    }

    @UiHandler("refBookBox")
    public void onRefBookBox(ValueChangeEvent<RefBook> event) {
        DeclarationSubreportParam selectedSubreportParam = getSelectedSubreportParam();
        selectedSubreportParam.setRefBookAttributeId(event.getValue().getAttributes().get(0).getId());
        refBookAttrBox.setValue(event.getValue().getAttributes().get(0));
        refBookAttrBox.setAcceptableValues(event.getValue().getAttributes());
        onSubreportChanged();
    }

    @UiHandler("refBookAttrBox")
    public void onRefBookAttrBox(ValueChangeEvent<RefBookAttribute> event) {
        if (event.getValue()==null){
            return;
        }
        DeclarationSubreportParam selectedSubreportParam = getSelectedSubreportParam();
        selectedSubreportParam.setRefBookAttributeId(event.getValue().getId());
        onSubreportChanged();
    }

    @UiHandler("refBookAttrFilterArea")
    public void onRefBookAttrFilterArea(KeyUpEvent event) {
        DeclarationSubreportParam selectedSubreportParam = getSelectedSubreportParam();
        selectedSubreportParam.setFilter(refBookAttrFilterArea.getValue());
    }


    @Override
    public final void setRefBookList(List<RefBook> refBookList) {
        if (refBookList == null || refBookList.isEmpty())
            return;
        refBookBox.setValue(refBookList.get(0));
        refBookBox.setAcceptableValues(refBookList);
    }
}
