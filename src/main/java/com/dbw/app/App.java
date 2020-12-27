package com.dbw.app;

import java.sql.SQLException;
import java.util.Objects;

import com.dbw.cfg.Config;
import com.dbw.cfg.ConfigParser;
import com.dbw.cfg.DatabaseConfig;
import com.dbw.cli.CLI;
import com.dbw.db.Database;
import com.dbw.db.DatabaseFactory;
import com.dbw.err.AppInitException;
import com.dbw.err.InitialAuditRecordDeleteException;
import com.dbw.err.InvalidCLIOptionInputException;
import com.dbw.err.PreparationException;
import com.dbw.err.UnknownDbTypeException;
import com.dbw.err.WatcherStartException;
import com.dbw.log.ErrorMessages;
import com.dbw.log.Level;
import com.dbw.log.LogMessages;
import com.dbw.log.Logger;
import com.dbw.log.SuccessMessages;
import com.dbw.watcher.AuditTableWatcher;
import com.google.inject.Inject;
import org.apache.commons.cli.ParseException;

public class App {
    @Inject
    private AuditTableWatcher watcher;
    
    public static CLI.ParsedOptions options;
    private Config config;
    private Database db;
    
    public void init(String[] args) throws AppInitException {
        try {
            options = handleArgs(args);
            config = ConfigParser.fromYMLFile(options.getConfigPath());
            setDb();
            connectToDb();
        } catch (Exception e) {
            throw new AppInitException(e.getMessage(), e);
        }
    }
    
    private CLI.ParsedOptions handleArgs(String[] args) throws ParseException, InvalidCLIOptionInputException {
        CLI cli = new CLI();
        cli.setArgs(args);
        cli.init();
        return cli.parseArgs();
    }

    private void setDb() throws UnknownDbTypeException {
        DatabaseConfig dbConfig = config.getDatabase();
        db = DatabaseFactory.getDatabase(dbConfig);
    }

    private void connectToDb() throws SQLException, ClassNotFoundException {
        db.connect();
    }

    public void start() throws WatcherStartException, InitialAuditRecordDeleteException {
        String deleteFirstNRowsOption = options.getDeleteFirstNRows();
        if (!Objects.isNull(deleteFirstNRowsOption)) {
            deleteFirstNRows(deleteFirstNRowsOption);
        }
        
        if (options.getPurge()) {
            purge();
        } else {
            addShutdownHook();
            startWatcher();
        }
    }

    private void deleteFirstNRows(String nRows) throws InitialAuditRecordDeleteException {
        String successMessage;
        try {
            successMessage = db.deleteFirstNRows(nRows);
        } catch (SQLException e) {
            throw new InitialAuditRecordDeleteException(e.getMessage(), e);
        }
        Logger.log(Level.INFO, SuccessMessages.format(successMessage, nRows));
    }

    private void purge() {
        if (db.purge(config.getTables())) {
            Logger.log(Level.INFO, SuccessMessages.CLI_PURGE);
        } else {
            Logger.log(Level.ERROR, ErrorMessages.CLI_PURGE);
        }
    }

    private void startWatcher() throws WatcherStartException {
        try {
            watcher.setWatchedTables(config.getTables());
            watcher.setDb(db);
            watcher.init();
            watcher.start();
        } catch (PreparationException | SQLException e) {
            throw new WatcherStartException(e.getMessage(), e);
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Logger.log(Level.INFO, LogMessages.SHUTDOWN);
                try {
                    shutdown();
                } catch (SQLException e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
            }
        });
    }

    private void shutdown() throws SQLException {
        db.close();
    }
}
