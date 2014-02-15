package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.LazyTree;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionEvent;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookUiTreeItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * @author aivanov
 */
public class RefBookTreePickerView extends ViewWithUiHandlers<RefBookTreePickerUiHandlers>
        implements RefBookTreePickerPresenter.MyView, RefBookView {

    interface Binder extends UiBinder<Widget, RefBookTreePickerView> {
    }

    private static Binder binder = GWT.create(Binder.class);

    @UiField(provided = true)
    LazyTree<RefBookUiTreeItem> tree;

    @UiField
    TextBox txtFind;

    @UiField
    DateMaskBoxPicker version;

    @UiField
    Button okButton;

    @UiField
    Label selectionCountLabel;

    private Boolean multiSelect = false;

    private Long refBookAttrId;
    private String filter;
    private Date startDate;
    private Date endDate;

    public RefBookTreePickerView() {
        this(false);
    }

    public RefBookTreePickerView(final boolean multiSelect) {
        this.multiSelect = multiSelect;

        tree = new LazyTree<RefBookUiTreeItem>(multiSelect);

        initWidget(binder.createAndBindUi(this));

        // подключаем презентер
        new RefBookTreePickerPresenter(this);

        tree.addOpenHandler(new OpenHandler<TreeItem>() {
            @Override
            public void onOpen(OpenEvent<TreeItem> event) {
                RefBookUiTreeItem refBookUiTreeItem = (RefBookUiTreeItem) event.getTarget();
                if (!refBookUiTreeItem.isChildLoaded()) {
                    getUiHandlers().loadForItem(refBookUiTreeItem);
                    refBookUiTreeItem.setChildLoaded(true);
                }
            }
        });

        tree.addLazyTreeSelectionHandler(new LazyTreeSelectionHandler<RefBookUiTreeItem>() {
            @Override
            public void onSelected(LazyTreeSelectionEvent<RefBookUiTreeItem> event) {
                updateCountLabel();
                okButton.setEnabled(!tree.getSelectedItems().isEmpty());
            }
        });

        version.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(final ValueChangeEvent<Date> dateValueChangeEvent) {
                Date d = dateValueChangeEvent.getValue();
                if (RefBookPickerUtils.isCorrectDate(startDate, endDate, d)) {
                    version.setValue(startDate, false);
                }
                getUiHandlers().reload();
            }
        });

        version.getDatePicker().addShowRangeHandler(new ShowRangeHandler<Date>() {
            @Override
            public void onShowRange(final ShowRangeEvent<Date> dateShowRangeEvent) {
                Date d = new Date(dateShowRangeEvent.getStart().getTime());
                while (d.before(dateShowRangeEvent.getEnd())) {
                    if (RefBookPickerUtils.isCorrectDate(startDate, endDate, d)) {
                        version.getDatePicker().setTransientEnabledOnDates(false, d);
                    }
                    CalendarUtil.addDaysToDate(d, 1);
                }
            }
        });

        txtFind.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    getUiHandlers().search();
                }
            }
        });
    }

    @Override
    public void loadRoot(List<RefBookTreeItem> values) {
        tree.clear();
        for (RefBookTreeItem value : values) {
            final RefBookUiTreeItem item = new RefBookUiTreeItem(value, multiSelect);
            tree.addTreeItem(item);
        }
    }

    @Override
    public void insertChildrens(RefBookUiTreeItem uiTreeItem, List<RefBookTreeItem> values) {
        for (RefBookTreeItem value : values) {
            final RefBookUiTreeItem item = new RefBookUiTreeItem(value, multiSelect);
            uiTreeItem.addItem(item);
        }
    }

    @UiHandler("clearButton")
    void onBtnClearClick(ClickEvent event) {
        clearSelected();
    }

    @UiHandler("searchButton")
    void onSearchButtonClick(ClickEvent event) {
        Dialog.infoMessage("Поиск по фильтру", "В разработке...");
        //getUiHandlers().search();
    }

    @UiHandler("okButton")
    void onBtnOkClick(ClickEvent event) {
        if (tree.getSelectedItems() != null && !tree.getSelectedItems().isEmpty()) {
            widgetFireChangeEvent(getSelectedLongs());
        }
    }

    @UiHandler("cancel")
    void onBtnCancelClick(ClickEvent event) {
        clearSelected();
        //TODO aivanov сделать нормальную отмену, а не как очистку.
    }

    public void focus() {
        txtFind.setFocus(true);
        txtFind.setCursorPos(txtFind.getText().length());
    }

    private void clearSelected() {

        txtFind.setValue("");
        tree.clearSelection();
        updateCountLabel();

        widgetFireChangeEvent(new ArrayList<Long>());
    }

    @Override
    public List<RefBookTreeItem> getSelectionValues() {
        List<RefBookTreeItem> refBookTreeItems = new ArrayList<RefBookTreeItem>();
        for (RefBookUiTreeItem uiItem : tree.getSelectedItems()) {
            refBookTreeItems.add(uiItem.getRefBookTreeItem());
        }
        return refBookTreeItems;
    }

    private List<Long> getSelectedLongs() {
        List<Long> longs = new ArrayList<Long>();
        if (tree.getSelectedItems() != null && !tree.getSelectedItems().isEmpty()) {
            List<RefBookTreeItem> selectionValues = getSelectionValues();
            for (RefBookTreeItem selectionValue : selectionValues) {
                longs.add(selectionValue.getId());
            }
        }
        return longs;
    }

    @Override
    public Long getSingleValue() {
        return (tree.getSelectedItems() != null && !tree.getSelectedItems().isEmpty() ? tree.getSelectedItems().get(0).getRefBookTreeItem().getId() : null);
    }

    @Override
    public List<Long> getValue() {
        return getSelectedLongs();
    }

    @Override
    public void setValue(Long value) {
        GWT.log("RefBookTreePicker can't setValue()");
        // заглушка
    }

    @Override
    public void setValue(List<Long> value) {
        GWT.log("RefBookTreePicker can't setValue()");
        // заглушка
    }

    @Override
    public void setValue(List<Long> value, boolean fireEvent) {
        GWT.log("RefBookTreePicker can't setValue()");
        // заглушка
    }

    @Override
    public String getSearchPattern() {
        return txtFind.getValue();
    }

    @Override
    public void setVersion(Date versionDate) {
        version.setValue(versionDate);
    }

    @Override
    public Date getVersion() {
        return version.getValue();
    }

    @Override
    public void load() {
        getUiHandlers().init(refBookAttrId, filter, version.getValue());
    }

    @Override
    public void load(long refBookAttrId, String filter, Date startDate, Date endDate) {
        this.refBookAttrId = refBookAttrId;
        this.filter = filter;
        this.startDate = startDate;
        this.endDate = endDate;
        if (!RefBookPickerUtils.isCorrectDate(startDate, endDate, version.getValue())) {
            this.version.setValue(endDate != null ? endDate : startDate);
        }
        getUiHandlers().init(refBookAttrId, filter, version.getValue());
    }

    @Override
    public String getDereferenceValue() {
        List<RefBookTreeItem> selectedItems = getSelectionValues();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedItems.size(); i++) {
            RefBookTreeItem item = selectedItems.get(i);
            sb.append(item.getDereferenceValue());
            if (i < selectedItems.size() - 1) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }

    @Override
    public String getOtherDereferenceValue(String alias) {
        List<RefBookTreeItem> selectedItems = getSelectionValues();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            return RefBookPickerUtils.getDereferenceValue(selectedItems.get(0).getRefBookRecordDereferenceValues(), alias);
        }
        return null;
    }

    @Override
    public String getOtherDereferenceValue(Long attrId) {
        List<RefBookTreeItem> selectedItems = getSelectionValues();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            return RefBookPickerUtils.getDereferenceValue(selectedItems.get(0).getRefBookRecordDereferenceValues(), attrId);
        }
        return null;
    }

    private void updateCountLabel() {
        selectionCountLabel.setText("Выбрано: " + tree.getSelectedItems().size());
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Long>> handler) {
        return asWidget().addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void widgetFireChangeEvent(List<Long> value) {
        ValueChangeEvent.fire(RefBookTreePickerView.this, value);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        asWidget().fireEvent(event);
    }

    @Override
    public Long getAttributeId() {
        return refBookAttrId;
    }

    @Override
    public void setAttributeId(long refBookAttrId) {
        this.refBookAttrId = refBookAttrId;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public void setPeriodDates(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }


    @Override
    public String getFilter() {
        return filter;
    }

    @Override
    public void setFilter(String filter) {
        this.filter = filter;
    }
}