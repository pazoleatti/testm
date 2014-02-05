package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;

/**
 * Label-разделитель
 * @author vpetrov
 */
public class LabelSeparator extends Composite implements HasText {
    private static Binder uiBinder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, LabelSeparator> {
    }

    @UiField
    Label textBox;

    @UiField
    HorizontalPanel panel;

    public LabelSeparator() {
    }

    @Inject
    @UiConstructor
    public LabelSeparator(String text) {
        initWidget(uiBinder.createAndBindUi(this));
        textBox.setText(text);
    }

    public void setText(String text){
        textBox.setText(text);
    }

    public String getText(){
        return textBox.getText();
    }

    public void setWidth(String width){
        panel.setWidth(width);
    }

    public int getOffsetWidth(){
      return super.getOffsetWidth();
    }

    public void setVisible(boolean visible){
        panel.setVisible(visible);
    }

    public boolean isVisible(){
        return panel.isVisible();
    }
}
