package com.dbw.cli;

import com.dbw.log.ErrorMessages;
import com.dbw.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowLatestOperationsOption {
    private static final long MINUTE_IN_SECONDS = 60;
    private static final long HOUR_IN_SECONDS = MINUTE_IN_SECONDS * 60;

    private final String raw;
    private boolean isTime;
    private long value;

    private ShowLatestOperationsOption(String raw) {
        this.raw = raw;
    }

    public String getRaw() {
        return raw;
    }

    public boolean isTime() {
        return isTime;
    }

    public void setIsTime(boolean isTime) {
        this.isTime = isTime;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public static ShowLatestOperationsOption create(String input) throws Exception {
        ShowLatestOperationsOption opt = new ShowLatestOperationsOption(input);
        if (StringUtils.isNumeric(input)) {
            opt.setIsTime(false);
            opt.setValue(Short.parseShort(input));
        } else {
            if (!opt.isValid()) {
                throw new Exception(ErrorMessages.CLI_INVALID_LATEST_OP);
            }
            long minutesMultiplier = input.contains("m") ? MINUTE_IN_SECONDS : 1;
            long hoursMultiplier = input.contains("h") ? HOUR_IN_SECONDS : 1;
            long extractedNumber = Short.parseShort(input.substring(0, input.length() - 1));
            long timeInSeconds = extractedNumber * minutesMultiplier * hoursMultiplier;
            opt.setIsTime(true);
            opt.setValue(timeInSeconds);
        }
        return opt;
    }

    private boolean isValid() {
        Pattern validationPattern = Pattern.compile("^\\d+(s|m|h){1}$");
        Matcher validationMatcher = validationPattern.matcher(raw.trim());
        return validationMatcher.matches();
    }
}
