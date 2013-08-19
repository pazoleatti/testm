package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.enums.*;
import com.aplana.sbrf.taxaccounting.model.migration.row.AbstractRnuRow;
import com.aplana.sbrf.taxaccounting.service.MigrationService;
import com.aplana.sbrf.taxaccounting.service.RnuGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class RnuGenerationServiceImpl implements RnuGenerationService {

    @Autowired
    MigrationService migrationService;

    private static final String CR = "\n\r";
    private static final String TOTAL_ROW = "TOTAL_P";
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public String generateRnuFile(Exemplar ex) {
        StringBuilder bu = new StringBuilder();
        bu.append(getRnuFirstRow(ex)).append(CR);
        bu.append(CR);

        List<? extends AbstractRnuRow> rnuRows = migrationService.getRnuList(ex);

        for (AbstractRnuRow row : rnuRows) {
            if (TOTAL_ROW.equals(row.getTypeRow())) {
                bu.append(CR);                          //итоговая строка отделается пустой строкой
            }
            bu.append(row.toRow()).append(CR);
        }
        return bu.toString();
    }

    /**
     * Возвращает названия для ТФ с расширением *.rnu
     * NNNПППИК.RXX, где:
     * <p/>
     * NNN - код налоговой формы;
     * ППП - код подразделения;
     * И - номер АС, формирующей налоговую форму автоматически;
     * К - код отчетного периода.
     * XX – дополнительный код части АС. Для РНУ, не передаваемых частями АС, XX = "NU" (т.е. получается расширение "RNU")
     *
     * @param exemplar модель с информацией по экмепляру рну
     * @return возвращает название файла с расширение rnu
     */
    @Override
    public String getRnuFileName(Exemplar exemplar) {
        StringBuilder builder = new StringBuilder();

        //NNN
        builder.append(NalogFormType.getNewCodefromOldCode(exemplar.getRnuTypeId()));

        //ППП
        builder.append(getRnuDepCode(exemplar));

        //И - замена на ИИИ
        builder.append(SystemType.getNewCodeByOldCode(exemplar.getSystemId()));

        DateFormat df = new SimpleDateFormat("MM");
        DateFormat year = new SimpleDateFormat("yyyy");

        //К - замена на КК
        switch (exemplar.getPeriodityId()) {
            case 1:            //Ежегодно
                builder.append(YearCode.fromYear(Integer.valueOf(year.format(exemplar.getBeginDate()))));
                break;
            case 4:             //Ежеквартально
                Integer month = Integer.valueOf(df.format(exemplar.getBeginDate()));
                builder.append(QuartalCode.fromNum(month));
                break;
            case 5:            //Ежемесячно
                builder.append(df.format(exemplar.getBeginDate()));
                break;
            case 8:             //ежедневно и по рабочим дням
            case 10:
            default:
        }

        //.RXX
        // убрал потом учто не понятно rnu только для DC или для Гамма в том числе.
        // builder.append(".R").append("00".equals(exemplar.getSubSystemId()) ? "NU" : exemplar.getSubSystemId());
        builder.append(".RNU");

        return builder.toString();
    }

    private String getRnuDepCode(Exemplar ex) {
        String cutCode = ex.getDepCode().substring(2); //сдвиг влево
        Integer intCode = Integer.valueOf(cutCode);

        if (Integer.valueOf(13).equals(intCode)) {
            return String.valueOf(SystemType.getDepCode(ex.getSystemId(), ex.getSubSystemId()));
        } else if (Integer.valueOf(22).equals(intCode)) {
            return String.valueOf(intCode) + "0";
        } else {
            //throw new ServiceException("Ошибка при транспонировании кода департамента.");
            // TODO (aivanov 14.08.2013) сделать правильную обработку, после уточнения значений
            return "XXX";
        }
    }

    /**
     * Формирует транслируему первую строку для ТФ с расширением *.rnu
     * <p/>
     * Первая строка (заголовок налоговой формы) содержит информацию вида:
     * <p/>
     * 1.Номер тер. банка;
     * 2.Номер подразделения ЦА, отделения.
     * 3.Начало отчетного периода (первый календарный день);
     * 4.Окончание отчетного периода (последний календарный день);
     * 5.Код налоговой формы.
     * 6.Номер АС, формирующей налоговую форму, может быть вместе с кодом части АС
     * <p/>
     * Пример: 99|0013|01.01.2013|31.03.2013|640|901
     *
     * @param exemplar основные данные рну
     * @return возвращает строку с кодом
     */
    private String getRnuFirstRow(Exemplar exemplar) {
        char sep = AbstractRnuRow.SEP;
        StringBuilder builder = new StringBuilder();

        builder.append(exemplar.getTerCode()).append(sep);              //1
        builder.append(getRnuDepCode(exemplar)).append(sep);       //2

        builder.append(formatter.format(exemplar.getBeginDate())).append(sep);      //3
        builder.append(formatter.format(exemplar.getEndDate())).append(sep);        //4

        builder.append(NalogFormType.getNewCodefromOldCode(exemplar.getRnuTypeId())).append(sep);   //5

        builder.append(SystemType.getNewCodeByOldCode(exemplar.getSystemId()));                     //6
        //Номер АС, формирующей налоговую форму, может быть вместе с кодом части АС
        //пока неизвестно нужно ли это
        //builder.append('|');
        //builder.append(exemplar.getSubSystemId());
        return builder.toString();
    }
}
