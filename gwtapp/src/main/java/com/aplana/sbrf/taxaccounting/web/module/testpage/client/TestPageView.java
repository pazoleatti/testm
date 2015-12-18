package com.aplana.sbrf.taxaccounting.web.module.testpage.client;

import com.aplana.gwt.client.*;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.gwt.client.mask.ui.TextMaskBox;
import com.aplana.gwt.client.mask.ui.DateMaskBox;
import com.aplana.gwt.client.mask.ui.MonthYearMaskBox;
import com.aplana.gwt.client.mask.ui.YearMaskBox;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.DropdownButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.LabelSeparator;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class TestPageView extends ViewWithUiHandlers<TestPageUiHandlers> implements TestPagePresenter.MyView {

    public static final DateTimeFormat formatDMY = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

    interface Binder extends UiBinder<Widget, TestPageView> {
    }

    @UiField
    LabelSeparator sep;

    @UiField(provided = true)
    MultiListBox mlistbox;

    @UiField
    Label showResult;

    @UiField
    Button showDialog;

    @UiField
    DateMaskBox boxDate;
    @UiField
    MonthYearMaskBox boxMy;
    @UiField
    YearMaskBox boxY;
    @UiField
    TextMaskBox boxText;
    @UiField
    DateMaskBoxPicker boxDatePicker;
    @UiField
    Label boxDateLb,
            boxMyLb,
            boxYLb,
            boxTextLb,
            boxDatePickerLb;
    @UiField
    CheckBox checkBoxNull,
            checkBoxEnabled;

    @UiField
    LinkButton
            linkButtonDisable,
            linkButton;
    @UiField
    LinkAnchor
            linkAnchor,
            linkAnchorDisable;

    @UiField
    RefBookPickerWidget hPicker;
    @UiField
    Label hPickerLabel,
            hPickerList;
    @UiField
    CheckBox hpMultiPickCb,
            hpDisabledCb,
            hpSearchCb,
            hpManualCb;
    @UiField
    TextBox hpValueTb;
    @UiField
    Button hpSetValueBtn;

    @UiField
    RefBookPickerWidget fPicker;

    @UiField
    Label fPickerLabel,
            fPickerList;
    @UiField
    CheckBox fpMultiPickCb,
            fpDisabledCb,
            fpSearchCb,
            fpManualCb;
    @UiField
    TextBox fpValueTb;
    @UiField
    Button fpSetValueBtn;
    @UiField
    Button showDialog2;
    @UiField
    Button showDialog3;

    @UiField
    DropdownButton dropdownButton;
    @UiField
    CheckBox depUsageCb;
    @UiField
    Button depUsageBtn;
    @UiField
    ListBox eventLb;
    @UiField
    Button eventBtn;

    @Inject
    public TestPageView(final Binder uiBinder) {

        multiListBox();

        initWidget(uiBinder.createAndBindUi(this));

        Character s = 'а';
        System.out.println("check " + (int)s +"");

        System.out.println( "\\u" + Integer.toHexString('а' | 0x10000).substring(1) );

        List<TestItem> getM = (List<TestItem>) mlistbox.getValue();
        String strCont = "";
        for (TestItem str : getM)
            strCont = strCont + str.getTitle() + "; ";

        showResult.setText(strCont);

        modalWind();

        lineinRefBook();

        heirarRefBook();

        testMaskBox();

        linkButtons();

        dropdownButton.addItem("item1", new LinkButton("item1"));
        Button btn = new Button("item2");
        btn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Dialog.confirmMessage("It's OK!");
            }
        });
        dropdownButton.addItem("item2", btn);

        initDepUsage();
        initEvents();

    }

    private void multiListBox() {
        List<TestItem> itemList = new ArrayList<TestItem>();

        itemList.add(new TestItem("aad", 1));
        itemList.add(new TestItem("фыв", 2));
        itemList.add(new TestItem("aaaaaaaaaaaaaaaaaaaaaasssssssssssssssssssssssssssdddddddddddddddddddddddddddddddddddddddfffffffffffffffffffffffffffffffffffffeeeeeeeeeeeeeeeeaxxaz", 3));

        List<TestItem> valueList = new ArrayList<TestItem>();
        valueList.add(new TestItem("фыв", 2));

        mlistbox = new MultiListBox(new AbstractRenderer<TestItem>() {
            @Override
            public String render(TestItem item) {
                return item.getTitle();
            }
        }, true, true);

        mlistbox.setAvailableValues(itemList);
        mlistbox.setValue(valueList);
        mlistbox.addValueChangeHandler(new ValueChangeHandler<List>() {
            @Override
            public void onValueChange(ValueChangeEvent<List> event) {
                List<TestItem> getM = (List<TestItem>) mlistbox.getValue();
                String strCont = "";
                for (TestItem str : getM)
                    strCont = strCont + str.getTitle() + "; ";
                showResult.setText(strCont);
            }
        });

        mlistbox.setAvailableValues(itemList);
        mlistbox.setValue(valueList);
        mlistbox.addValueChangeHandler(new ValueChangeHandler<List>() {
            @Override
            public void onValueChange(ValueChangeEvent<List> event) {
                List<String> getM = (List<String>) mlistbox.getValue();
                String strCont = "";
                for (String str : getM)
                    strCont = strCont + str + "; ";
                showResult.setText(strCont);
            }
        });
    }


    private void modalWind() {
        final ModalWindow mw = new ModalWindow("тест", "resources/img/question_mark.png");
        HTMLPanel lbl1 = new HTMLPanel("Тут будет содержаться любой объект. А пока закрой меня.");
        lbl1.setHeight("100%");
//        lbl1.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                mw.setSize("200px", "200px");
//                System.out.println(mw.getOffsetWidth() + " " + mw.getOffsetHeight());
//            }
//        });
        //lbl1.setSize("200px","200px");
        mw.add(lbl1);
        showDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                mw.center();
            }

        });
        showDialog2.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Dialog.confirmMessage("Тескт", new DialogHandler() {
                    @Override
                    public void yes() {
                        Dialog.infoMessage("kfkfkfk");
                    }
                });
            }
        });

        showDialog3.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().openMessageDialog();
            }
        });
    }

    private void lineinRefBook() {
        Date date  = new Date();
        fPicker.setPeriodDates(date, date);
        fPicker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                fPickerLabel.setText("Выбрано: " + fPicker.getDereferenceValue());
                fPickerList.setText("Список: " + event.getValue());
            }
        });
        fpDisabledCb.setValue(!fPicker.isEnabled());
        fpManualCb.setValue(fPicker.isManualUpdate());
        fpMultiPickCb.setValue(hPicker.getMultiSelect());
        fpSearchCb.setValue(!hPicker.getSearchEnabled());
        fpSetValueBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String s = fpValueTb.getText();
                List<Long> longs = new LinkedList<Long>();
                if (s != null && !s.trim().isEmpty()) {
                    s = s.trim();
                    if (s.contains(",")) {
                        for (String s1 : s.split(",")) {
                            if (s1 != null && !s1.trim().isEmpty()) {
                                longs.add(Long.valueOf(s1.trim()));
                            }
                        }
                    } else {
                        longs.add(Long.valueOf(s));
                    }
                } else {
                    longs = null;
                }
                fPicker.setValue(longs, true);
            }
        });
        fpSearchCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fPicker.setSearchEnabled(!event.getValue());
            }
        });
        fpMultiPickCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fPicker.setMultiSelect(event.getValue());
            }
        });
        fpManualCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fPicker.setManualUpdate(event.getValue());
            }
        });
        fpDisabledCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fPicker.setEnabled(!event.getValue());
            }
        });
    }

    private void heirarRefBook() {
        Date date  = new Date();
        hPicker.setPeriodDates(date, date);
        hPicker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                hPickerLabel.setText("Выбрано: " + hPicker.getDereferenceValue());
                hPickerList.setText("Список: " + event.getValue());
            }
        });
        hpDisabledCb.setValue(!hPicker.isEnabled());
        hpManualCb.setValue(hPicker.isManualUpdate());
        hpMultiPickCb.setValue(hPicker.getMultiSelect());
        hpSearchCb.setValue(!hPicker.getSearchEnabled());
        hpSetValueBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String s = hpValueTb.getText();
                List<Long> longs = new LinkedList<Long>();
                if (s != null && !s.trim().isEmpty()) {
                    s = s.trim();
                    if (s.contains(",")) {
                        for (String s1 : s.split(",")) {
                            if (s1 != null && !s1.trim().isEmpty()) {
                                longs.add(Long.valueOf(s1.trim()));
                            }
                        }
                    } else {
                        longs.add(Long.valueOf(s));
                    }
                } else {
                    longs = null;
                }
                hPicker.setValue(longs, true);
            }
        });
        hpSearchCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
               hPicker.setSearchEnabled(!event.getValue());
            }
        });
        hpMultiPickCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                hPicker.setMultiSelect(event.getValue());
            }
        });
        hpManualCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                hPicker.setManualUpdate(event.getValue());
            }
        });
        hpDisabledCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                hPicker.setEnabled(!event.getValue());
            }
        });
    }

    private void linkButtons() {
        linkButtonDisable.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("!!!");
            }
        });

        linkButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("!!!!");
            }
        });

        linkAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("@@@@");
            }
        });

        linkAnchorDisable.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("@@@@");
            }
        });
    }

    private void testMaskBox() {
        boxDatePicker.setLimitDates(new Date(2014 - 1900, 3 - 1, 1), new Date(2014 - 1900, 3 - 1, 27));
        //boxDatePicker.setValue(new Date(2013 - 1900, 2, 1));
        checkBoxNull.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boxDate.setMayBeNull(event.getValue());
                boxMy.setMayBeNull(event.getValue());
                boxY.setMayBeNull(event.getValue());
                boxText.setMayBeNull(event.getValue());
                boxDatePicker.setCanBeEmpty(event.getValue());
                if (event.getValue()) {
                    boxDate.setValue(null, true);
                    boxMy.setValue(null, true);
                    boxY.setValue(null, true);
                    boxText.setValue(null, true);
                    boxDatePicker.setValue(null, true);
                }
            }
        });

        checkBoxEnabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boxDate.setEnabled(event.getValue());
                boxMy.setEnabled(event.getValue());
                boxY.setEnabled(event.getValue());
                boxText.setEnabled(event.getValue());
                boxDatePicker.setEnabled(event.getValue());
            }
        });

        Date date = new Date();
        boxDate.setValue(date);
        boxMy.setValue(date);
        boxY.setValue(date);

        boxDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                boxDateLb.setText(getTestMaskValues(event.getValue(), boxDate.getValue()));
            }
        });

        boxMy.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                boxMyLb.setText(getTestMaskValues(event.getValue(), boxMy.getValue()));
            }
        });

        boxY.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                boxYLb.setText(getTestMaskValues(event.getValue(), boxY.getValue()));
            }
        });

        boxText.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                boxTextLb.setText(getTestMaskValues(event.getValue(), boxText.getValue()));
            }
        });

        boxDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                boxDatePickerLb.setText(getTestMaskValues(event.getValue(), boxDatePicker.getValue()));
            }
        });
    }

    private String getTestMaskValues(String eventValue, String elemValue) {
        return "Событие: \"" + eventValue + "\", значение: \"" + elemValue + "\" .";
    }

    private String getTestMaskValues(Date eventValue, Date elemValue) {
        return "Событие: \"" + (eventValue != null ? formatDMY.format(eventValue) : null) + "\", значение: \"" + (elemValue != null ? formatDMY.format(elemValue) : null) + "\" .";
    }


    private void initDepUsage(){
        depUsageCb.setValue(false);
        depUsageBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (hPicker.getAttributeId() == 161L) {
                    getUiHandlers().setUsageDepartment();
                } else {
                    Dialog.infoMessage("Иерархический справочник не справочник подразделений");
                }
            }
        });
    }

    private void initEvents(){
        eventBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(eventLb.getSelectedIndex() > 0){
                    getUiHandlers().doEvent();
                }
            }
        });
    }

    @Override
    public Long getDepId() {
        return hPicker.getSingleValue();
    }

    @Override
    public Boolean getDepUsageValue() {
        return depUsageCb.getValue();
    }

    @Override
    public String getSelectedEvent() {
        return eventLb.getValue(eventLb.getSelectedIndex());
    }

    @Override
    public void setEvents(Map<Integer, String> map) {
        for (Map.Entry<Integer, String> integerStringEntry : map.entrySet()) {
            eventLb.addItem(integerStringEntry.getValue(), String.valueOf(integerStringEntry.getKey()));
        }
    }


    @Override
    public void setIds(int fpickerId, int hpickerId) {
        if (fpickerId != -1) {
            fPicker.setAttributeIdInt(fpickerId);
        }
        fPicker.setTitle(fPicker.getTitle() + "Выбор из справочника. Атрибут разименования=" +  fPicker.getAttributeId());
        if (hpickerId != -1) {
            hPicker.setAttributeIdInt(hpickerId);
        }
        hPicker.setTitle(hPicker.getTitle() + "Выбор из справочника. Атрибут разименования=" + hPicker.getAttributeId());
    }
}
