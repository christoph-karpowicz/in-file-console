package com.dbw.cli;

import com.dbw.err.UnrecoverableException;
import com.dbw.log.ErrorMessages;
import com.dbw.util.StringUtils;
import com.google.common.collect.Sets;
import org.apache.commons.cli.*;

import java.util.Optional;
import java.util.Set;

public class CLI {
    private CommandLineParser parser;
    private Options options;
    private CommandLine cmd;
    private String[] args;

    public void init(String[] args) throws ParseException, UnrecoverableException {
        setArgs(args);
        parser = new DefaultParser();
        options = new Opts();
        setCmd();
    }

    public ParsedOptions handleArgs() throws UnrecoverableException {
        ParsedOptions parsedOptions = parseArgs();
        if (parsedOptions.getShowHelp()) {
            printHelp();
            System.exit(0);
        }
        return parsedOptions;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(Opts.HELP_USAGE, options);
    }

    private void setCmd() throws ParseException {
        cmd = parser.parse(options, args);
    }

    private void setArgs(String[] args) {
        this.args = args;
    }

    public ParsedOptions parseArgs() throws UnrecoverableException {
        ParsedOptions parsedOptions = new ParsedOptions();
        parsedOptions.clearCache = getClearCache();
        parsedOptions.configPaths = getConfigOption();
        parsedOptions.debug = getDebugOption();
        parsedOptions.showHelp = getShowHelpOption();
        parsedOptions.purge = getPurgeOption();
        parsedOptions.reuseConfig = getReuseConfigOption();
        parsedOptions.showQuery = getQueryOption();
        parsedOptions.verboseDiff = getVerboseDiff();
        try {
            parsedOptions.deleteFirstNRows = getDeleteFirstNRowsOption();
            parsedOptions.interval = getInterval();
            parsedOptions.maxColumnWidth = getMaxColumnWidthOption();
            parsedOptions.maxRowWidth = getMaxRowWidthOption();
            parsedOptions.oneOff = getOneOff();
            parsedOptions.showLatestOperations = getShowLatestOperationsOption();
            parsedOptions.tables = getTables();
            parsedOptions.timeDiffSeparatorMinVal = getTimeDiffSeparatorMinVal();
        } catch (Exception e) {
            throw new UnrecoverableException("InvalidCLIOptionInput", e.getMessage(), e, parsedOptions.debug);
        }
        return parsedOptions;
    }

    private boolean getClearCache() {
        return cmd.hasOption(Opts.CLEAR_CACHE);
    }

    private Optional<Set<String>> getConfigOption() {
        if (cmd.hasOption(Opts.CONFIG)) {
            Set<String> configPaths = listOptionToSet(Opts.CONFIG);
            return Optional.of(configPaths);
        }
        return Optional.empty();
    }
    
    private boolean getDebugOption() {
        return cmd.hasOption(Opts.DEBUG);
    }

    private String getDeleteFirstNRowsOption() throws Exception {
        if (cmd.hasOption(Opts.DELETE_FIRST_N_ROWS)) {
            String value = cmd.getOptionValue(Opts.DELETE_FIRST_N_ROWS);
            if (!StringUtils.isNumeric(value) && !value.trim().equals(Opts.ALL_SYMBOL)) {
                throw new Exception(ErrorMessages.CLI_INVALID_DELETE_N_ROWS);
            }
            return value;
        }
        return null;
    }

    private boolean getShowHelpOption() {
        return cmd.hasOption(Opts.SHOW_HELP);
    }

    private Short getInterval() throws Exception {
        if (cmd.hasOption(Opts.INTERVAL)) {
            String optionValue = cmd.getOptionValue(Opts.INTERVAL);
            Short value = Short.parseShort(optionValue);
            if (value < 10) {
                throw new Exception(ErrorMessages.CLI_INVALID_INTERVAL_SMALL);
            }
            if (value > 10000) {
                throw new Exception(ErrorMessages.CLI_INVALID_INTERVAL_BIG);
            }
            return value;
        }
        return null;
    }

    private Short getMaxColumnWidthOption() throws Exception {
        if (cmd.hasOption(Opts.MAX_COL_WIDTH)) {
            String optionValue = cmd.getOptionValue(Opts.MAX_COL_WIDTH);
            Short value = Short.parseShort(optionValue);
            if (value <= 3) {
                throw new Exception(ErrorMessages.CLI_INVALID_MAX_COLUMN_WIDTH);
            }
            return value;
        }
        return null;
    }

