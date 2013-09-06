package com.aplana.sbrf.taxaccounting.web.module.migration.client;

import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationResult;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * View для формы "Миграция исторических данных"
 *
 * @author Dmitriy Levykin
 */
public class MigrationView extends ViewWithUiHandlers<MigrationUiHandlers>
        implements MigrationPresenter.MyView {

    interface Binder extends UiBinder<Widget, MigrationView> {
    }

    @UiField
    TextArea textArea;

    @UiField
    Button loadButton;

    @Inject
    @UiConstructor
    public MigrationView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        loadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                textArea.setValue(null);
                getUiHandlers().runImport();
            }
        });
    }

    @Override
    public void setResult(MigrationResult result) {
        if (result.getExemplarList() != null) {
            String msg = "Актуальных экземпляров найдено: " + result.getExemplarList().size()+"\n";
            msg += "Отправлено экземпляров: " + result.getSendFilesCount();
            textArea.setValue(msg);
        }
    }
}