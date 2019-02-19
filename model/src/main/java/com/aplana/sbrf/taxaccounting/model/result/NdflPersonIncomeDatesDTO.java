package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * DTO для "Массового редактирования дат в разделе 2" налоговой формы.
 */
@Data
public class NdflPersonIncomeDatesDTO {
    private List<Long> incomeIds;
    private Date accruedDate;
    private Date payoutDate;
    private Date taxDate;
    private Date transferDate;
}
