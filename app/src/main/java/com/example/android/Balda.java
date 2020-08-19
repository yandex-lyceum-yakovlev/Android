package com.example.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Balda {

    private int N = 0;
    private String[][] board;
    private String[] dictionary;
    //static int[][] dest = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // возможные направления слова
    int[][] dest = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

    public Balda(String[][] board, ArrayList<String> words) {
        this.N = board.length - 2;
        this.board = new String[board.length][board.length];
        for (int i = 0; i < N+2; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, N + 2);
        }
        ArrayList<String> w = (ArrayList<String>) words.clone();
        Collections.shuffle(words);
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        };
        dictionary = w.toArray(new String[0]);
        Arrays.sort(dictionary, comparator);
    }

    // находит возможные позиции для новой буквы
    public ArrayList<int[]> newLetterPositions(String[][] a) {
        ArrayList<int[]> result = new ArrayList<>();
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                if (!a[i][j].isEmpty()) {
                    result.add(new int[]{i, j});
                } else {
                    for (int[] d : dest) {
                        String g = a[i + d[0]][j + d[1]];
                        if (!g.isEmpty() && !g.equals("#")) {
                            result.add(new int[]{i, j});
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    // Функция проверяет, можно ли на поле a начиная с позиции i,j
    // записать слово word.
    public ArrayList<int[]> canb(String word, String[][] a, int i, int j, int k) {
        ArrayList<int[]> result = new ArrayList<>();
        ArrayList<int[]> e = new ArrayList<>();
        e.add(new int[]{0, 0});
        if (word.isEmpty() && k == 0) return result;
        if (!a[i][j].equals(word.substring(0, 1))) {
            if (a[i][j].isEmpty() && k > 0) {
                a[i][j] = "#";
                String w = word.substring(1);
                for (int[] d : dest) {
                    ArrayList<int[]> r = canb(w, a, i + d[0], j + d[1], k - 1);
                    if (!(r.size() == 1 && (r.get(0)[0] == 0 && r.get(0)[1] == 0))) {
                        a[i][j] = "";
                        r.add(0, new int[]{i, j, word.charAt(0)});
                        return r;
                    }
                }
                a[i][j] = "";
            }
            return e;
        }
        if (a[i][j].equals(word)) {
            ArrayList<int[]> r = new ArrayList<>();
            r.add(new int[]{i, j});
            return r;
        }
        a[i][j] = "#";
        String w = word.substring(1);
        for (int[] d : dest) {
            ArrayList<int[]> r = canb(w, a, i + d[0], j + d[1], k);
            if (!(r.size() == 1 && (r.get(0)[0] == 0 && r.get(0)[1] == 0))) {
                a[i][j] = word.substring(0, 1);
                r.add(0, new int[]{i, j});
                return r;
            }
        }
        a[i][j] = word.substring(0, 1);
        return e;
    }

    // функция выдает наилучший ход в виде списка полей и буквы, которую надо поставить
    public ArrayList<int[]> maxb(String[] d,  // Словарь
                                        String[][] a,  // Игровое поле
                                        ArrayList<int[]> fst) { // Список полей, куда можно поставить букву
        ArrayList<int[]> e = new ArrayList<>();
        e.add(new int[]{0, 0});
        for (String line : d) {
            for (int[] start : fst) {
                ArrayList<int[]> r = canb(line, a, start[0], start[1], 1);
                if (!(r.size() == 1 && (r.get(0)[0] == 0 && r.get(0)[1] == 0))) {
                    return r;
                }
            }
        }
        return e;
    }

    public ArrayList<int[]> compMove() {
        ArrayList<int[]> nlp = newLetterPositions(board);
        Collections.shuffle(nlp);
        return maxb(dictionary, board, nlp);
        /*StringBuilder w = new StringBuilder();
        for (int[] coordinates : r) {
            if (coordinates.length==3) w.append((char) coordinates[2]);
            else w.append(board[coordinates[0]][coordinates[1]]);
            //System.out.println(Arrays.toString(coordinates));
        }*/
        //System.out.println(w);
    }
}
