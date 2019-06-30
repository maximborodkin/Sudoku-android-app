package ru.maxim.sudoku;

class SudokuGenerator {

    private int[] mat[];
    private final int N = 9;
    private final int SRN = 3;
    private int K;

    SudokuGenerator(int spacesCount) {
        this.K = spacesCount;
        this.mat = new int[N][N];
    }

    private void fillValues() {
        fillDiagonal();
        fillRemaining(0, SRN);
    }

    private void fillDiagonal() {
        for (int i = 0; i<N; i=i+SRN) {
            fillBox(i, i);
        }
    }

    private boolean unUsedInBox(int rowStart, int colStart, int num) {
        for (int i = 0; i<SRN; i++) {
            for (int j = 0; j < SRN; j++) {
                if (mat[rowStart + i][colStart + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private void fillBox(int row, int col) {
        int num;
        for (int i=0; i<SRN; i++) {
            for (int j=0; j<SRN; j++) {
                do {
                    num = randomGenerator(N);
                } while (!unUsedInBox(row, col, num));
                mat[row+i][col+j] = num;
            }
        }
    }

    private int randomGenerator(int num) {
        return (int) Math.floor((Math.random()*num+1));
    }

    private boolean CheckIfSafe(int i, int j, int num) {
        return (unUsedInRow(i, num) &&
                unUsedInCol(j, num) &&
                unUsedInBox(i-i%SRN, j-j%SRN, num));
    }

    private boolean unUsedInRow(int i, int num) {
        for (int j = 0; j<N; j++) {
            if (mat[i][j] == num) {
                return false;
            }
        }
        return true;
    }

    private boolean unUsedInCol(int j, int num) {
        for (int i = 0; i<N; i++) {
            if (mat[i][j] == num) {
                return false;
            }
        }
        return true;
    }

    private boolean fillRemaining(int i, int j) {
        if (j>=N && i<N-1) {
            i = i + 1;
            j = 0;
        }
        if (i>=N && j>=N) {
            return true;
        }
        if (i < SRN) {
            if (j < SRN) {
                j = SRN;
            }
        } else if (i < N-SRN) {
            if (j==(i/SRN)*SRN) {
                j = j + SRN;
            }
        } else {
            if (j == N-SRN) {
                i = i + 1;
                j = 0;
                if (i>=N) {
                    return true;
                }
            }
        }
        for (int num = 1; num<=N; num++) {
            if (CheckIfSafe(i, j, num)) {
                mat[i][j] = num;
                if (fillRemaining(i, j+1)) {
                    return true;
                }
                mat[i][j] = 0;
            }
        }
        return false;
    }

    private void removeKDigits(){
        int count = K;
        while (count >0){
            int i = randomGenerator(8);
            int j = randomGenerator(8);
            if (mat[i][j] != 0){
                mat[i][j] = 0;
                count--;
            }
        }
    }

    int[][] getSudoku(){
        fillValues();
        removeKDigits();
        return mat;
    }
}
