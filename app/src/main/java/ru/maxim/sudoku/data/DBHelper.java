package ru.maxim.sudoku.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_SUDOKU_TABLE = "CREATE TABLE " + SudokuContract.SudokuEntry.TABLE_NAME + "("
                + SudokuContract.SudokuEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SudokuContract.SudokuEntry.COLUMN_HASH + " INTEGER NOT NULL, "
                + SudokuContract.SudokuEntry.ORIGINAL_FIELD + " TEXT NOT NULL, "
                + SudokuContract.SudokuEntry.MODYFIED_FIELD + " TEXT NOT NULL, "
                + SudokuContract.SudokuEntry.CREATION_DATE + " TEXT NOT NULL, "
                + SudokuContract.SudokuEntry.LAST_MODIFY_DATE + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_SUDOKU_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