    private Short getMaxRowWidthOption() throws Exception {
        if (cmd.hasOption(Opts.MAX_ROW_WIDTH)) {
            String optionValue = cmd.getOptionValue(Opts.MAX_ROW_WIDTH);
            Short value = Short.parseShort(optionValue);
            if (value <= 10) {
                throw new Exception(ErrorMessages.CLI_INVALID_MAX_ROW_WIDTH);
            }
            return value;
        }
        return null;
    }

    private boolean getOneOff() throws Exception {
        boolean hasOneOffOption = cmd.hasOption(Opts.ONE_OFF);
        if (hasOneOffOption && !cmd.hasOption(Opts.LATEST)) {
            throw new Exception(ErrorMessages.CLI_ONE_OFF_NO_LASTEST_OP);
        }
        return hasOneOffOption;
    }

    private boolean getPurgeOption() {
        return cmd.hasOption(Opts.PURGE);
    }

    private boolean getReuseConfigOption() {
        return cmd.hasOption(Opts.REUSE_CONFIG);
    }

    private boolean getQueryOption() {
        return cmd.hasOption(Opts.QUERY);
    }

    private ShowLatestOperationsOption getShowLatestOperationsOption() throws Exception {
        if (cmd.hasOption(Opts.LATEST)) {
            String value = cmd.getOptionValue(Opts.LATEST);
            return ShowLatestOperationsOption.create(value);
        }
        return null;
    }

    private Optional<Set<String>> getTables() {
        if (cmd.hasOption(Opts.TABLES)) {
            Set<String> tables = listOptionToSet(Opts.TABLES);
            return Optional.of(tables);
        }
        return Optional.empty();
    }

    private Integer getTimeDiffSeparatorMinVal() throws Exception {
        if (cmd.hasOption(Opts.TIME_DIFF_SEPARATOR)) {
            String optionValue = cmd.getOptionValue(Opts.TIME_DIFF_SEPARATOR);
            Integer value = Integer.parseInt(optionValue);
            if (value < 0) {
                throw new Exception(ErrorMessages.CLI_TIME_DIFF_SEP_LT_ZERO);
            }
            return value;
        }
        return null;
    }

    private boolean getVerboseDiff() {
        return cmd.hasOption(Opts.VERBOSE_DIFF);
    }

    private Set<String> listOptionToSet(String opt) {
        String option = cmd.getOptionValue(opt);
        String[] values = option.split(",");
        return Sets.newHashSet(values);
    }

    public class ParsedOptions {
        private boolean clearCache;
        private Optional<Set<String>> configPaths;
        private boolean debug;
        private String deleteFirstNRows;
        private boolean showHelp;
        private Short interval;
        private Short maxColumnWidth;
        private Short maxRowWidth;
        private boolean oneOff;
        private boolean purge;
        private boolean reuseConfig;
        private boolean showQuery;
        private ShowLatestOperationsOption showLatestOperations;
        private Optional<Set<String>> tables;
        private Integer timeDiffSeparatorMinVal;
        private boolean verboseDiff;

        public boolean getClearCache() {
            return clearCache;
        }

        public Optional<Set<String>> getConfigPaths() {
            return configPaths;
        }
        
        public boolean getDebug() {
            return debug;
        }

        public String getDeleteFirstNRows() {
            return deleteFirstNRows;
        }

        public boolean getShowHelp() {
            return showHelp;
        }

        public Short getInterval() {
            return interval;
        }

        public Short getMaxColumnWidth() {
            return maxColumnWidth;
        }

        public Short getMaxRowWidth() {
            return maxRowWidth;
        }

        public boolean getOneOff() {
            return oneOff;
        }

        public boolean getPurge() {
            return purge;
        }

        public boolean getReuseConfig() {
            return reuseConfig;
        }

        public boolean getShowQuery() {
            return showQuery;
        }

        public ShowLatestOperationsOption getShowLatestOperations() {
            return showLatestOperations;
        }

        public boolean showLatestOperationsPresentAndGtThanZero() {
            return showLatestOperations != null && showLatestOperations.getValue() > 0;
        }

        public Optional<Set<String>> getTables() {
            return tables;
        }

        public Integer getTimeDiffSeparatorMinVal() {
            return timeDiffSeparatorMinVal;
        }

        public boolean getVerboseDiff() {
            return verboseDiff;
        }
    }
}
