package com.dbw.actions;

import com.dbw.log.ErrorMessages;
import com.dbw.log.Level;
import com.dbw.log.Logger;
import com.dbw.log.WarningMessages;
import com.dbw.watcher.Watcher;

import java.sql.SQLException;

public class TruncateBasedOnLimitAction implements Runnable {
    private final Watcher watcher;
    private final int auditRecordCount;
    private final int opMin;

    public TruncateBasedOnLimitAction(Watcher watcher, int auditRecordCount, int opMin) {
        this.watcher = watcher;
        this.auditRecordCount = auditRecordCount;
        this.opMin = opMin;
    }

    @Override
    public void run() {
        int recordsToRemove = auditRecordCount - opMin;
        if (watcher.isRemovingAuditRecords()) {
            return;
        }
        watcher.setRemovingAuditRecords(true);
        try {
            watcher.getDb().deleteFirstNRows(recordsToRemove);
        } catch (SQLException e) {
            String errMsg = String.format(ErrorMessages.OP_LIMIT_REACHED_DELETE_ATTEMPT, recordsToRemove);
            Logger.log(Level.ERROR, watcher.getDb().getDbConfig().getName(), errMsg);
        }
        String warnMsg = String.format(WarningMessages.OP_LIMIT_REACHED, recordsToRemove, opMin);
        Logger.log(Level.WARNING, watcher.getDb().getDbConfig().getName(), warnMsg);
        watcher.setRemovingAuditRecords(false);
    }
}
