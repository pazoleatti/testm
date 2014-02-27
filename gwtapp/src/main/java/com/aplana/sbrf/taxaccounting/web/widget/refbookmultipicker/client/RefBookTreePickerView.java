package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.LazyTree;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionEvent;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookRecordDereferenceValue;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookUiTreeItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * Представление для отображения иерархического справочника
 *
 * @author aivanov
 */
public class RefBookTreePickerView extends ViewWithUiHandlers<RefBookTreePickerUiHandlers>
        implements RefBookTreePickerPresenter.MyView, RefBookView {

    interface Binder extends UiBinder<Widget, RefBookTreePickerView> {
    }

    private static Binder binder = GWT.create(Binder.class);

    @UiField(provided = true)
    LazyTree<RefBookUiTreeItem> tree;

    private Set<Long> longList = new LinkedHashSet<Long>();

    private Boolean isEnabledFireChangeEvent = true;
    private Boolean multiSelect = false;

    public RefBookTreePickerView() {
        this(false);
    }

    public RefBookTreePickerView(final boolean multiSelect) {
        this.multiSelect = multiSelect;

        tree = new LazyTree<RefBookUiTreeItem>(multiSelect, RefBookPickerUtils.TREE_KEY_PROVIDER);

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
                onSelection();
            }
        });
    }

    private void onSelection() {
        if (!isEnabledFireChangeEvent) {
            isEnabledFireChangeEvent = true;
        } else {
            widgetFireChangeEvent(getSelectedIds());
        }
    }

    @Override
    public void loadRoot(List<RefBookTreeItem> values) {
        tree.clear();
        for (RefBookTreeItem value : values) {
            tree.addTreeItem(new RefBookUiTreeItem(value, multiSelect));
        }
    }

    @Override
    public void insertChildrens(RefBookUiTreeItem uiTreeItem, List<RefBookTreeItem> values) {
        for (RefBookTreeItem value : values) {
            tree.addTreeItem(uiTreeItem, new RefBookUiTreeItem(value, multiSelect));
        }
    }

    @Override
    public List<RefBookTreeItem> getSelectionValues() {
        List<RefBookTreeItem> refBookTreeItems = new ArrayList<RefBookTreeItem>();
        for (RefBookUiTreeItem uiItem : getSelectedSet()) {
            refBookTreeItems.add(uiItem.getRefBookTreeItem());
        }
        return refBookTreeItems;
    }

    @Override
    public void trySelectValues(Set<Long> value) {
    }

    @Override
    public void setSelection(List<RefBookTreeItem> values) {
        if (values != null) {
            if (!values.isEmpty()) {
                clearSelected(false);
                for (RefBookTreeItem item : values) {
                    tree.setSelected(new RefBookUiTreeItem(item, multiSelect), true);
                }
                widgetFireChangeEvent(getSelectedIds());
            }
        }
    }

    @Override
    public void load(PickerState pickerState) {
        getUiHandlers().init(pickerState);
    }

    @Override
    public void reload() {
        getUiHandlers().reload();
    }

    @Override
    public void find(String searchPattern) {
        getUiHandlers().find(searchPattern);
    }

    @Override
    public void reloadOnDate(Date version) {
        clearSelected(true);
        getUiHandlers().reloadForDate(version);
    }

    @Override
    public void clearSelected(boolean fireChangeEvent) {
        isEnabledFireChangeEvent = fireChangeEvent;
        longList.clear();
        tree.clearSelection();
        onSelection();
    }

    @Override
    public Set<Long> getSelectedIds() {
        longList.clear();
        for (RefBookUiTreeItem item : getSelectedSet()) {
            longList.add(item.getRefBookTreeItem().getId());
        }
        return longList;
    }

    private Set<RefBookUiTreeItem> getSelectedSet() {
        return tree.getSelectedItems();
    }

    @Override
    public String getDereferenceValue() {
        Set<RefBookUiTreeItem> selectedItems = getSelectedSet();
        StringBuilder sb = new StringBuilder();
        if (selectedItems.size() > 0) {
            for (RefBookUiTreeItem item : selectedItems) {
                sb.append(item.getRefBookTreeItem().getDereferenceValue());
                sb.append("; ");
            }
            sb.deleteCharAt(sb.length() - 2);
        }
        return sb.toString();
    }

    @Override
    public String getOtherDereferenceValue(String alias) {
        Set<RefBookUiTreeItem> selectedItems = getSelectedSet();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            List<RefBookRecordDereferenceValue> dereferenceValues =
                    selectedItems.iterator().next().getRefBookTreeItem().getRefBookRecordDereferenceValues();
            return RefBookPickerUtils.getDereferenceValue(dereferenceValues, alias);
        }
        return null;
    }

    @Override
    public String getOtherDereferenceValue(Long attrId) {
        Set<RefBookUiTreeItem> selectedItems = getSelectedSet();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            List<RefBookRecordDereferenceValue> dereferenceValues =
                    selectedItems.iterator().next().getRefBookTreeItem().getRefBookRecordDereferenceValues();
            return RefBookPickerUtils.getDereferenceValue(dereferenceValues, attrId);
        }
        return null;
    }

    @Override
    public Boolean isMultiSelect() {
        return multiSelect;
    }

    @Override
    public void setMultiSelect(Boolean multiSelect) {
        this.multiSelect = multiSelect;
        tree.setMultiSelect(this.multiSelect);
        widgetFireChangeEvent(getSelectedIds());
    }

    public void widgetFireChangeEvent(Set<Long> value) {
        ValueChangeEvent.fire(RefBookTreePickerView.this, value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<Long>> handler) {
        return asWidget().addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        asWidget().fireEvent(event);
    }
}