package com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.Spinner;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.SourcesUtils;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.ValueBoxRenderer;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * Вью диалогового окна
 *
 * @author aivanov
 */
public class AssignDialogView extends ViewWithUiHandlers<AssignDialogUiHandlers>
        implements AssignDialogPresenter.MyView {

    public interface Binder extends UiBinder<ModalWindow, AssignDialogView> {
    }

    /**
     * 1.Назначить источники
     * 2.Назначить приемники
     * 5.Редактировать назначение
     */
    public enum State {
        CREATE_SOURCES,
        CREATE_RECEPIENTS,
        UPDATE
    }

    @UiField
    ModalWindow window;

    @UiField
    Label label;
    @UiField(provided = true)
    ValueListBox<PeriodInfo> periodFrom;
    @UiField
    Spinner yearFrom;
    @UiField(provided = true)
    ValueListBox<PeriodInfo> periodTo;
    @UiField
    Spinner yearTo;

    @UiField
    Button okButton;
    @UiField
    Button cancelButton;

    PeriodsInterval periodsInterval = new PeriodsInterval();

    ButtonClickHandlers buttonClickHandler;

    @Inject
    public AssignDialogView(Binder uiBinder) {
        ValueBoxRenderer<PeriodInfo> abstractRenderer = new ValueBoxRenderer<PeriodInfo>();
        periodFrom = new ValueListBox<PeriodInfo>(abstractRenderer, new ProvidesKey<PeriodInfo>() {
            @Override
            public Object getKey(PeriodInfo item) {
                return item != null ? item.getCode() : null;
            }
        });
        periodTo = new ValueListBox<PeriodInfo>(abstractRenderer, new ProvidesKey<PeriodInfo>() {
            @Override
            public Object getKey(PeriodInfo item) {
                return item != null ? item.getCode() : null;
            }
        });
        initWidget(uiBinder.createAndBindUi(this));

        periodFrom.addValueChangeHandler(new ValueChangeHandler<PeriodInfo>() {
            @Override
            public void onValueChange(ValueChangeEvent<PeriodInfo> event) {
                SourcesUtils.setupPeriodTitle((ValueListBox<PeriodInfo>) event.getSource());
            }
        });
        periodTo.addValueChangeHandler(new ValueChangeHandler<PeriodInfo>() {
            @Override
            public void onValueChange(ValueChangeEvent<PeriodInfo> event) {
                if (event.getValue() == null) {
                    yearTo.setValue(null);
                }
                SourcesUtils.setupPeriodTitle((ValueListBox<PeriodInfo>) event.getSource());
            }
        });

        yearFrom.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                yearFrom.setTitle(event.getValue() != null ? event.getValue() + " год" : "");
                if (event.getValue() != null)
                    yearTo.setMinValue(event.getValue());
                else
                    yearTo.setMinValue(1970);
            }
        });
        yearTo.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                yearTo.setTitle(yearTo.getValue() != null ? yearTo.getValue() + " год" : "");
            }
        });
    }

    @Override
    public void setAcceptablePeriods(List<PeriodInfo> periods) {
        periodFrom.setValue(periods.get(0));
        periodFrom.setAcceptableValues(periods);
        periodTo.setValue(null);
        periodTo.setAcceptableValues(periods);

        WidgetUtils.setupOptionTitle(periodFrom);
        WidgetUtils.setupOptionTitle(periodTo);
        periodTo.setValue(periods.get(periods.size() - 1));
    }

    @Override
    public void close() {
        window.hide();
    }

    @UiHandler("okButton")
    public void okClick(ClickEvent event) {
        PeriodsInterval pi = getPeriodsInterval();
        if (SourcesUtils.isCorrectPeriod(pi)) {
            buttonClickHandler.ok(pi);
            window.hide();
        } else {
            Dialog.errorMessage(window.getTitle(), "Неверно задан период!");
        }
    }

    @UiHandler("cancelButton")
    public void cancelClick(ClickEvent event) {
        Dialog.confirmMessage("Отменить редактирование?", new DialogHandler() {
            @Override
            public void yes() {
                buttonClickHandler.cancel();
                window.hide();
            }
        });
    }

    @Override
    public void open(State state, PeriodsInterval pi, ButtonClickHandlers buttonClickHandler) {
        this.buttonClickHandler = buttonClickHandler;
        setupView(state);
        setPeriodsInterval(pi);
        window.center();
    }

    public PeriodsInterval getPeriodsInterval() {
        periodsInterval.setPeriodFrom(periodFrom.getValue());
        periodsInterval.setPeriodTo(periodTo.getValue());
        periodsInterval.setYearFrom(yearFrom.getValue());
        periodsInterval.setYearTo(yearTo.getValue());

        return periodsInterval;
    }

    public void setPeriodsInterval(PeriodsInterval pi) {
        this.periodsInterval = pi;
        periodFrom.setValue(pi.getPeriodFrom());
        yearFrom.setValue(pi.getYearFrom());
        if (pi.getPeriodTo() != null) {
            periodTo.setValue(pi.getPeriodTo());
            yearTo.setValue(pi.getYearTo());
            yearTo.setTitle("");
        } else {
            periodTo.setValue(null);
            yearTo.setValue(null);
            yearTo.setTitle("");
            yearTo.setMinValue(pi.getYearFrom());
        }
    }

    private void setupView(State state) {
        switch (state) {
            case CREATE_SOURCES:
                window.setTitle("Назначение источников");
                label.setText("Создать назначение источников в периоде");
                okButton.setText("Создать");
                break;
            case CREATE_RECEPIENTS:
                window.setTitle("Назначение приемников");
                label.setText("Создать назначение приемников в периоде");
                okButton.setText("Создать");
                break;
            case UPDATE:
                window.setTitle("Редактирование назначений");
                label.setText("Обновить интервал периодов назначений в периоде");
                okButton.setText("Обновить");
                break;
        }
    }

}
