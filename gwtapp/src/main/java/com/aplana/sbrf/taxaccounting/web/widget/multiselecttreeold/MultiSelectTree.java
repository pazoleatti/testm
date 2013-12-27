package com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

/**
 * Дерево с колонкой множественного выбора.
 */
public class MultiSelectTree extends Composite implements HasConstrainedValue<List<Integer>> {

    interface Binder extends UiBinder<Widget, MultiSelectTree> {
    }

    private static Binder binder = GWT.create(Binder.class);

    /** Заголовок. */
    @UiField
    Label label;

    @UiField
    ScrollPanel scrollPanel;

    /* Панель с чекбоксами. */
    @UiField
    VerticalPanel cbPanel;

    /** Дерево. */
    @UiField
    Tree tree;

    /** Мапа для чекбоксов. */
    private HashMap<Integer, CheckBox> cbHash = new HashMap<Integer, CheckBox>();

    private HashMap<Label, MultiSelectTreeItem> treeItemsHash = new HashMap<Label, MultiSelectTreeItem>();

    /** Дерево с колонкой множественного выбора. */
    public MultiSelectTree(String text) {
        initWidget(binder.createAndBindUi(this));

        setText(text);
        tree.addOpenHandler(new OpenHandler<TreeItem>() {
            @Override
            public void onOpen(OpenEvent<TreeItem> event) {
                updateCheckBoxPanel();
            }
        });

        tree.addCloseHandler(new CloseHandler<TreeItem>() {
            @Override
            public void onClose(CloseEvent<TreeItem> event) {
                updateCheckBoxPanel();
            }
        });
    }

    @Override
    public List<Integer> getValue() {
        List<Integer> result = new ArrayList<Integer>();
        for (MultiSelectTreeItem item : getItems()) {
            CheckBox cb = cbHash.get(item.getId());
            if (cb.getValue()) {
                result.add(item.getId());
            }
        }
        return result;
    }

    @Override
    public void setValue(List<Integer> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Integer> values, boolean fireEvents) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (MultiSelectTreeItem item : getItems()) {
            boolean isContain = values.contains(item.getId());
            if (isContain) {
                // раскрыть дерево до выбранного элемента
                TreeItem parent = item.getParentItem();
                while (parent != null) {
                    parent.setState(true);
                    parent = parent.getParentItem();
                }
            }
            CheckBox cb = cbHash.get(item.getId());
            cb.setValue(isContain);
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, values);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Integer>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void setAcceptableValues(Collection<List<Integer>> values) {
        // TODO (Ramil Timerbaev)
    }

    private Collection<MultiSelectTreeItem> getItems() {
        List<MultiSelectTreeItem> result = new ArrayList<MultiSelectTreeItem>();
        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem item = tree.getItem(i);
            findAllChild(result, (MultiSelectTreeItem) item);
        }
        return result;
    }

    /**
     * Заполнить дерево.
     *
     * @param items список данных
     */
    public void setTreeValues(List<MultiSelectTreeItem> items) {
        cbHash.clear();
        treeItemsHash.clear();
        // заполнение дерева и для каждого узла создание чекбоксов
        for (MultiSelectTreeItem item : items) {
            item.setState(true);
            this.tree.addItem(item);

            // получить все дочерние элементы узла и выделить для них checkBox'ы
            List<MultiSelectTreeItem> allChild = new ArrayList<MultiSelectTreeItem>();
            findAllChild(allChild, item);
            for (MultiSelectTreeItem i : allChild) {
                CheckBox cb = new CheckBox();
                cb.setTitle(i.getText());
                cbHash.put(i.getId(), cb);
                treeItemsHash.put((Label) i.getWidget(), i);

                i.addDoubleClickHandler(new DoubleClickHandler() {
                    @Override
                    public void onDoubleClick(DoubleClickEvent event) {
                        Label label = (Label) event.getSource();
                        MultiSelectTreeItem item = treeItemsHash.get(label);
                        if (item.getChildCount() > 0) {
                            item.setState(!item.getState());
                        }
                    }
                });

//                i.addClickHandler(new ClickHandler() {
//                    @Override
//                    public void onClick(ClickEvent event) {
//                        Window.alert(event.getSource().getClass().getName());
//                    }
//                });
            }
        }
        updateCheckBoxPanel();
    }

    /**
     * Найти все дочерние элементы узла. Поиск рекурсивный.
     *
     * @param list список дочерних элементов
     * @param item узел для которого ищутся дочерние
     */
    private void findAllChild(List<MultiSelectTreeItem> list, MultiSelectTreeItem item) {
        list.add(item);
        if (item.getChildCount() > 0) {
            for (int i = 0; i < item.getChildCount(); i++) {
                findAllChild(list, (MultiSelectTreeItem) item.getChild(i));
            }
        }
    }

    /**
     * Получить дочерние элементы узла.
     *
     * @param item узел для которого ищутся узлы
     * @return список дочерних элеметов
     */
    private List<MultiSelectTreeItem> getItemChild(MultiSelectTreeItem item) {
        List<MultiSelectTreeItem> list = new ArrayList<MultiSelectTreeItem>();
        if (item.getChildCount() > 0) {
            for (int i = 0; i < item.getChildCount(); i++) {
                list.add((MultiSelectTreeItem) item.getChild(i));
            }
        }
        return list;
    }

    /** Добавить checkBox в левую панель. */
    private void addCb(CheckBox cb) {
        cbPanel.add(cb);
        cb.getElement().getParentElement().getStyle().setPaddingTop(2, com.google.gwt.dom.client.Style.Unit.PX);
        cb.getElement().getParentElement().getStyle().setPaddingBottom(2, com.google.gwt.dom.client.Style.Unit.PX);
        // TODO (Ramil Timerbaev) почему то в коде стили не применяются
        // cb.addStyleName(style.msiCheckBox());
        // cb.getElement().getParentElement().addClassName(style.msiCheckBox());
    }

    /** Обновить левую панель (с чекбоксами). */
    private void updateCheckBoxPanel() {
        int count = tree.getItemCount();
        cbPanel.clear();
        for (int i = 0; i < count; i++) {
            MultiSelectTreeItem item = (MultiSelectTreeItem)tree.getItem(i);
            addCb(item);
        }
    }

    /** Добавить чекбоксы для дочерних элементов указанного узла. */
    private void addCb(MultiSelectTreeItem item) {
        addCb(cbHash.get(item.getId()));
        // если узел раскрыт, то добавить еще и дочерние элементы
        if (item.getState()) {
            List<MultiSelectTreeItem> list = getItemChild(item);
            for (MultiSelectTreeItem i : list) {
                addCb(i);
            }
        }
    }

    public String getText() {
        return label.getText();
    }

    public void setText(String text) {
        label.setText(text == null ? "" : text);
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        scrollPanel.setWidth(width);
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        scrollPanel.setHeight(height);
    }
}
