package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.BlobData;

/**
 * User: avanteev
 */
public interface BlobDataDao {

    String create(BlobData blobData);
    void delete(String uuid);
    void save(BlobData blobData);
    BlobData get(String uuid);
}
