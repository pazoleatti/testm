package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client;

import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.user.cellview.client.*;

public class ConstTransportHeaderBuilder extends AbstractHeaderOrFooterBuilder implements TableWithCheckedColumn {

    public ConstTransportHeaderBuilder(DataGrid table) {
        super(table, false);
    }

    boolean needCheckedRow = true;


    @Override
    public boolean isNeedCheckedRow() {
        return needCheckedRow;
    }

    @Override
    public void setNeedCheckedRow(boolean needCheckedRow) {
        this.needCheckedRow = needCheckedRow;
    }

    @Override
    protected boolean buildHeaderOrFooterImpl() {


        TableRowBuilder tr = startRow();
        if (needCheckedRow) {
            CheckBoxHeader cbh = new CheckBoxHeader();

            buildHeader(tr, cbh, 0, 2, true);
        }

        buildHeader(tr, new TextHeader("Параметры декларации"), 2, 0, true);

        buildHeader(tr, new TextHeader("Реквизиты подразделения"), 5, 0, true);

        buildHeader(tr, new TextHeader("Сведения о реорганизации"), 3, 0, true);

        buildHeader(tr, new TextHeader("Ответственный за декларацию"), 6, 0, true);

        tr.endTR();

        tr = startRow();

        buildHeader(tr, new TextHeader("Код налогового органа"), 0, 0, true);

        buildHeader(tr, new TextHeader("КПП"), 0, 0, true);

        buildHeader(tr, new TextHeader("Код по месту нахождения (учета)"), 0, 0, true);

        buildHeader(tr, new TextHeader("Наименование (налогоплательщик)"), 0, 0, true);

        buildHeader(tr, new TextHeader("ОКВЭД"), 0, 0, true);

        buildHeader(tr, new TextHeader("Номер контактного телефона"), 0, 0, true);

        buildHeader(tr, new TextHeader("Обязанность по уплате авансовых платежей"), 0, 0, true);

        buildHeader(tr, new TextHeader("Код формы реорганизации и ликвидации"), 0, 0, true);

        buildHeader(tr, new TextHeader("ИНН реорг. организации"), 0, 0, true);

        buildHeader(tr, new TextHeader("КПП реорг. организации"), 0, 0, true);

        buildHeader(tr, new TextHeader("Признак лица, подписавшего документ"), 0, 0, true);

        buildHeader(tr, new TextHeader("Фамилия"), 0, 0, true);

        buildHeader(tr, new TextHeader("Имя"), 0, 0, true);

        buildHeader(tr, new TextHeader("Отчество"), 0, 0, true);

        buildHeader(tr, new TextHeader("Наименование документа представителя"), 0, 0, true);

        buildHeader(tr, new TextHeader("Наименование организации представителя"), 0, 0, true);

        tr.endTR();

        return true;
    }

    private void buildHeader(TableRowBuilder out, Header<?> header, int colSpan, int rowSpan, boolean needBorder) {
        AbstractCellTable.Style style = getTable().getResources().style();
        StringBuilder classesBuilder = new StringBuilder(style.header());

        TableCellBuilder th = out.startTH().colSpan(colSpan).rowSpan(rowSpan).className(classesBuilder.toString());

        Context context = new Context(0, 2, header.getKey());
        if (!needBorder) {
            th.style().borderStyle(com.google.gwt.dom.client.Style.BorderStyle.NONE);
        } else {
            th.style().width(0, com.google.gwt.dom.client.Style.Unit.EM);
        }
        renderHeader(th, context, header);

        th.endTH();
    }
}
