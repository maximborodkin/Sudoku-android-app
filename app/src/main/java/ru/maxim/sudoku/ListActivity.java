package ru.maxim.sudoku;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import ru.maxim.sudoku.data.DBHelper;
import ru.maxim.sudoku.data.SudokuContract;

public class ListActivity extends AppCompatActivity {
    DBHelper dbHelper;
    SQLiteDatabase db;
    ListView listView;
    ArrayList<Sudoku> sudokus;
    final int MENU_RENAME = 0;
    final int MENU_DELETE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_list));
        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();
        listView = findViewById(R.id.sudoku_list);
        fillList();
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListActivity.this, SudokuActivity.class);
                intent.putExtra("sudoku_hash", parent.getItemAtPosition(position).hashCode());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        fillList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_delete:
                if (sudokus.size() == 0) return false;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.delete_all_fields);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.delete(SudokuContract.SudokuEntry.TABLE_NAME, null, null);
                        startActivity(new Intent(ListActivity.this, MainActivity.class));
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.sudoku_list){
            menu.add(0, MENU_RENAME, 0, R.string.rename);
            menu.add(0, MENU_DELETE, 0, R.string.delete);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case MENU_RENAME:
                //Rename sudoku
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.rename);
                final EditText input = new EditText(this);
                builder.setView(input);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().length() != 0) {
                            ContentValues cv = new ContentValues();
                            cv.put(SudokuContract.SudokuEntry.CREATION_DATE, input.getText().toString());
                            db.update(SudokuContract.SudokuEntry.TABLE_NAME, cv, SudokuContract.SudokuEntry.COLUMN_HASH + " = ?", new String[]{String.valueOf(sudokus.get(info.position).hashCode())});
                            fillList();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
            case MENU_DELETE:
                db.delete(SudokuContract.SudokuEntry.TABLE_NAME, "hash = " + sudokus.get(info.position).hashCode(), null);
                fillList();
        }
        return super.onContextItemSelected(item);
    }


    private void fillList() {
        Cursor cursor = db.query(SudokuContract.SudokuEntry.TABLE_NAME, null, null, null, null, null, null);
        sudokus = new ArrayList<>();
        while (cursor.moveToNext()){
            String original_field = cursor.getString(cursor.getColumnIndex(SudokuContract.SudokuEntry.ORIGINAL_FIELD));
            String modyfied_field = cursor.getString(cursor.getColumnIndex(SudokuContract.SudokuEntry.MODYFIED_FIELD));
            String creation_date = cursor.getString(cursor.getColumnIndex(SudokuContract.SudokuEntry.CREATION_DATE));
            String last_modify_date = cursor.getString(cursor.getColumnIndex(SudokuContract.SudokuEntry.LAST_MODIFY_DATE));
            sudokus.add(new Sudoku(Sudoku.getArray(original_field), Sudoku.getArray(modyfied_field), creation_date, last_modify_date));

        }
        cursor.close();
        listView.setAdapter(new SudokuListAdapter(this, sudokus));
    }
}
