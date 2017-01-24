package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client;

import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.user.cellview.client.*;

public class ConstPfrHeaderBuilder extends AbstractHeaderOrFooterBuilder implements TableWithCheckedColumn {

    public ConstPfrHeaderBuilder(DataGrid table) {
        super(table, false);
    }

    boolean needCheckedRow = true;

    private CheckBoxHeader checkBoxHeader = new CheckBoxHeader();

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
            buildHeader(tr, checkBoxHeader, 0, 3, true);
        }

        buildHeader(tr, new TextHeader("№"), 0, 3, true);

        tr.endTR();

        tr = startRow();

        buildHeader(tr, new TextHeader("Параметры представления"), 2, 0, true);

        buildHeader(tr, new TextHeader("Параметры подразделения"), 7, 0, true);

        //buildHeader(tr, new TextHeader("Условия расчетов для ОП"), 1, 0, true);

        buildHeader(tr, new TextHeader("Сведения о реорганизации"), 3, 0, true);

        buildHeader(tr, new TextHeader("Ответственный за расчет"), 6, 0, true);

        tr.endTR();

        tr = startRow();

        buildHeader(tr, new TextHeader("Код НО конечного"), 0, 0, true);

        buildHeader(tr, new TextHeader("Код НО промежуточного"), 0, 0, true);

        buildHeader(tr, new TextHeader("КПП"), 0, 0, true);

        buildHeader(tr, new TextHeader("Код места, по которому представляется документ"), 0, 0, true);

        buildHeader(tr, new TextHeader("Наименование подразделения для отчета"), 0, 0, true);

        //buildHeader(tr, new TextHeader("Наименование для Приложения № 5"), 0, 0, true);

        buildHeader(tr, new TextHeader("Код вида экономической деятельности и по классификатору ОКВЭД"), 0, 0, true);

        buildHeader(tr, new TextHeader("Субъект Российской Федерации (код)"), 0, 0, true);

        buildHeader(tr, new TextHeader("ОКТМО"), 0, 0, true);

        buildHeader(tr, new TextHeader("Номер контактного телефона"), 0, 0, true);

        buildHeader(tr, new TextHeader("Код формы реорганизации и ликвидации"), 0, 0, true);

        buildHeader(tr, new TextHeader("ИНН реорганизованного обособленного подразделения"), 0, 0, true);

        buildHeader(tr, new TextHeader("КПП реорганизованного обособленного подразделения"), 0, 0, true);

        buildHeader(tr, new TextHeader("Признак лица, подписавшего документ"), 0, 0, true);

        buildHeader(tr, new TextHeader("Фамилия подписанта"), 0, 0, true);

        buildHeader(tr, new TextHeader("Имя подписанта"), 0, 0, true);

        buildHeader(tr, new TextHeader("Отчество подписанта"), 0, 0, true);

        buildHeader(tr, new TextHeader("Наименование документа, подтверждающего полномочия представителя"), 0, 0, true);

        buildHeader(tr, new TextHeader("Наименование организации-представителя налогоплательщика"), 0, 0, true);

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

    @Override
    public CheckBoxHeader getCheckBoxHeader() {
        return checkBoxHeader;
    }
}