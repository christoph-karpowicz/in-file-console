package com.dbw.diff;

import java.util.List;

import com.dbw.app.App;
import com.dbw.db.Operation;
import com.dbw.output.OutputBuilder;
import com.google.common.base.Strings;
import com.google.inject.Singleton;

@Singleton
public class TableDiffBuilder implements OutputBuilder {
    public static final short DEFAULT_MAX_COL_LENGTH = 17;
    public static final short DEFAULT_MAX_ROW_LENGTH = 120;

    private StringBuilder builder;

    public static short getMaxColumnLength() {
        return Strings.isNullOrEmpty(App.options.getMaxColumnLength()) ? DEFAULT_MAX_COL_LENGTH : Short.parseShort(App.options.getMaxColumnLength());
    }

    public static short getMaxRowLength() {
        return Strings.isNullOrEmpty(App.options.getMaxRowLength()) ? DEFAULT_MAX_ROW_LENGTH : Short.parseShort(App.options.getMaxRowLength());
    }
    
    public void init() {
        builder = new StringBuilder();
    }

    public void build(List<List<StateColumn>> stateRows, Operation dbOperation) {
        for (List<StateColumn> stateColumns : stateRows) {
            addHorizontalBorders(stateColumns);
            addColumnHeaders(stateColumns);
            switch (dbOperation) {
                case UPDATE:
                    buildUpdate(stateColumns);
                    break;
                case INSERT:
                    buildInsert(stateColumns);
                    break;
                case DELETE:
                    buildDelete(stateColumns);
                    break;
            }
            addHorizontalBorders(stateColumns);
        }
    }

    private void addHorizontalBorders(List<StateColumn> stateColumns) {
        stateColumns.forEach(stateColumn -> {
            builder.append(PADDING);
            builder.append(stateColumn.hasDiff() ? DIFF_HORIZONTAL_BORDER : PADDING);
            String border = "";
            String filler = stateColumn.hasDiff() ? DIFF_HORIZONTAL_BORDER : PADDING;
            int maxLength = DEFAULT_MAX_COL_LENGTH < stateColumn.getMaxLength() ? DEFAULT_MAX_COL_LENGTH : stateColumn.getMaxLength();
            for (short i = 0; i < maxLength; i++) {
                border += filler;
            }
            builder.append(border);
            builder.append(stateColumn.hasDiff() ? DIFF_HORIZONTAL_BORDER : PADDING);
            builder.append(PADDING);
        });
        builder.append(NEW_LINE);
    }

    private void addColumnHeaders(List<StateColumn> stateColumns) {
        stateColumns.forEach(stateColumn -> {
            append(stateColumn, stateColumn.getColumnName(), HEADER_UNDERLINE_PADDING);
        });
        builder.append(NEW_LINE);
    }

    private void buildUpdate(List<StateColumn> stateColumns) {
        stateColumns.forEach(stateColumn -> {
            append(stateColumn, stateColumn.getOldState(), PADDING);
        });
        builder.append(NEW_LINE);
        stateColumns.forEach(stateColumn -> {
            append(stateColumn, stateColumn.getNewState(), PADDING);
        });
        builder.append(NEW_LINE);
    }

    private void buildInsert(List<StateColumn> stateColumns) {
        stateColumns.forEach(stateColumn -> {
            append(stateColumn, stateColumn.getNewState(), PADDING);
        });
        builder.append(NEW_LINE);
    }

    private void buildDelete(List<StateColumn> stateColumns) {
        stateColumns.forEach(stateColumn -> {
            append(stateColumn, stateColumn.getOldState(), PADDING);
        });
        builder.append(NEW_LINE);
    }
    
    private void append(StateColumn stateColumn, String value, String padding) {
        builder.append(PADDING);
        builder.append(stateColumn.hasDiff() ? DIFF_VERTICAL_BORDER : PADDING);
        int maxLength = DEFAULT_MAX_COL_LENGTH < stateColumn.getMaxLength() ? DEFAULT_MAX_COL_LENGTH : stateColumn.getMaxLength();
        int substringLength = maxLength - ELLIPSIS.length();
        int valueLength = value.length();
        String finalValue = value;
        if (valueLength > maxLength) {
            finalValue = value.substring(0, substringLength) + ELLIPSIS;
            stateColumn.setCut(true);
        } else if (valueLength < maxLength) {
            int lengthDiff = substringLength + ELLIPSIS.length() - valueLength;
            for (short i = 0; i < lengthDiff; i++) {
                finalValue += padding;
            }
        }
        builder.append(finalValue);
        builder.append(stateColumn.hasDiff() ? DIFF_VERTICAL_BORDER : PADDING);
        builder.append(PADDING);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
    
}
