package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.MigrationDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.MapperUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.row.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MigrationDaoImpl extends AbstractDao implements MigrationDao {

    private static final Log LOG = LogFactory.getLog(MigrationDaoImpl.class);
    private static final String ACTUAL_EXEMPLAR_NOT_FOUND_MESSAGE = "Актуального экземпляра записей для типа РНУ - %d не найдено в БД";

    private class ExemplarRowMapper implements RowMapper<Exemplar> {
        @Override
        public Exemplar mapRow(ResultSet rs, int index) throws SQLException {
            Exemplar exemplar = new Exemplar();

            exemplar.setExemplarId(SqlUtils.getLong(rs,1));
            exemplar.setPeriodityId(SqlUtils.getInteger(rs,2));
            exemplar.setBeginDate(rs.getDate(3));
            exemplar.setEndDate(rs.getDate(4));
            exemplar.setRnuTypeId(SqlUtils.getInteger(rs,5));
            exemplar.setDepCode(rs.getString(6));
            exemplar.setSystemId(SqlUtils.getInteger(rs,7));
            exemplar.setSubSystemId(rs.getString(8));
            exemplar.setTerCode(rs.getString(9));

            return exemplar;
        }
    }

    @Override
    public List<Exemplar> getExemplarByRnuType(long rnuTypeId, String yearSeq) {
        try {
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"ex\".idexemplar as \"exemplarid\",\n" +
                            "\"per\".fidperiodity as \"periodityid\",\n" +
                            "\"per\".datebegin as \"begindate\",\n" +
                            "\"per\".dateend as \"enddate\",\n" +
                            "\"objdict\".idobjdict as \"rnutypeid\",\n" +
                            "\"dep\".code as \"depcode\",\n" +
                            "\"sys\".idasystem as \"systemid\",\n" +
                            "\"sys\".codesubsystem as \"subsystemid\",\n" +
                            "\"depter\".code as \"tercode\"\n" +
                            "from\n" +
                            "migration.exemplar \"ex\"\n" +
                            "inner join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "inner join migration.objdict \"objdict\" on \"obj\".fidobjdict = \"objdict\".idobjdict\n" +
                            "inner join migration.periodlist \"per\" on \"ex\".fidperiodlist = \"per\".idperiodlist\n" +
                            "inner join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "inner join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.department \"depter\" on \"depter\".id = \"dep\".par_field\n" +
                            "where\n" +
                            "\"ex\".typeexemplar like 'ACTUAL' and\n " +
                            "TO_CHAR(\"per\".datebegin, 'yyyy') in (" + yearSeq + ")  and\n " +
                            "\"objdict\".idobjdict = ?",
                    new Object[]{rnuTypeId},
                    new ExemplarRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(ACTUAL_EXEMPLAR_NOT_FOUND_MESSAGE, rnuTypeId);
        }
    }

    private class Rnu25RowMapper implements RowMapper<Rnu25Row> {
        @Override
        public Rnu25Row mapRow(ResultSet rs, int index) throws SQLException {
            Rnu25Row row = new Rnu25Row();

            row.setNum(MapperUtils.getLong(rs, 1));
            row.setNumState(MapperUtils.getString(rs, 2));
            row.setNumDeal(MapperUtils.getString(rs, 3));
            row.setNumPaperPrev(MapperUtils.getBD(rs, 4));
            row.setNumPaper(MapperUtils.getBD(rs, 5));
            row.setReservePrev(MapperUtils.getBD(rs, 6));
            row.setGetPrice(MapperUtils.getBD(rs, 7));
            row.setTypePaper(MapperUtils.getString(rs, 8));
            row.setMarketPriceOne(MapperUtils.getBD(rs, 9));
            row.setMarketPrice(MapperUtils.getBD(rs, 10));
            row.setReserve(MapperUtils.getBD(rs, 11));
            row.setReserveCreate(MapperUtils.getBD(rs, 12));
            row.setReserveRest(MapperUtils.getBD(rs, 13));
            row.setTypeRow(MapperUtils.getString(rs, 14));
            return row;
        }
    }

    @Override
    public List<Rnu25Row> getRnu25RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu25RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r25\".num as \"num\",\n" +
                            "\"r25\".numstate as \"numstate\",\n" +
                            "\"r25\".numdeal as \"numdeal\",\n" +
                            "\"r25\".numpaperprev as \"numpaperprev\",\n" +
                            "\"r25\".numpaper as \"numpaper\",\n" +
                            "\"r25\".reserveprev as \"reserveprev\",\n" +
                            "\"r25\".getprice as \"getprice\",\n" +
                            "\"r25\".typepaper as \"typepaper\",\n" +
                            "\"r25\".marketpriceone as \"marketpriceone\",\n" +
                            "\"r25\".marketprice as \"marketprice\",\n" +
                            "\"r25\".reserve as \"reserve\",\n" +
                            "\"r25\".reservecreate as \"reservecreate\",\n" +
                            "\"r25\".reserverest as \"reserverest\",\n" +
                            "\"r25\".typerow as \"typerow\"\n" +
                            "from\n" +
                            "migration.trd_25 \"r25\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r25\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=? " +
                            "order by \"num\" asc,\n" +
                            "\"typerow\" desc",
                    new Object[]{ex.getExemplarId()},
                    new Rnu25RowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }

    private class Rnu26RowMapper implements RowMapper<Rnu26Row> {
        @Override
        public Rnu26Row mapRow(ResultSet rs, int index) throws SQLException {
            Rnu26Row row = new Rnu26Row();

            row.setNum(MapperUtils.getLong(rs, 1));
            row.setEmitter(MapperUtils.getString(rs, 2));
            row.setTypeshares(MapperUtils.getString(rs, 3));
            row.setNumDeal(MapperUtils.getString(rs, 4));
            row.setCurshares(MapperUtils.getString(rs, 5));
            row.setNumPaperPrev(MapperUtils.getBD(rs, 6));
            row.setNumPaper(MapperUtils.getBD(rs, 7));
            row.setReservePrev(MapperUtils.getBD(rs, 8));
            row.setGetPrice(MapperUtils.getBD(rs, 9));
            row.setTypePaper(MapperUtils.getString(rs, 10));
            row.setQuotcur(MapperUtils.getBD(rs, 11));
            row.setRatequot(MapperUtils.getBD(rs, 12));
            row.setMarketPriceOne(MapperUtils.getBD(rs, 13));
            row.setMarketPrice(MapperUtils.getBD(rs, 14));
            row.setReserve(MapperUtils.getBD(rs, 15));
            row.setReserveCreate(MapperUtils.getBD(rs, 16));
            row.setReserveRest(MapperUtils.getBD(rs, 17));
            row.setTypeRow(MapperUtils.getString(rs, 18));
            return row;
        }
    }

    @Override
    public List<Rnu26Row> getRnu26RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu26RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r26\".num,\n" +
                            "\"r26\".emitter,\n" +
                            "\"r26\".typeshares,\n" +
                            "\"r26\".numdeal,\n" +
                            "\"r26\".curshares,\n" +
                            "\"r26\".numpaperprev,\n" +
                            "\"r26\".numpaper,\n" +
                            "\"r26\".reserveprev,\n" +
                            "\"r26\".getprice,\n" +
                            "\"r26\".typepaper,\n" +
                            "\"r26\".quotcur,\n" +
                            "\"r26\".ratequot,\n" +
                            "\"r26\".marketpriceone,\n" +
                            "\"r26\".marketprice,\n" +
                            "\"r26\".reserve,\n" +
                            "\"r26\".reservecreate,\n" +
                            "\"r26\".reserverest,\n" +
                            "\"r26\".typerow\n" +
                            "from migration.trd_26 \"r26\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r26\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=?\n" +
                            "order by \"r26\".num asc, \"r26\".typerow desc",
                    new Object[]{ex.getExemplarId()},
                    new Rnu26RowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }

    private class Rnu27RowMapper implements RowMapper<Rnu27Row> {
        @Override
        public Rnu27Row mapRow(ResultSet rs, int index) throws SQLException {
            Rnu27Row row = new Rnu27Row();

            row.setNum(MapperUtils.getLong(rs, 1));
            row.setEmitter(MapperUtils.getString(rs, 2));
            row.setNumState(MapperUtils.getString(rs, 3));
            row.setNumDeal(MapperUtils.getString(rs, 4));
            row.setCurshares(MapperUtils.getString(rs, 5));
            row.setNumPaperPrev(MapperUtils.getBD(rs, 6));
            row.setNumPaper(MapperUtils.getBD(rs, 7));
            row.setReservePrev(MapperUtils.getBD(rs, 8));
            row.setGetPrice(MapperUtils.getBD(rs, 9));
            row.setTypePaper(MapperUtils.getString(rs, 10));
            row.setQuotcur(MapperUtils.getBD(rs, 11));
            row.setRatequot(MapperUtils.getBD(rs, 12));
            row.setMarketPriceOne(MapperUtils.getBD(rs, 13));
            row.setMarketPrice(MapperUtils.getBD(rs, 14));
            row.setReserve(MapperUtils.getBD(rs, 15));
            row.setReserveCreate(MapperUtils.getBD(rs, 16));
            row.setReserveRest(MapperUtils.getBD(rs, 17));
            row.setTypeRow(MapperUtils.getString(rs, 18));
            return row;
        }
    }

    @Override
    public List<Rnu27Row> getRnu27RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu27RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r27\".num,\n" +
                            "\"r27\".emitter,\n" +
                            "\"r27\".numstate,\n" +
                            "\"r27\".numdeal,\n" +
                            "\"r27\".curshares,\n" +
                            "\"r27\".numpaperprev,\n" +
                            "\"r27\".numpaper,\n" +
                            "\"r27\".reserveprev,\n" +
                            "\"r27\".getprice,\n" +
                            "\"r27\".typepaper,\n" +
                            "\"r27\".quotcur,\n" +
                            "\"r27\".ratequot,\n" +
                            "\"r27\".marketpriceone,\n" +
                            "\"r27\".marketprice,\n" +
                            "\"r27\".reserve,\n" +
                            "\"r27\".reservecreate,\n" +
                            "\"r27\".reserverest,\n" +
                            "\"r27\".typerow\n" +
                            "from migration.trd_27 \"r27\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r27\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=?\n" +
                            "order by \"r27\".num asc, \"r27\".typerow desc",
                    new Object[]{ex.getExemplarId()},
                    new Rnu27RowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }


    private class Rnu31RowMapper implements RowMapper<Rnu31Row> {
        @Override
        public Rnu31Row mapRow(ResultSet rs, int index) throws SQLException {
            Rnu31Row row = new Rnu31Row();

            row.setNum(MapperUtils.getLong(rs, 1));
            row.setCodeTypePaper(MapperUtils.getLong(rs, 2));
            row.setTypePaper(MapperUtils.getString(rs, 3));
            row.setPercCashOfz(MapperUtils.getBD(rs, 4));
            row.setPercCashFed(MapperUtils.getBD(rs, 5));
            row.setPercCashBel(MapperUtils.getBD(rs, 6));
            row.setPercCashIpotAfter(MapperUtils.getBD(rs, 7));
            row.setPercCashMun(MapperUtils.getBD(rs, 8));
            row.setPercCashIpotBefore(MapperUtils.getBD(rs, 9));
            row.setPercCashOgvz(MapperUtils.getBD(rs, 10));
            row.setPercCashEuroNew(MapperUtils.getBD(rs, 11));
            row.setPercCashOther(MapperUtils.getBD(rs, 12));
            row.setPercCashCorp(MapperUtils.getBD(rs, 13));
            row.setTypeRow(MapperUtils.getString(rs, 14));
            return row;
        }
    }

    @Override
    public List<Rnu31Row> getRnu31RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu31RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r31\".num,\n" +
                            "\"r31\".codetypepaper,\n" +
                            "\"r31\".typepaper,\n" +
                            "\"r31\".perccashofz,\n" +
                            "\"r31\".perccashfed,\n" +
                            "\"r31\".perccashbel,\n" +
                            "\"r31\".perccashipotafter,\n" +
                            "\"r31\".perccashmun,\n" +
                            "\"r31\".perccashipotbefore,\n" +
                            "\"r31\".perccashogvz,\n" +
                            "\"r31\".perccasheuronew,\n" +
                            "\"r31\".perccashother,\n" +
                            "\"r31\".perccashcorp,\n" +
                            "\"r31\".typerow\n" +
                            "from migration.trd_31 \"r31\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r31\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=?\n" +
                            "order by \"r31\".num asc, \"r31\".typerow desc",
                    new Object[]{ex.getExemplarId()},
                    new Rnu31RowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }


    private class Rnu64RowMapper implements RowMapper<Rnu64Row> {
        @Override
        public Rnu64Row mapRow(ResultSet rs, int index) throws SQLException {
            Rnu64Row row = new Rnu64Row();

            row.setNum(MapperUtils.getLong(rs, 1));
            row.setDealDate(MapperUtils.getDate(rs, 2));
            row.setPartDeal(MapperUtils.getString(rs, 3));
            row.setNumDeal(MapperUtils.getString(rs, 4));
            row.setDefPaper(MapperUtils.getString(rs, 5));
            row.setCost(MapperUtils.getBD(rs, 6));
            row.setTypeRow(MapperUtils.getString(rs, 7));
            return row;
        }
    }

    @Override
    public List<Rnu64Row> getRnu64RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu64RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r64\".num,\n" +
                            "\"r64\".ddeal,\n" +
                            "\"r64\".partdeal,\n" +
                            "\"r64\".numdeal,\n" +
                            "\"r64\".defpaper,\n" +
                            "\"r64\".rcost,\n" +
                            "\"r64\".typerow\n" +
                            "from migration.trd_64 \"r64\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r64\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=?\n" +
                            "order by \"r64\".num asc, \"r64\".typerow desc",
                    new Object[]{ex.getExemplarId()},
                    new Rnu64RowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }

    private class RnuCommonRowMapper implements RowMapper<RnuCommonRow> {
        @Override
        public RnuCommonRow mapRow(ResultSet rs, int index) throws SQLException {
            RnuCommonRow row = new RnuCommonRow();

            row.setNum(MapperUtils.getLong(rs, 1));

            row.setNumDeal(MapperUtils.getString(rs, 2));
            row.setDefPaper(MapperUtils.getString(rs, 3));
            row.setCodecurrency(MapperUtils.getString(rs, 4));
            row.setNompaper(MapperUtils.getBD(rs, 5));
            row.setDrepo1(MapperUtils.getDate(rs, 6));
            row.setDrepo2(MapperUtils.getDate(rs, 7));
            row.setGetpricenkd(MapperUtils.getBD(rs, 8));
            row.setSalepricenkd(MapperUtils.getBD(rs, 9));
            row.setCostrepo(MapperUtils.getBD(rs, 10));
            row.setImplrepo(MapperUtils.getBD(rs, 11));
            row.setBankrate(MapperUtils.getBD(rs, 12));
            row.setCostrepo269(MapperUtils.getBD(rs, 13));
            row.setCostrepotax(MapperUtils.getBD(rs, 14));

            row.setTypeRow(MapperUtils.getString(rs, 15));
            return row;
        }
    }

    @Override
    public List<RnuCommonRow> getRnu60RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu60RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r60\".num,\n" +
                            "\"r60\".numdeal,\n" +
                            "\"r60\".defpaper,\n" +
                            "\"r60\".codecurrency,\n" +
                            "\"r60\".nompaper,\n" +
                            "\"r60\".drepo1,\n" +
                            "\"r60\".drepo2,\n" +
                            "\"r60\".getpricenkd,\n" +
                            "\"r60\".salepricenkd,\n" +
                            "\"r60\".costrepo,\n" +
                            "\"r60\".implrepo,\n" +
                            "\"r60\".bankrate,\n" +
                            "\"r60\".costrepo269,\n" +
                            "\"r60\".costrepotax,\n" +
                            "\"r60\".typerow\n" +
                            "from migration.trd_60 \"r60\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r60\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=?\n" +
                            "order by \"r60\".num asc, \"r60\".typerow desc",
                    new Object[]{ex.getExemplarId()},
                    new RnuCommonRowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }

    @Override
    public List<RnuCommonRow> getRnu59RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu59RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r59\".num,\n" +
                            "\"r59\".numdeal,\n" +
                            "\"r59\".defpaper,\n" +
                            "\"r59\".codecurrency,\n" +
                            "\"r59\".nompaper,\n" +
                            "\"r59\".drepo1,\n" +
                            "\"r59\".drepo2,\n" +
                            "\"r59\".getpricenkd,\n" +
                            "\"r59\".salepricenkd,\n" +
                            "\"r59\".costrepo,\n" +
                            "\"r59\".implrepo,\n" +
                            "\"r59\".bankrate,\n" +
                            "\"r59\".costrepo269,\n" +
                            "\"r59\".costrepotax,\n" +
                            "\"r59\".typerow\n" +
                            "from migration.trd_59 \"r59\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r59\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=?\n" +
                            "order by \"r59\".num asc, \"r59\".typerow desc",
                    new Object[]{ex.getExemplarId()},
                    new RnuCommonRowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }

    @Override
    public List<RnuCommonRow> getRnu54RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu54RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r54\".num,\n" +
                            "\"r54\".numdeal,\n" +
                            "\"r54\".defpaper,\n" +
                            "\"r54\".codecurrency,\n" +
                            "\"r54\".nompaper,\n" +
                            "\"r54\".drepo1,\n" +
                            "\"r54\".drepo2,\n" +
                            "\"r54\".getpricenkd,\n" +
                            "\"r54\".salepricenkd,\n" +
                            "\"r54\".costrepo,\n" +
                            "\"r54\".implrepo,\n" +
                            "\"r54\".bankrate,\n" +
                            "\"r54\".costrepo269,\n" +
                            "\"r54\".costrepotax,\n" +
                            "\"r54\".typerow\n" +
                            "from migration.trd_54 \"r54\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r54\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=?\n" +
                            "order by \"r54\".num asc, \"r54\".typerow desc",
                    new Object[]{ex.getExemplarId()},
                    new RnuCommonRowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }


    @Override
    public List<RnuCommonRow> getRnu53RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu53RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r53\".num,\n" +
                            "\"r53\".numdeal,\n" +
                            "\"r53\".defpaper,\n" +
                            "\"r53\".codecurrency,\n" +
                            "\"r53\".nompaper,\n" +
                            "\"r53\".drepo1,\n" +
                            "\"r53\".drepo2,\n" +
                            "\"r53\".getpricenkd,\n" +
                            "\"r53\".salepricenkd,\n" +
                            "\"r53\".costrepo,\n" +
                            "\"r53\".implrepo,\n" +
                            "\"r53\".bankrate,\n" +
                            "\"r53\".costrepo269,\n" +
                            "\"r53\".costrepotax,\n" +
                            "\"r53\".typerow\n" +
                            "from migration.trd_53 \"r53\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r53\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=?\n" +
                            "order by \"r53\".num asc, \"r53\".typerow desc",
                    new Object[]{ex.getExemplarId()},
                    new RnuCommonRowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }

    private class Rnu51RowMapper implements RowMapper<Rnu51Row> {
        @Override
        public Rnu51Row mapRow(ResultSet rs, int index) throws SQLException {
            Rnu51Row row = new Rnu51Row();

            row.setNum(MapperUtils.getLong(rs, 1));
            row.setCodedeal(MapperUtils.getLong(rs, 2));

            row.setTypepaper(MapperUtils.getString(rs, 3));
            row.setDefpaper(MapperUtils.getString(rs, 4));
            row.setDget(MapperUtils.getDate(rs, 5));
            row.setDimpl(MapperUtils.getDate(rs, 6));
            row.setNumpaper(MapperUtils.getLong(rs, 7));
            row.setSalepriceperc(MapperUtils.getBD(rs, 8));
            row.setRsaleprice(MapperUtils.getBD(rs, 9));
            row.setGetsalepricetax(MapperUtils.getBD(rs, 10));
            row.setGetmpriceperc(MapperUtils.getBD(rs, 11));
            row.setGetmprice(MapperUtils.getBD(rs, 12));
            row.setRmarketprice(MapperUtils.getBD(rs, 13));
            row.setMarketpriceperc(MapperUtils.getBD(rs, 14));
            row.setRcost(MapperUtils.getBD(rs, 15));
            row.setRtotalcost(MapperUtils.getBD(rs, 16));
            row.setRprofitcost(MapperUtils.getBD(rs, 17));
            row.setRgetprice(MapperUtils.getBD(rs, 18));
            row.setRgetcost(MapperUtils.getBD(rs, 19));
            row.setRsumext(MapperUtils.getBD(rs, 20));
            row.setRsalepricetax(MapperUtils.getBD(rs, 21));
            row.setRovwrprice(MapperUtils.getBD(rs, 22));

            row.setTypeRow(MapperUtils.getString(rs, 23));
            return row;
        }
    }

    @Override
    public List<Rnu51Row> getRnu51RowList(Exemplar ex) {
        try {
            LOG.debug("Start getRnu51RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r51\".num,\n" +
                            "\"r51\".codedeal as \"tradenumber\",\n" +
                            "\"r51\".typepaper as \"singsecurirty\",\n" +
                            "\"r51\".defpaper as \"issue\",\n" +
                            "\"r51\".dget as \"acquisitiondate\",\n" +
                            "\"r51\".dimpl as \"saledate\",\n" +
                            "\"r51\".numpaper as \"amountbonds\",\n" +
                            "\"r51\".salepriceperc as \"priceinfactperc\",\n" +
                            "\"r51\".rsaleprice as \"priceinfactrub\",\n" +
                            "\"r51\".getsalepricetax,\n" +
                            "\"r51\".getmpriceperc,\n" +
                            "\"r51\".getmprice,\n" +
                            "\"r51\".rmarketprice,\n" +
                            "\"r51\".marketpriceperc,\n" +
                            "\"r51\".rcost as \"expensesonsale\",\n" +
                            "\"r51\".rtotalcost as \"expensestotal\",\n" +
                            "\"r51\".rprofitcost as \"profit\",\n" +
                            "\"r51\".rgetprice,\n" +
                            "\"r51\".rgetcost,\n" +
                            "\"r51\".rsumext,\n" +
                            "\"r51\".rsalepricetax,\n" +
                            "\"r51\".rovwrprice\n," +
                            "\"r51\".typerow\n" +
                            "from migration.trd_51 \"r51\" \n" +
                            "inner join migration.exemplar \"ex\"  on \"r51\".fidexemplar = \"ex\".idexemplar\n" +
                            "left outer join migration.periodlist \"per\" on \"per\".idperiodlist = \"ex\".fidperiodlist\n" +
                            "left outer join migration.obj \"obj\" on \"ex\".fidobj = \"obj\".idobj\n" +
                            "left outer join migration.provider \"prov\" on \"obj\".fidprovider = \"prov\".idprovider\n" +
                            "left outer join migration.department \"dep\" on \"prov\".fiddepartment = \"dep\".id\n" +
                            "inner join migration.asystem \"sys\" on \"prov\".fidasystem = \"sys\".idasystem\n" +
                            "where \"ex\".idexemplar=?\n" +
                            "order by \"r51\".num asc, \"r51\".typerow desc",
                    new Object[]{ex.getExemplarId()},
                    new Rnu51RowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }
}
