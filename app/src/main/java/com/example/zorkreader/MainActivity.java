package com.example.zorkreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private long player = -1;
    private boolean wait = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView txtCommand = findViewById(R.id.txtCommand);
        txtCommand.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    submit(textView);
                    return true;
                }
                return false;
            }
        });
        Switch show = findViewById(R.id.swtCommon);
        show.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                TableLayout layout = findViewById(R.id.layCommon);
                if (checked) {
                    layout.setVisibility(View.VISIBLE);
                } else {
                    layout.setVisibility(View.GONE);
                }
            }
        });
        findViewById(R.id.layCommon).setVisibility(View.GONE);
        findViewById(R.id.layGame).setVisibility(View.GONE);
    }

    public void submit(View view) {
        TextView txtCommand = findViewById(R.id.txtCommand);
        String text = txtCommand.getText().toString().toLowerCase();
        if (wait || text.startsWith("load") || text.startsWith("play")) {
            return;
        }
        wait = true;
        Thread t = new Thread(new Command(view, text));
        t.start();
    }

    public void startNew(View view) {
        TextView txtStart = findViewById(R.id.txtStart);
        String text = txtStart.getText().toString();
        if (wait) {
            return;
        }
        wait = true;
        Thread t = new Thread(new Start(view, "play " + text));
        t.start();
    }

    public void startLoad(View view) {
        TextView txtStart = findViewById(R.id.txtStart);
        String text = txtStart.getText().toString();
        long id = -1;
        try {
            id = Long.parseLong(text);
        } catch (Exception e) {
            txtStart.setText("");
            txtStart.setHint("Please enter a valid id");
        }
        if (wait) {
            return;
        }
        wait = true;
        Thread t = new Thread(new Start(view, "load " + id));
        t.start();
    }

    public void buttonCommand(View view) {
        if (wait) {
            return;
        }
        Button btn = (Button) view;
        String text = btn.getText().toString().toLowerCase();
        wait = true;
        Thread t = new Thread(new Command(view, text));
        t.start();
    }

    private String processCommand(String command) {
        String result = "Unknown Command";
        try {
            if (player < 0) {
                return "No current player!";
            } else {
                if (!(command.startsWith("check") || command.startsWith("reset")
                        || command.startsWith("help")
                        || command.startsWith("status") || command.startsWith("move"))) {
                    command = "act_" + command;
                }
                if (!(command.startsWith("reset") || command.startsWith("help"))) {
                    command = player + "_" + command;
                }
                command = command.replace(" ", "_");
                result = ReadHelper.readTextFromUrl("https://quiet-tundra-15027.herokuapp.com/" + command.trim());
                if (command.equals("reset")) {
                    player = -1;
                }
            }
        } catch (IOException e) {
            return "Invalid Read";
        }
        return result.trim() + "\n---";
    }

    private String processStart(String command) {
        String result = "Unknown Command";
        try {
            if (player < 0) {
                command = command.replace(" ", "_");
                if (command.startsWith("play")) {
                    result = ReadHelper.readTextFromUrl("https://quiet-tundra-15027.herokuapp.com/" + command.trim());
                    player = Long.parseLong(result);
                    result = "Player created! Your id is '" + result + "'\nYou are in the Foyer.";
                } else if (command.startsWith("load") || command.startsWith("reset")) {
                    result = ReadHelper.readTextFromUrl("https://quiet-tundra-15027.herokuapp.com/" + command.trim());
                    if (command.startsWith("load")) {
                        player = Long.parseLong(command.substring(5));
                    }
                }
            }
        } catch (IOException e) {
            return "Invalid Read";
        }
        return result.trim() + "\n---";
    }

    class Command implements Runnable {

        View view;
        String command;

        Command(View view, String command) {
            this.view = view;
            this.command = command;
        }

        @Override
        public void run() {
            String result = processCommand(command);
            runOnUiThread(new PostResult(view, result));
        }
    }

    class Start implements Runnable {

        View view;
        String command;

        Start(View view, String command) {
            this.view = view;
            this.command = command;
        }

        @Override
        public void run() {
            String result = processStart(command);
            runOnUiThread(new PostStart(view, result));
        }
    }

    class PostResult implements Runnable {

        View view;
        String result;

        PostResult(View view, String result) {
            this.view = view;
            this.result = result;
        }

        @Override
        public void run() {
            TextView txtCommand = findViewById(R.id.txtCommand);
            LinearLayout layout = findViewById(R.id.layHistory);
            txtCommand.setText("");
            TextView txtNew = new TextView(view.getContext());
            txtNew.setId(layout.getChildCount());
            txtNew.setText(result);
            txtNew.setTextColor(getColor(R.color.colorBlack));
            txtNew.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            TextView prev = (TextView) layout.getChildAt(layout.getChildCount() - 1);
            prev.setTextColor(getColor(R.color.colorGrey));
            layout.addView(txtNew);
            ScrollView scrHistory = findViewById(R.id.scrHistory);
            scrHistory.post(new Runnable() {
                @Override
                public void run() {
                    ScrollView scrHistory = findViewById(R.id.scrHistory);
                    scrHistory.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            wait = false;
        }
    }

    class PostStart implements Runnable {

        View view;
        String result;

        PostStart(View view, String result) {
            this.view = view;
            this.result = result;
        }

        @Override
        public void run() {
            TextView txtCommand = findViewById(R.id.txtCommand);
            LinearLayout layout = findViewById(R.id.layHistory);
            if (result.startsWith("Invalid Read")) {
                TextView txtStart = findViewById(R.id.txtStart);
                txtStart.setText("");
                txtStart.setHint("Please enter a valid id");
            } else {
                findViewById(R.id.layGame).setVisibility(View.VISIBLE);
                findViewById(R.id.layLogin).setVisibility(View.GONE);
                txtCommand.setText("");
                TextView txtNew = new TextView(view.getContext());
                txtNew.setId(layout.getChildCount());
                txtNew.setText(result);
                txtNew.setTextColor(getColor(R.color.colorBlack));
                txtNew.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                TextView prev = (TextView) layout.getChildAt(layout.getChildCount() - 1);
                prev.setTextColor(getColor(R.color.colorGrey));
                layout.addView(txtNew);
                ScrollView scrHistory = findViewById(R.id.scrHistory);
                scrHistory.post(new Runnable() {
                    @Override
                    public void run() {
                        ScrollView scrHistory = findViewById(R.id.scrHistory);
                        scrHistory.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
            wait = false;
        }
    }
}
