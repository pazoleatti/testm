package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.MigrationDao;

import com.aplana.sbrf.taxaccounting.dao.impl.util.MapperUtils;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.row.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class MigrationDaoImpl extends AbstractDao implements MigrationDao {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String ACTUAL_EXEMPLAR_NOT_FOUND_MESSAGE = "Актуального экземпляра записей для типа РНУ - %d не найдено в БД";

    private class ExemplarRowMapper implements RowMapper<Exemplar> {
        public Exemplar mapRow(ResultSet rs, int index) throws SQLException {
            Exemplar exemplar = new Exemplar();

            exemplar.setExemplarId(rs.getLong(1));
            exemplar.setPeriodityId(rs.getInt(2));
            exemplar.setBeginDate(rs.getDate(3));
            exemplar.setEndDate(rs.getDate(4));
            exemplar.setRnuTypeId(rs.getInt(5));
            exemplar.setDepCode(rs.getString(6));
            exemplar.setSystemId(rs.getInt(7));
            exemplar.setSubSystemId(rs.getString(8));
            exemplar.setTerCode(rs.getString(9));

            return exemplar;
        }
    }


    @Override
    public List<Exemplar> getActualExemplarByRnuType(long rnuTypeId) {
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
                            "\"ex\".typeexemplar like 'ACTUAL' and\n" +
                            "\"objdict\".idobjdict = ?",
                    new Object[]{rnuTypeId},
                    new ExemplarRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(ACTUAL_EXEMPLAR_NOT_FOUND_MESSAGE, rnuTypeId);
        }
    }


    private class Rnu25RowMapper implements RowMapper<Rnu25Row> {
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
            logger.debug("Start getRnu25RowList with " + ex);
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
            logger.debug("Start getRnu26RowList with " + ex);
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
            logger.debug("Start getRnu27RowList with " + ex);
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
        public Rnu31Row mapRow(ResultSet rs, int index) throws SQLException {
            Rnu31Row row = new Rnu31Row();

            row.setNum(MapperUtils.getLong(rs, 1));
            row.setCodeTypePaper(MapperUtils.getLong(rs, 2));
            row.setTypePaper(MapperUtils.getString(rs, 3));
            row.setPercCashOfz(MapperUtils.getBD(rs, 6));
            row.setPercCashEuro(MapperUtils.getBD(rs, 6));
            row.setPercCashFed(MapperUtils.getBD(rs, 6));
            row.setPercCashOgvz(MapperUtils.getBD(rs, 6));
            row.setPercCashOther(MapperUtils.getBD(rs, 6));
            row.setPercCashCorp(MapperUtils.getBD(rs, 6));
            row.setTypeRow(MapperUtils.getString(rs, 18));
            return row;
        }
    }

    @Override
    public List<Rnu31Row> getRnu31RowList(Exemplar ex) {
        try {
            logger.debug("Start getRnu31RowList with " + ex);
            return getJdbcTemplate().query(
                    "select\n" +
                            "\"r31\".num,\n" +
                            "\"r31\".codetypepaper,\n" +
                            "\"r31\".typepaper,\n" +
                            "\"r31\".perccashofz,\n" +
                            "\"r31\".perccasheuro,\n" +
                            "\"r31\".perccashfed,\n" +
                            "\"r31\".perccashogvz,\n" +
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
            logger.debug("Start getRnu64RowList with " + ex);
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

    private class Rnu60RowMapper implements RowMapper<Rnu60Row> {
        public Rnu60Row mapRow(ResultSet rs, int index) throws SQLException {
            Rnu60Row row = new Rnu60Row();

            row.setNum(MapperUtils.getLong(rs, 1));

            row.setNumDeal(MapperUtils.getString(rs, 2));
            row.setDefPaper(MapperUtils.getString(rs, 3));
            row.setCodecurrency(MapperUtils.getString(rs, 4));
            row.setNompaper(MapperUtils.getBD(rs, 5));
            row.setDrepo1(MapperUtils.getDate(rs, 6));
            row.setDrepo1(MapperUtils.getDate(rs, 7));
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
    public List<Rnu60Row> getRnu60RowList(Exemplar ex) {
        try {
            logger.debug("Start getRnu60RowList with " + ex);
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
                    new Rnu60RowMapper()
            );
        } catch (DataRetrievalFailureException e) {
            throw new DaoException("Error " + e.getLocalizedMessage(), ex.getExemplarId());
        }
    }
}
