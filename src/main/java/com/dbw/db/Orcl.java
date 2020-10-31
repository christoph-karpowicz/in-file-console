package com.dbw.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dbw.cfg.DatabaseConfig;
import com.dbw.err.PreparationException;
import com.dbw.log.Level;
import com.dbw.log.Logger;
import com.dbw.log.LogMessages;

public class Orcl extends Database {
    public final static short STATE_COLUMN_MAX_LENGTH = 4000;
    public final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    public final String COL_NAME_ALIAS = "COLUMN_NAME";
    public final String DATA_TYPE_ALIAS = "DATA_TYPE";

    public Orcl(DatabaseConfig config) {
        super(config);
    }

    public void connect() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER);
        Connection conn = DriverManager.getConnection(getConnectionString(), config.getUser(), config.getPassword());
        setConn(conn);
        Logger.log(Level.INFO, LogMessages.DB_OPENED);
    }

    private String getConnectionString() {
        return config.getConnectionString();
    }

    public void prepare(List<String> watchedTables) throws PreparationException {
        OrclPrepareService orclPrepareService = new OrclPrepareService(this, watchedTables);
        orclPrepareService.prepare();
    }

    public boolean auditTableExists() throws SQLException {
        String[] stringArgs = {config.getUser().toUpperCase(), Common.DBW_AUDIT_TABLE_NAME};
        return objectExists(OrclQueries.FIND_AUDIT_TABLE, stringArgs);
    }

    public void createAuditTable() throws SQLException {
        executeUpdate(OrclQueries.CREATE_AUDIT_TABLE, Common.DBW_AUDIT_TABLE_NAME);
        Logger.log(Level.INFO, LogMessages.AUDIT_TABLE_CREATED);
    }

    public boolean auditTriggerExists(String tableName) throws SQLException {
        String[] stringArgs = {Common.DBW_PREFIX + tableName + Common.AUDIT_POSTFIX};
        return objectExists(OrclQueries.FIND_AUDIT_TRIGGER, stringArgs);
    }

    public void createAuditTrigger(String tableName, String query) throws SQLException {
        executeUpdate(query);
        Logger.log(Level.INFO, String.format(LogMessages.AUDIT_TRIGGER_CREATED, tableName));
    }

    public void dropAuditTrigger(String tableName) throws SQLException {
        executeUpdate(OrclQueries.DROP_AUDIT_TRIGGER, QueryBuilder.buildAuditTriggerName(tableName));
        Logger.log(Level.INFO, String.format(LogMessages.AUDIT_TRIGGER_DROPPED, tableName));
    }

    public String[] selectAuditTriggers() throws SQLException {
        String[] stringArgs = {};
        List<String> auditTriggers = selectStringArray(OrclQueries.SELECT_AUDIT_TRIGGERS, stringArgs);
        String[] auditTriggerNames = new String[auditTriggers.size()];
        for (short i = 0; i < auditTriggers.size(); i++) {
            String auditTriggerName = auditTriggers.get(i)
                .replace(Common.DBW_PREFIX, "")
                .replace(Common.AUDIT_POSTFIX, "");
            auditTriggerNames[i] = auditTriggerName;
        }
        return auditTriggerNames;
    }

    public Column[] selectTableColumns(String tableName) {
        List<Column> result = new ArrayList<Column>();
        try {
            PreparedStatement pstmt = getConn().prepareStatement(OrclQueries.SELECT_TABLE_COLUMNS);
            pstmt.setString(1, tableName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Column column = new Column();
                column.setName(rs.getString(COL_NAME_ALIAS));
                column.setDataType(rs.getString(DATA_TYPE_ALIAS));
                result.add(column);
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
        Column[] columns = new Column[result.size()];
        return result.toArray(columns);
    }

    public int selectMaxId() throws SQLException {
        return selectMaxId(OrclQueries.SELECT_AUDIT_TABLE_MAX_ID);
    }

    public List<AuditRecord> selectAuditRecords(int fromId) throws SQLException {
        return selectAuditRecords(OrclQueries.SELECT_AUDIT_RECORDS, fromId);
    }
 
    public void clean(List<String> watchedTables) throws SQLException {
        for (String tableName : watchedTables) {
            dropAuditTrigger(tableName);
        }
        dropAuditTable();
    }

    public void close() throws SQLException {
        getConn().close();
        Logger.log(Level.INFO, LogMessages.DB_CLOSED);
    }
    
}
