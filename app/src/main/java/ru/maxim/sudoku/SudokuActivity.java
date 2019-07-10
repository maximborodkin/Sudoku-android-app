package ru.maxim.sudoku;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import ru.maxim.sudoku.data.DBHelper;
import ru.maxim.sudoku.data.SudokuContract;

public class SudokuActivity extends AppCompatActivity implements View.OnClickListener {

    private int[][] sudoku = new int[9][9];
    private Sudoku currentSudoku;
    private Button currentBtn;
    SudokuGenerator sg;
    private Button scBtnCurrent;
    private int spacesCount;
    private boolean isSolved = false;
    DBHelper dbHelper;
    static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_sudoku));
        Button btn = findViewById(R.id.btn);
        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);
        Button btn3 = findViewById(R.id.btn3);
        Button btn4 = findViewById(R.id.btn4);
        Button btn5 = findViewById(R.id.btn5);
        Button btn6 = findViewById(R.id.btn6);
        Button btn7 = findViewById(R.id.btn7);
        Button btn8 = findViewById(R.id.btn8);
        Button btn9 = findViewById(R.id.btn9);
        Button scBtn5 = findViewById(R.id.scBtn5);
        Button scBtn10 = findViewById(R.id.scBtn10);
        Button scBtn15 = findViewById(R.id.scBtn15);
        Button scBtn20 = findViewById(R.id.scBtn20);
        Button scBtn25 = findViewById(R.id.scBtn25);
        Button newGame = findViewById(R.id.newGameBtn);
        btn.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);
        scBtn5.setOnClickListener(this);
        scBtn10.setOnClickListener(this);
        scBtn15.setOnClickListener(this);
        scBtn20.setOnClickListener(this);
        scBtn25.setOnClickListener(this);
        newGame.setOnClickListener(this);
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        spacesCount = Integer.parseInt(
                Objects.requireNonNull(
                        PreferenceManager.getDefaultSharedPreferences(
                                getBaseContext()
                        ).getString("spacesCount", "15"))
        );
        //setSC(15, scBtn15.getId());
        Intent intent = getIntent();
        if (intent.hasExtra("sudoku_hash")) {
            resumeGame((int) Objects.requireNonNull(intent.getExtras()).get("sudoku_hash"));
        }
        else{
            if (currentSudoku == null) startGame();
            else resumeGame(currentSudoku.hashCode());
        }
    }

    public void addSudoku (Sudoku sudoku){
        int hashCode = sudoku.hashCode();
        String original_field = Sudoku.getJSONArray(sudoku.getOriginal_field());
        String modyfied_field = Sudoku.getJSONArray(sudoku.getModyfied_field());
        String creation_date = sudoku.getCreation_date();
        String last_modify_date = sudoku.getLast_modify_date();

        ContentValues values = new ContentValues();
        values.put(SudokuContract.SudokuEntry.COLUMN_HASH, hashCode);
        values.put(SudokuContract.SudokuEntry.ORIGINAL_FIELD, original_field);
        values.put(SudokuContract.SudokuEntry.MODYFIED_FIELD, modyfied_field);
        values.put(SudokuContract.SudokuEntry.CREATION_DATE, creation_date);
        values.put(SudokuContract.SudokuEntry.LAST_MODIFY_DATE, last_modify_date);
        db.insert(SudokuContract.SudokuEntry.TABLE_NAME, null, values);
    }

    public void updateSudoku(Sudoku newSudoku){
        ContentValues cv = new ContentValues();
        cv.put(SudokuContract.SudokuEntry.MODYFIED_FIELD, Sudoku.getJSONArray(newSudoku.getModyfied_field()));
        cv.put(SudokuContract.SudokuEntry.LAST_MODIFY_DATE, newSudoku.getLast_modify_date());
        db.update(SudokuContract.SudokuEntry.TABLE_NAME, cv, SudokuContract.SudokuEntry.COLUMN_HASH + " = ?", new String[]{String.valueOf(newSudoku.hashCode())});
    }

    @Override
    protected void onDestroy() {
        currentSudoku.setModyfied_field(sudoku);
        currentSudoku.setLast_modify_date(SimpleDateFormat.getDateTimeInstance().format(new Date()));
        updateSudoku(currentSudoku);
        super.onDestroy();
    }

    public void deleteSudoku(int hashCode){
        db.delete(SudokuContract.SudokuEntry.TABLE_NAME, "hash = " + hashCode, null);
    }

    private void startGame(){
        Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
        sg = new SudokuGenerator(spacesCount);
        currentSudoku = new Sudoku(sg.getSudoku());
        for (int i = 0; i < sudoku.length; i++) {
            sudoku[i] = Arrays.copyOfRange(currentSudoku.getModyfied_field()[i], 0, currentSudoku.getModyfied_field()[i].length);
        }
        isSolved = false;
        drawGrid();
        setClickable(true);
        addSudoku(new Sudoku(sudoku));
    }

    private void resumeGame(int old_game_hash){
        // find target sudoku in db
        Toast.makeText(this, "resume", Toast.LENGTH_SHORT).show();
        Cursor cursor = db.query(
                SudokuContract.SudokuEntry.TABLE_NAME,
                null,
                SudokuContract.SudokuEntry.COLUMN_HASH + " = ?",
                new String[]{String.valueOf(old_game_hash)},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        String original_field = cursor.getString(cursor.getColumnIndex(SudokuContract.SudokuEntry.ORIGINAL_FIELD));
        String modyfied_field = cursor.getString(cursor.getColumnIndex(SudokuContract.SudokuEntry.MODYFIED_FIELD));
        String creation_date = cursor.getString(cursor.getColumnIndex(SudokuContract.SudokuEntry.CREATION_DATE));
        String last_modify_date = cursor.getString(cursor.getColumnIndex(SudokuContract.SudokuEntry.LAST_MODIFY_DATE));
        cursor.close();
        currentSudoku = new Sudoku(Sudoku.getArray(original_field), Sudoku.getArray(modyfied_field), creation_date, last_modify_date);
        for (int i = 0; i < sudoku.length; i++) {
            sudoku[i] = Arrays.copyOf(currentSudoku.getModyfied_field()[i], currentSudoku.getModyfied_field()[i].length);
        }
        // draw new field on Activity
        setClickable(true);
        drawGrid();
        // ФЗЩОУККЦЗЩК4З5
    }

    private void congrats(){
        Toast.makeText(this, getString(R.string.congrats), Toast.LENGTH_SHORT).show();
        isSolved = true;
        setClickable(false);
        deleteSudoku(currentSudoku.hashCode());
    }

    private void setClickable(boolean clickable){
        TableLayout parent;
        TableRow tr;
        Button btn;

        parent= findViewById(R.id.parent_layout);
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (i !=3 && i !=7) {
                tr = (TableRow) parent.getChildAt(i);
                for (int j = 0; j < tr.getChildCount(); j++) {
                    if (j != 3 && j!=7){
                        btn = (Button) tr.getChildAt(j);
                        if (!clickable) btn.setBackgroundResource(R.drawable.soluted_grid_button);
                        btn.setClickable(clickable);
                    }
                }
            }
        }
        parent = findViewById(R.id.digit_buttons);
        for (int i = 0; i < parent.getChildCount()-2; i++) {
            tr = (TableRow) parent.getChildAt(i);
            for (int j = 0; j < tr.getChildCount(); j++) {
                btn = (Button) tr.getChildAt(j);
                btn.setClickable(clickable);
            }
        }
    }

    private void drawGrid(){
        TableLayout parent = findViewById(R.id.parent_layout);
        parent.removeAllViews();
        TableRow row;
        Button btn;
        int[][] original_field = currentSudoku.getOriginal_field();
        int[][] modyfied_field = currentSudoku.getModyfied_field();

        for (int i = 0; i < 9; i++) {
            row = new TableRow(this);
            row.setId(200000+i);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            parent.addView(row);
            for (int j = 0; j < 9; j++) {
                btn = new Button(this);
                btn.setId(300000 + i*10 +j);
                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1.0f);
                btn.setLayoutParams(params);
                if (original_field[i][j] != 0){
                    btn.setText(String.valueOf(original_field[i][j]));
                    btn.setBackgroundResource(R.drawable.unclickable_grid_button);
                    btn.setClickable(false);
                }else{
                    btn.setText((modyfied_field[i][j] == 0)? "" : String.valueOf(modyfied_field[i][j]));
                    btn.setBackgroundResource(R.drawable.clickable_grid_button);
                    btn.setClickable(true);
                    btn.setOnClickListener(this);
                }
                row.addView(btn);
                if (j == 2 || j == 5){
                    btn = new Button(this);
                    btn.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT,
                            1.73f));
                    btn.setClickable(false);
                    btn.setBackgroundResource(R.drawable.vertical_spacer);
                    row.addView(btn);
                }
            }
            if (i == 2 || i == 5){
                TableRow spacer = new TableRow(this);
                spacer.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        1.0f));
                TextView space = new TextView(this);
                space.setTextSize(TypedValue.COMPLEX_UNIT_SP, 3);
                spacer.addView(space);
                parent.addView(spacer);
            }
        }
    }

    private void setDigit(int digit){
        if (currentBtn != null) {
            currentBtn.setText((digit == 0) ? "" : String.valueOf(digit));
            int i = ((currentBtn.getId()) - 300000) / 10;
            int j = ((currentBtn.getId()) - 300000) % 10;
            sudoku[i][j] = digit;
            if (checkSolution()) {
                congrats();
            }
        }
    }

    boolean useInSquare(int digit_i, int digit_j, int digit){
        int rowStart = (digit_i/3)*3;
        int colStart = (digit_j/3)*3;
        for (int i = rowStart; i < rowStart+3; i++) {
            for (int j = colStart; j < colStart+3; j++) {
                if (sudoku[i][j] == digit && i != digit_i && j != digit_j){
                    return false;
                }
            }
        }
        return true;
    }

    boolean useInRow(int row, int col,  int digit){
        for (int i = 0; i < 9; i++) {
            if (sudoku[row][i] == digit && i != col){
                return false;
            }
        }
        return true;
    }

    boolean useInColumn(int row, int col, int digit){
        for (int i = 0; i < 9; i++) {
            if (sudoku[i][col] == digit && i != row){
                return false;
            }
        }
        return true;
    }

    private boolean checkSolution() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sudoku[i][j] == 0){
                    return false;
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!useInRow(i, j, sudoku[i][j])
                        || !useInColumn(i, j, sudoku[i][j])
                        || !useInSquare(i, j, sudoku[i][j])
                ){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn:
                setDigit(0);
                break;
            case R.id.btn1:
                setDigit(1);
                break;
            case R.id.btn2:
                setDigit(2);
                break;
            case R.id.btn3:
                setDigit(3);
                break;
            case R.id.btn4:
                setDigit(4);
                break;
            case R.id.btn5:
                setDigit(5);
                break;
            case R.id.btn6:
                setDigit(6);
                break;
            case R.id.btn7:
                setDigit(7);
                break;
            case R.id.btn8:
                setDigit(8);
                break;
            case R.id.btn9:
                setDigit(9);
                break;
            case R.id.scBtn5:
                setSC(1, v.getId());
                break;
            case R.id.scBtn10:
                setSC(10, v.getId());
                break;
            case R.id.scBtn15:
                setSC(15, v.getId());
                break;
            case R.id.scBtn20:
                setSC(20, v.getId());
                break;
            case R.id.scBtn25:
                setSC(25, v.getId());
                break;
            case R.id.newGameBtn:
                if (!isSolved) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.start_new_game))
                            .setMessage(getString(R.string.are_ypu_sure))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startGame();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }else{
                    startGame();
                }
                break;
            default:
                if (currentBtn != null){
                    currentBtn.setBackgroundResource(R.drawable.clickable_grid_button);
                }
                currentBtn = findViewById(v.getId());
                currentBtn.setBackgroundResource(R.drawable.current_grid_button);
                break;
        }
    }

    private void setSC(int sc, int btnId) {
        if (scBtnCurrent != null){
            scBtnCurrent.setBackgroundResource(R.drawable.sc_button);
        }
        scBtnCurrent = findViewById(btnId);
        scBtnCurrent.setBackgroundResource(R.drawable.sc_button_current);
        spacesCount = sc;
    }
}
