package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Параметры для создания формы 2-НДФЛ (ФЛ) из GUI
 */
@Getter
@Setter
public class Create2NdflFLParams implements Serializable {
    private long personId;
    private int declarationTypeId;
    private int reportPeriodId;
    private List<KppOktmoPair> kppOktmoPairs;
    private String signatory;
    private List<Long> declaration2Ndfl1Ids;
    private transient InputStream arialFont;
    private transient InputStream arialBoldFont;
    /**
     * Параметр ответа groovy скрипта - содержит файл отчета для идентификатора налоговой формы
     */
    private transient Map<Long, File> createdReports;
    /**
     * Параметр ответа groovy скрипта - содержит имя файла отчета для идентификатора налоговой формы
     */
    private transient Map<Long, String> createdReportsFileName;
}
