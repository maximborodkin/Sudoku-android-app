package ru.maxim.sudoku;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Sudoku {
    private int[][] original_field;
    private int[][] modyfied_field;
    private String creation_date;
    private String last_modify_date;

    Sudoku(int[][] original_field) {
        this.original_field = original_field;
        this.modyfied_field = original_field;
        this.creation_date = SimpleDateFormat.getDateTimeInstance().format(new Date());
        this.last_modify_date = this.creation_date;
    }

    Sudoku(int[][] original_field, int[][] modyfied_field, String creation_date, String last_modify_date){
        this.original_field = original_field;
        this.modyfied_field = modyfied_field;
        this.creation_date = creation_date;
        this.last_modify_date = last_modify_date;
    }

    static int[][] getArray(String JSON) {
        int[][] result = new int[9][9];
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(JSON).getAsJsonArray();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                result[i][j] = array.get(i).getAsJsonArray().get(j).getAsInt();
            }
        }
        return result;
    }

    int[][] getOriginal_field() {
        return original_field;
    }

    int[][] getModyfied_field() {
        return modyfied_field;
    }



    void setModyfied_field(int[][] modyfied_field) {
        this.modyfied_field = modyfied_field;
    }

    String getCreation_date() {
        return creation_date;
    }

    String getLast_modify_date() {
        return last_modify_date;
    }

    void setLast_modify_date(String last_modify_date) {
        this.last_modify_date = last_modify_date;
    }

    static String getJSONArray(int[][] array){
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (int i = 0; i < array.length; i++) {
            result.append("[");
            for (int j = 0; j < array[0].length; j++) {
                result.append(array[i][j]).append((j == array[0].length - 1) ? "]" : ",");
            }
            result.append((i == array.length-1)? "]" : ",");
        }
        return result.toString();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int[] anOriginal_field : original_field) {
            hash += Arrays.hashCode(anOriginal_field);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    @Override
    public String toString() {
        return this.hashCode() + "\n"
                + getJSONArray(original_field);
    }

}