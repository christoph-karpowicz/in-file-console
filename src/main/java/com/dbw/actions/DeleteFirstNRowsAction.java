package com.dbw.actions;

import com.dbw.db.DatabaseManager;
import com.dbw.err.DbwException;
import com.google.inject.Inject;

public class DeleteFirstNRowsAction {
    @Inject
    private DatabaseManager databaseManager;

    private final String numberOfRowsToDelete;

    public DeleteFirstNRowsAction(String numberOfRowsToDelete) {
        this.numberOfRowsToDelete = numberOfRowsToDelete;
    }

    public void execute() throws DbwException {
        databaseManager.deleteFirstNRows(numberOfRowsToDelete);
    }
}
