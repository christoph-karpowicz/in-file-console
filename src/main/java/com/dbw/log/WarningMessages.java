package com.dbw.log;

import com.dbw.cli.Opts;

public class WarningMessages extends Messages {
    public final static String LATEST_OPS_NUM_GT_AUDIT_RECORD_COUNT = addCliFlagInfo(Opts.SHOW_LATEST_OP) + "Requested number of latest operations is greater than the audit record count. Outputting all operations...";
    public final static String NO_LATEST_OPS = addCliFlagInfo(Opts.SHOW_LATEST_OP) + "Last n changes flag given but there are no audit records.";
    public final static String NO_CACHE_FILE = "Cache file not found.";
    public final static String CLEAR_CACHE_NOT_FOUND = addCliFlagInfo(Opts.CLEAR_CACHE) + "Clear config cache request: %s not found in cache.";
}
