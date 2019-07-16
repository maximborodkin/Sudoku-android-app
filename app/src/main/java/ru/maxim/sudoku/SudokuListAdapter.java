package ru.maxim.sudoku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class SudokuListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<Sudoku> objects;
    private Context ctx;

    SudokuListAdapter(Context context, ArrayList<Sudoku> sudokus){
        this.ctx = context;
        this.objects = sudokus;
        this.layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private Sudoku getSudoku(int position){
        return (Sudoku) getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) view = layoutInflater.inflate(R.layout.list_item, parent, false);
        Sudoku sudoku = getSudoku(position);

        ((TextView) view.findViewById(R.id.item_name)).setText(sudoku.getCreation_date());
        ((TextView) view.findViewById(R.id.item_date)).setText(sudoku.getLast_modify_date());

        TableLayout base = view.findViewById(R.id.table_base);
        base.removeAllViews();
        TableRow row;
        TextView cell;
        for (int i = 0; i < 9; i++) {
            row = new TableRow(ctx);
            base.addView(row);
            for (int j = 0; j < 9; j++) {
                cell = new TextView(ctx);
                if (sudoku.getOriginal_field()[i][j] != 0) {
                    cell.setText(String.valueOf(sudoku.getOriginal_field()[i][j]));
                    cell.setBackgroundResource(R.drawable.unclickable_grid_button);
                }else{
                    cell.setText((sudoku.getModyfied_field()[i][j] != 0)? String.valueOf(sudoku.getModyfied_field()[i][j]) : "");
                    cell.setBackgroundResource(R.drawable.clickable_grid_button);
                }
                row.addView(cell);
            }
        }
        return  view;
    }
}
