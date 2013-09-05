package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.enums.*;
import com.aplana.sbrf.taxaccounting.model.migration.row.AbstractRnuRow;
import com.aplana.sbrf.taxaccounting.service.MigrationService;
import com.aplana.sbrf.taxaccounting.service.RnuGenerationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class RnuGenerationServiceImpl implements RnuGenerationService {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    MigrationService migrationService;

    private static final String CR = "\r\n";
    private static final String TOTAL_ROW = "TOTAL_P";
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public String generateRnuFileToString(Exemplar ex) {
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

    @Override
    public byte[] generateRnuFileToBytes(Exemplar ex) {
        String strFile = generateRnuFileToString(ex);
        return strFile.getBytes();
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
        builder.append(NalogFormType.getById(exemplar.getRnuTypeId()).getStringNNN());

        //ППП
        builder.append(DepartmentCode.fromDataBaseCode(exemplar.getDepCode()).getFilenamePartCode());

        //И
        builder.append(SystemType.fromId(exemplar.getSystemId()).getSysCodeChar());

        Integer month = Integer.valueOf(new SimpleDateFormat("MM").format(exemplar.getBeginDate()));
        Integer year = Integer.valueOf(new SimpleDateFormat("yyyy").format(exemplar.getBeginDate()));

        //К
        switch (Periodity.fromId(exemplar.getPeriodityId())) {
            case YEAR:            //Ежегодно
                builder.append(YearCode.fromYear(year).getCode());
                break;
            case QUARTER:             //Ежеквартально
                builder.append(QuartalCode.fromNum(month).getCodeIfQuartal());
                break;
            case MONTH:            //Ежемесячно
                builder.append(QuartalCode.fromNum(month).getCodeIfMonth());
                break;
            case DAY:             //ежедневно и по рабочим дням
            case WORKDAY:
            default:
        }

        builder.append(".RNU");

        return builder.toString();
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

        builder.append(Integer.valueOf(exemplar.getTerCode())).append(sep);              //1
        builder.append(exemplar.getDepCode()).append(sep);       //2

        builder.append(formatter.format(exemplar.getBeginDate())).append(sep);      //3
        builder.append(formatter.format(exemplar.getEndDate())).append(sep);        //4

        builder.append(DepartmentCode.fromDataBaseCode(exemplar.getDepCode()).getFilenamePartCode()).append(sep);   //5

        SystemType sysType = SystemType.fromId(exemplar.getSystemId());

        builder.append(sysType.getSysCodeChar());                     //6
        if (SystemType.DC != sysType) {
            builder.append(sysType.getSubCode());
        }

        return builder.toString();
    }
}
