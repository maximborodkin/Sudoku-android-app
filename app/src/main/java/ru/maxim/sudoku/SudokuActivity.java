package ru.maxim.sudoku;

import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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
    private int spacesCount;
    private boolean isHelpEnabled;
    private boolean isHighlightEnabled;
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
        Button newGame = findViewById(R.id.newGameBtn);
        Button resetField = findViewById(R.id.resetFieldBtn);
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
        newGame.setOnClickListener(this);
        resetField.setOnClickListener(this);
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        spacesCount = Integer.parseInt(
                Objects.requireNonNull(
                        PreferenceManager.getDefaultSharedPreferences(
                                getBaseContext()
                        ).getString("spacesCount", "15"))
        );
        isHelpEnabled = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("help", false);
        isHighlightEnabled = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("highlight", false);
        if (getLastCustomNonConfigurationInstance() != null){
            currentSudoku = (Sudoku) getLastCustomNonConfigurationInstance();
        }else if (getIntent().hasExtra("sudoku_hash")){
            currentSudoku = findSudokuByHashcode((int) Objects.requireNonNull(getIntent().getExtras()).get("sudoku_hash"));
        }else{
            currentSudoku = new Sudoku(new SudokuGenerator(spacesCount).getSudoku());
            addSudoku(currentSudoku);
        }
        startGame();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        super.onRetainCustomNonConfigurationInstance();
        return currentSudoku;
    }

    public Sudoku findSudokuByHashcode(int hashCode){
        Cursor cursor = db.query(
                SudokuContract.SudokuEntry.TABLE_NAME,
                null,
                SudokuContract.SudokuEntry.COLUMN_HASH + " = ?",
                new String[]{String.valueOf(hashCode)},
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
        return new Sudoku(Sudoku.getArray(original_field), Sudoku.getArray(modyfied_field), creation_date, last_modify_date);
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

    private void resetField(){
        for (int i = 0; i < sudoku.length; i++) {
            sudoku[i] = Arrays.copyOf(currentSudoku.getOriginal_field()[i], currentSudoku.getOriginal_field()[i].length);
        }
        currentSudoku.setModyfied_field(sudoku);
        updateSudoku(currentSudoku);
        isSolved = false;
        drawGrid();
        setClickable(true);
    }

    private void startGame(){
        for (int i = 0; i < sudoku.length; i++) {
            sudoku[i] = Arrays.copyOf(currentSudoku.getModyfied_field()[i], currentSudoku.getModyfied_field()[i].length);
        }
        isSolved = false;
        drawGrid();
        setClickable(true);
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
        TableRow tableRow;
        LinearLayout square, line;
        Button btn;

        parent= findViewById(R.id.parent_layout);
        for (int i = 0; i < parent.getChildCount(); i++) {
            for (int j = 0; j < 3; j++) {
                tableRow = (TableRow) parent.getChildAt(j);
                for (int k = 0; k < 3; k++) {
                    square = (LinearLayout) tableRow.getChildAt(k);
                    for (int l = 0; l < 3; l++) {
                        line = (LinearLayout) square.getChildAt(l);
                        for (int m = 0; m < 3; m++) {
                            btn = (Button) line.getChildAt(m);
                            btn.setClickable(clickable);
                            if (!clickable)
                                btn.setBackgroundResource(R.drawable.soluted_grid_button);
                        }
                    }
                }
            }
        }
        parent = findViewById(R.id.digit_buttons);
        for (int i = 0; i < 2; i++) {
            tableRow = (TableRow) parent.getChildAt(i);
            for (int j = 0; j < 5; j++) {
                btn = (Button) tableRow.getChildAt(j);
                btn.setClickable(clickable);
            }
        }
    }

    private void drawGrid(){
        TableLayout parent = findViewById(R.id.parent_layout);
        parent.removeAllViews();
        TableRow row;
        LinearLayout square, line;
        Button btn;
        int[][] original_field = currentSudoku.getOriginal_field();
        int[][] modyfied_field = currentSudoku.getModyfied_field();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(modyfied_field[i][j] + " ");
            }
            System.out.println();
        }

        for (int i = 0; i < 3; i++) {
            row = new TableRow(this);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            parent.addView(row);
            for (int j = 0; j < 3; j++) {
                square = new LinearLayout(this);
                square.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f));
                square.setOrientation(LinearLayout.VERTICAL);
                square.setBackgroundResource(R.drawable.square_border);
                row.addView(square);
                for (int k = 0; k < 3; k++) {
                    line = new LinearLayout(this);
                    line.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    line.setOrientation(LinearLayout.HORIZONTAL);
                    square.addView(line);
                    for (int l = 0; l < 3; l++) {
                        btn = new Button(this);
                        btn.setId(300000 + (i*30+k*10) + j*3 + l);
                        btn.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1.0f));
                        btn.setBackgroundResource(R.drawable.grid_button);
                        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
                        if (original_field[i*3+k][j*3+l] != 0){
                            btn.setText(String.valueOf(original_field[i*3+k][j*3+l]));
                            btn.setTextColor(getResources().getColor(R.color.originalDigitColor));
                            btn.setClickable(false);
                        }else{
                            btn.setText((modyfied_field[i*3+k][j*3+l] == 0)? "" : String.valueOf(modyfied_field[i*3+k][j*3+l]));
                            btn.setTextColor(getResources().getColor(R.color.modyfiedDigitColor));
                            btn.setClickable(true);
                            btn.setOnClickListener(this);
                        }
                        line.addView(btn);
                    }
                }
            }
        }
    }

    private void setDigit(int digit){
        int i, j;
        if (currentBtn != null) {
            currentBtn.setText((digit == 0) ? "" : String.valueOf(digit));
            i = ((currentBtn.getId()) - 300000) / 10;
            j = ((currentBtn.getId()) - 300000) % 10;
            sudoku[i][j] = digit;
            if (isHighlightEnabled) {
                removeHighlight();
                highlightArea(i, j);
            }
            if (isHelpEnabled) highlightMatches(i, j, sudoku[i][j]);
            if (checkSolution()) {
                congrats();
            }
        }
    }

    private void highlightArea(int digit_i, int digit_j) {
        Button currentButton;

        // Highlight column
        for (int i = 0; i < 9; i++) {
            currentButton = findViewById(300000 + digit_j + i*10);
            currentButton.setBackgroundResource(R.drawable.highlighted_grid_button);
        }

        // Highlight row
        for (int j = 0; j < 9; j++) {
            currentButton = findViewById(300000 + j + digit_i*10);
            currentButton.setBackgroundResource(R.drawable.highlighted_grid_button);
        }

        //Highlight square
        int rowStart = (digit_i/3)*3;
        int colStart = (digit_j/3)*3;
        for (int i = rowStart; i < rowStart+3; i++) {
            for (int j = colStart; j < colStart+3; j++) {
                currentButton = findViewById(300000 + i*10 + j);
                currentButton.setBackgroundResource(R.drawable.highlighted_grid_button);
            }
        }
    }

    private void highlightMatches(int digit_i, int digit_j, int digit){
        if (digit == 0) return;
        Button currentButton;
        for (int i = 0; i < 9; i++) { //highlight
            currentButton = findViewById(300000 + digit_j + i*10);
            if (sudoku[i][digit_j] == digit)
            currentButton.setBackgroundResource(R.drawable.mathced_grid_button);
        }
        for (int j = 0; j < 9; j++) {
            currentButton = findViewById(300000 + j + digit_i*10);
            if (sudoku[digit_i][j] == digit)
            currentButton.setBackgroundResource(R.drawable.mathced_grid_button);
        }

        int rowStart = (digit_i/3)*3;
        int colStart = (digit_j/3)*3;
        for (int i = rowStart; i < rowStart+3; i++) {
            for (int j = colStart; j < colStart+3; j++) {
                currentButton = findViewById(300000 + i*10 + j);
                if (sudoku[i][j] == digit)
                currentButton.setBackgroundResource(R.drawable.mathced_grid_button);
            }
        }
        currentBtn.setBackgroundResource(R.drawable.current_grid_button);
    }

    private void removeHighlight() {
        int digit_i = ((currentBtn.getId()) - 300000) / 10;
        int digit_j = ((currentBtn.getId()) - 300000) % 10;
        Button currentButton;
        for (int i = 0; i < 9; i++) {
            currentButton = findViewById(300000 + digit_j + i*10);
            currentButton.setBackgroundResource(R.drawable.grid_button);
        }
        for (int j = 0; j < 9; j++) {
            currentButton = findViewById(300000 + j + digit_i*10);
            currentButton.setBackgroundResource(R.drawable.grid_button);
        }
        int rowStart = (digit_i/3)*3;
        int colStart = (digit_j/3)*3;
        for (int i = rowStart; i < rowStart+3; i++) {
            for (int j = colStart; j < colStart+3; j++) {
                currentButton = findViewById(300000 + i*10 + j);
                currentButton.setBackgroundResource(R.drawable.grid_button);
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
            case R.id.newGameBtn:
                if (!isSolved) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.start_new_game))
                            .setMessage(getString(R.string.are_ypu_sure))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    currentSudoku = new Sudoku(new SudokuGenerator(spacesCount).getSudoku());
                                    addSudoku(currentSudoku);
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
                    currentSudoku = new Sudoku(new SudokuGenerator(spacesCount).getSudoku());
                    startGame();
                }
                break;
            case R.id.resetFieldBtn:
                if (!isSolved) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.reset_this_field))
                            .setMessage(getString(R.string.are_ypu_sure))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resetField();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            })
                            .show();
                }
                break;
            default:
                if (currentBtn != null){
                    currentBtn.setBackgroundResource(R.drawable.grid_button);
                    if (isHighlightEnabled || isHelpEnabled) removeHighlight();
                }
                currentBtn = findViewById(v.getId());
                int digit_i = ((currentBtn.getId()) - 300000) / 10;
                int digit_j = ((currentBtn.getId()) - 300000) % 10;
                if (isHighlightEnabled) highlightArea(digit_i, digit_j);
                if (isHelpEnabled) highlightMatches(digit_i, digit_j, sudoku[digit_i][digit_j]);
                currentBtn.setBackgroundResource(R.drawable.current_grid_button);
                break;
        }
    }
}