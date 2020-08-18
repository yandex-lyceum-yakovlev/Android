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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    //Переменная для работы с БД
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;


    TableLayout main;
    String word = "";
    String nextChar = "";
    EditText eWord;
    TextView tvInfo;
    TextView tvScore;
    int N = 5;
    ArrayList<String> words = new ArrayList<>();
    String[][] board = new String[N + 2][N + 2];
    Square[][] eBoard = new Square[N+2][N+2];

    int hScore = 0;
    int cScore = 0;
    int pos = -1;
    int li=-1;
    int lj = -1;
    //static int mode = 0;
    boolean newC = true;


    static String selectColor = "#00ff00";
    static String voidColor = "#eeeeee";


    View.OnClickListener ocl = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Square square = (Square) view;
            String s = square.getText().toString();
            if (s.isEmpty() && !newC) return;
            if (!s.isEmpty() && !s.equals(nextChar)) return;
            if (((ColorDrawable)square.getBackground()).getColor()!=Color.parseColor(voidColor))
                return;
            if (li>=0 && (Math.abs(li - square.i) > 1 || Math.abs(lj - square.j) > 1))
                return;
            if (s.isEmpty()) newC = false;
            square.setText(nextChar);
            li = square.i;
            lj = square.j;
            square.select();
            pos++;
            if(pos >= word.length()) {
                hScore += word.length();
                tvScore.setText(String.format("Score: %d - %d", hScore, cScore));
            }
            else {
                nextChar = String.valueOf(word.charAt(pos));
                tvInfo.setText(String.format("Следующая буква: %s", nextChar));
            }
        }
    };



    private void refresh() {
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                String s = board[i][j];
                eBoard[i][j].clear();
                eBoard[i][j].setText(s);
            }
        }
    }

    private void clearBoard(){
        for (int i = 0; i < N + 2; i++) {
            for (int j = 0; j < N + 2; j++) {
                board[i][j] = "";
            }
        }
    }

    private void createBoard() {
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

    private void newGame(){
        hScore = 0;
        cScore = 0;
        clearBoard();

        Cursor cursor = mDb.rawQuery(
                String.format("SELECT word FROM words WHERE LENGTH(word) = %d ", N),
                null);
        cursor.moveToFirst();
        words.add(cursor.getString(0));
        while(cursor.moveToNext()){
            words.add(cursor.getString(0));
        }
        cursor.close();
        String temp = words.get(new Random().nextInt(words.size()));
        tvInfo.setText(temp);
        int h = N/2 + 1;
        for (int i = 1; i <= N; i++){
            board[h][i] = String.valueOf(temp.charAt(i-1));
        }
        refresh();
    }

    private void initDB(){
        mDBHelper = new DatabaseHelper(this);
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
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

        final Button buttonOK =findViewById(R.id.ok);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                word = eWord.getText().toString();
                Cursor cursor = mDb.rawQuery(
                        String.format("SELECT word FROM words WHERE word = '%s' ", word),
                        null);

                if (cursor.getCount()==0) {
                    tvInfo.setText("Такого слова нет");
                    cursor.close();
                    return;
                }
                cursor.close();
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
                newC = true;
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
    public int i;
    public int j;
    static int r = 0;

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