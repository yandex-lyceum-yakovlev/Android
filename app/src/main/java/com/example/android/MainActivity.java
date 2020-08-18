package com.example.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    TableLayout main;
    String word = "";
    EditText eWord;
    TextView nextC;
    int N = 5;
    String[][] board = new String[N + 2][N + 2];
    Square[][] eBoard = new Square[N+2][N+2];

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
            if (!s.isEmpty() && !s.equals(nextC.getText().toString())) return;
            if (((ColorDrawable)square.getBackground()).getColor()!=Color.parseColor(voidColor))
                return;
            if (li>=0 && (Math.abs(li - square.i) > 1 || Math.abs(lj - square.j) > 1))
                return;
            if (s.isEmpty()) newC = false;
            square.setText(nextC.getText().toString());
            li = square.i;
            lj = square.j;
            square.select();
            pos++;
            if(pos > word.length());
            else nextC.setText(String.valueOf(word.charAt(pos)));;
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
        for (int i = 0; i < N + 2; i++) {
            for (int j = 0; j < N + 2; j++) {
                board[i][j] = "";
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = findViewById(R.id.TL);
        eWord = findViewById(R.id.word);
        nextC = findViewById(R.id.nextC);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pos = 0;
                word = eWord.getText().toString();
                nextC.setText(String.valueOf(word.charAt(pos)));

            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newC = true;
                pos = -1;
                word = "";
                eWord.setText(word);
                nextC.setText(word);
                refresh();
            }
        });
        createBoard();



        board[3][1] = "к";
        board[3][2] = "а";
        board[3][3] = "б";
        board[3][4] = "а";
        board[3][5] = "н";
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