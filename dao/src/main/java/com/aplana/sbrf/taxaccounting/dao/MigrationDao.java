package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.row.*;

import java.util.List;

public interface MigrationDao {

    List<Exemplar> getExemplarByRnuType(long rnuTypeId, String yearSeq);

    List<Rnu25Row> getRnu25RowList(Exemplar ex);

    List<Rnu26Row> getRnu26RowList(Exemplar ex);

    List<Rnu27Row> getRnu27RowList(Exemplar ex);

    List<Rnu31Row> getRnu31RowList(Exemplar ex);

    List<Rnu64Row> getRnu64RowList(Exemplar ex);

    List<RnuCommonRow> getRnu60RowList(Exemplar ex);

    List<RnuCommonRow> getRnu59RowList(Exemplar ex);

    List<RnuCommonRow> getRnu54RowList(Exemplar ex);

    List<RnuCommonRow> getRnu53RowList(Exemplar ex);

    List<Rnu51Row> getRnu51RowList(Exemplar ex);
}
