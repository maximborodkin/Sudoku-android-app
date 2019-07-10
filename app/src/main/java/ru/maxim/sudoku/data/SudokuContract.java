package ru.maxim.sudoku.data;

import android.provider.BaseColumns;

public final class SudokuContract {
    public SudokuContract() {}

    public static final class SudokuEntry implements BaseColumns{
        public static final String TABLE_NAME = "sudoku";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_HASH = "hash";
        public static final String ORIGINAL_FIELD = "original_field";
        public static final String MODYFIED_FIELD = "modyfied_field";
        public static final String CREATION_DATE = "creation_date";
        public static final String LAST_MODIFY_DATE = "last_modify_date";

    }
}
