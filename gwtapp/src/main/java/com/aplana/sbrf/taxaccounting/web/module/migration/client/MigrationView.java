package com.aplana.sbrf.taxaccounting.web.module.migration.client;

import com.aplana.gwt.client.MultiListBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

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

    @UiField(provided = true)
    MultiListBox<Long> years;

    @UiField(provided = true)
    MultiListBox<Long> rnus;

    @Inject
    @UiConstructor
    public MigrationView(final Binder uiBinder) {
        years = new MultiListBox<Long>(new AbstractRenderer<Long>() {
            @Override
            public String render(Long object) {
                return object == null ? "" : object.toString();
            }
        }, true, true);

        rnus = new MultiListBox<Long>(new AbstractRenderer<Long>() {
            @Override
            public String render(Long object) {
                return object == null ? "" : object.toString();
            }
        }, true, true);

        initWidget(uiBinder.createAndBindUi(this));

        years.setAvailableValues(yearList);
        years.setValue(yearList);

        rnus.setAvailableValues(rnuList);
        rnus.setValue(rnuList);

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
        return rnus.getValue();
    }

    @Override
    public List<Long> getYears() {
        return years.getValue();
    }
}