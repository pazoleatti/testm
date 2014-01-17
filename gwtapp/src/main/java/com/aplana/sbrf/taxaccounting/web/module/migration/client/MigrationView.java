package com.aplana.sbrf.taxaccounting.web.module.migration.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * View для формы "Миграция исторических данных"
 *
 * @author Dmitriy Levykin
 */
public class MigrationView extends ViewWithUiHandlers<MigrationUiHandlers>
        implements MigrationPresenter.MyView {

    public static List<Long> yearList = Arrays.asList(2008L, 2009L, 2010L, 2011L, 2012L, 2013L);
    public static List<Long> rnuList = Arrays.asList(25L, 26L, 27L, 31L, 51L, 53L, 54L, 59L, 60L, 64L);

    interface Binder extends UiBinder<Widget, MigrationView> {
    }

    @UiField
    TextArea textArea;

    @UiField
    Button loadButton;

    @UiField
    VerticalPanel years;
    @UiField
    VerticalPanel rnus;

    List<CheckBox> yearCheckBoxes = new ArrayList<CheckBox>();
    List<CheckBox> rnuCheckBoxes = new ArrayList<CheckBox>();

    @Inject
    @UiConstructor
    public MigrationView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        for (Long year : yearList) {
            CheckBox checkBox = new CheckBox(year.toString());
            checkBox.setValue(true);
            yearCheckBoxes.add(checkBox);
            years.add(checkBox);
        }

        for (Long rnu : rnuList) {
            CheckBox checkBox = new CheckBox(rnu.toString());
            checkBox.setValue(true);
            rnuCheckBoxes.add(checkBox);
            rnus.add(checkBox);
        }

        loadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().runImport();
            }
        });
    }

    @Override
    public void setResult(String result) {
        textArea.setValue(textArea.getValue().concat(result + "\n\n"));
        textArea.getElement().setScrollTop(textArea.getElement().getScrollHeight());
    }

    @Override
    public List<Long> getRnus() {
        return getChecked(rnuCheckBoxes);
    }

    @Override
    public List<Long> getYears() {
        return getChecked(yearCheckBoxes);
    }

    private List<Long> getChecked(List<CheckBox> checkBoxes) {
        List<Long> longs = new ArrayList<Long>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.getValue()) {
                longs.add(Long.parseLong(checkBox.getText()));
            }
        }
        return longs;
    }

}