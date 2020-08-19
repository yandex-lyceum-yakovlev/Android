package com.example.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase mDb;

    TableLayout main;
    EditText eWord; // поле для ввода слова пользователем
    String word = ""; // ход пользователя
    String nextChar = ""; // следующий символ, который должен ввести пользователь
    int pos = -1; // позиция символа в слове
    TextView tvInfo; // информация
    TextView tvScore; // счет
    int N = 5; // размер поля
    ArrayList<String> words = new ArrayList<>(); // список в который считывается словарь
    String[][] board = new String[N + 2][N + 2]; // игровое поле
    Square[][] eBoard = new Square[N + 2][N + 2]; // клетки

    int hScore = 0; // счет человека
    int cScore = 0; // счет компьютера

    int li = -1; // координаты последней ячейки, по которой кликнул
    int lj = -1; // пользователь вводя слово
    int newCharI = -1; // координы ячейки куда будет вписана
    int newCharJ = -1; // новая буква
    String newCharC = ""; // новая буква


    static String selectColor = "#00ff00"; // цвет выделенных ячеек
    static String voidColor = "#eeeeee"; // цвет обычных ячеек


    private void compMove() { // ход компьютера
        refresh();
        Balda balda = new Balda(board, words);
        try {
            ArrayList<int[]> r = balda.compMove();
            StringBuilder w = new StringBuilder();
            for (int[] coordinates : r) {
                if (coordinates.length == 3){
                    w.append((char) coordinates[2]);
                    board[coordinates[0]][coordinates[1]] = String.valueOf((char) coordinates[2]);
                    eBoard[coordinates[0]][coordinates[1]].setText(board[coordinates[0]][coordinates[1]]);
                }
                else w.append(board[coordinates[0]][coordinates[1]]);
                eBoard[coordinates[0]][coordinates[1]].select();
            }
            tvInfo.setText(String.format("Ход компьютера: %s", w));
            cScore += w.length();
            words.remove(w.toString());
            tvScore.setText(String.format("Score: %d - %d", hScore, cScore));
        }
        catch (Exception e){
            Toast toast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    View.OnClickListener ocl = new View.OnClickListener() { //действия при клике по ячейке
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(View view) {
            Square square = (Square) view;
            String s = square.getText().toString();
            if (s.isEmpty() && newCharI >= 0) return; // нельзя ввести две новые буквы
            if (!s.isEmpty() && !s.equals(nextChar)) return; // в ячеке не та буква
            if (((ColorDrawable) square.getBackground()).getColor() != Color.parseColor(voidColor))
                return; // уже кликали по этой ячейке
            if (li >= 0 && (Math.abs(li - square.i) > 1 || Math.abs(lj - square.j) > 1))
                return; // нельзя прыгать более чем на одну клетку
            if (s.isEmpty()) {
                newCharI = square.i;
                newCharJ = square.j;
                newCharC = nextChar;
                square.setText(nextChar);
            }
            li = square.i;
            lj = square.j;
            square.select();
            pos++;
            if (pos >= word.length()) { // ввели все слово
                if (newCharI >=0) {
                    hScore += word.length();
                    words.remove(word);
                    tvScore.setText(String.format("Score: %d - %d", hScore, cScore));
                    board[newCharI][newCharJ] = newCharC;
                    newCharI = -1;
                    newCharJ = -1;
                    eWord.setText("");
                    pos=-1;
                    compMove();
                }
                else{
                    tvInfo.setText("Ошибка: надо вписать новую букву");
                    refresh();
                    pos = 0;
                    newCharI = -1;
                    newCharJ = -1;
                    nextChar = String.valueOf(word.charAt(pos));
                    tvInfo.setText(String.format("Следующая буква: %s", nextChar));
                }
            } else {
                nextChar = String.valueOf(word.charAt(pos));
                tvInfo.setText(String.format("Следующая буква: %s", nextChar));
            }
        }
    };


    private void refresh() { // перерисовка (очистка) поля
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                eBoard[i][j].clear();
                eBoard[i][j].setText(board[i][j]);
            }
        }
    }

    private void clearBoard() { // очистка поля в памяти
        for (int i = 0; i < N + 2; i++) {
            for (int j = 0; j < N + 2; j++) {
                board[i][j] = "#";
            }
        }
        for (int i = 1; i < N + 1; i++) {
            for (int j = 1; j < N + 1; j++) {
                board[i][j] = "";
            }
        }
    }

    private void createBoard() { // заполнение таблицы ячейками
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        for (int i = 1; i <= N; i++) {
            TableRow tr = new TableRow(this);
            tr.setBackgroundColor(Color.parseColor("#eeeeee"));
            for (int j = 1; j <= N; j++) {
                eBoard[i][j] = new Square(this, i, j);
                eBoard[i][j].clear();
                eBoard[i][j].setOnClickListener(ocl);
                tr.addView(eBoard[i][j]);
            }
            main.addView(tr, params);
        }
        clearBoard();
    }

    private void newGame() {
        hScore = 0;
        cScore = 0;
        clearBoard();

        Cursor cursor = mDb.rawQuery(
                String.format("SELECT word FROM words WHERE LENGTH(word) = %d ", N),
                null);
        cursor.moveToFirst();
        words.add(cursor.getString(0));
        while (cursor.moveToNext()) {
            words.add(cursor.getString(0));
        }
        cursor.close();
        String temp = words.get(new Random().nextInt(words.size()));
        tvInfo.setText(temp);
        words.remove(temp);
        int h = N / 2 + 1;
        for (int i = 1; i <= N; i++) {
            board[h][i] = String.valueOf(temp.charAt(i - 1));
        }
        refresh();
    }

    private void initDB() {
        //Переменная для работы с БД
        DatabaseHelper mDBHelper = new DatabaseHelper(this);
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        mDb = mDBHelper.getWritableDatabase();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = findViewById(R.id.TL);
        eWord = findViewById(R.id.word);
        tvInfo = findViewById(R.id.info);
        tvScore = findViewById(R.id.score);
        findViewById(R.id.newGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newGame();
            }
        });

        final Button buttonOK = findViewById(R.id.ok);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                word = eWord.getText().toString();
                Cursor cursor = mDb.rawQuery(
                        String.format("SELECT word FROM words WHERE word = '%s' ", word),
                        null);

                if (cursor.getCount() == 0) {
                    tvInfo.setText("Такого слова нет");
                    cursor.close();
                    return;
                }
                cursor.close();
                refresh();
                li = -1;
                lj = -1;
                pos = 0;
                nextChar = String.valueOf(word.charAt(pos));
                tvInfo.setText(String.format("Следующая буква: %s", nextChar));
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(buttonOK.getApplicationWindowToken(), 0);
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                li = -1;
                lj = -1;
                newCharI = -1;
                newCharJ = -1;
                pos = -1;
                word = "";
                eWord.setText(word);
                refresh();
            }
        });
        createBoard();
        initDB();
        refresh();
    }
}

@SuppressLint("ViewConstructor")
class Square extends androidx.appcompat.widget.AppCompatTextView {
    // ячейка игрового поля по которой можно кликать
    public int i;
    public int j;

    public void select() {
        this.setBackgroundColor(Color.parseColor(MainActivity.selectColor));
    }

    public void clear() {
        this.setBackgroundColor(Color.parseColor(MainActivity.voidColor));
    }

    public Square(@NonNull Context context, int i, int j) {
        super(context);
        this.setGravity(Gravity.CENTER);
        this.setTextSize(50);
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        this.i = i;
        this.j = j;
    }

}