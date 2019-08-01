package com.example.zorkreader;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private long player = -1;
    private String name = "";
    private boolean wait = false;
    private View defaultView;
    private MenuItem quit;
    private MenuItem help;
    private ArrayList<String> commands = new ArrayList<>(Arrays.asList("check", "move", "take",
            "use", "equip", "attack", "open", "status"));
    private ArrayList<String> used = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        used.add("room");
        used.add("move");
        used.add("north");
        used.add("south");
        used.add("east");
        used.add("west");
        setContentView(R.layout.activity_main);
        setTitle("Start a game of Zork");
        defaultView = findViewById(R.id.layHolder);
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
        txtCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                LinearLayout keywords = findViewById(R.id.layPredict);
                if (charSequence.toString().contains(" ")) {
                    keywords.removeAllViews();
                    if (used.size() > 0) {
                        ArrayList<String> toUse = new ArrayList<>(used);
                        if (charSequence.toString().contains("move")) {
                            toUse = new ArrayList<>(Arrays.asList("north", "south", "east", "west"));
                        } else if (charSequence.toString().contains("check")) {
                            toUse = new ArrayList<>(Arrays.asList("room", "move"));
                        } else if (charSequence.toString().contains("status")) {
                            toUse = new ArrayList<>();
                        }
                        for (String word : toUse) {
                            String prev = "";
                            if (charSequence.toString().split(" ").length > 1) {
                                prev = charSequence.toString().split(" ")[1].toLowerCase();
                            }
                            if (word.toLowerCase().startsWith(prev) ||
                                    word.replace(" ", "-").toLowerCase().startsWith(prev)) {
                                Button btn = new Button(getApplicationContext());
                                btn.setText(word);
                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        TextView txtCommand = findViewById(R.id.txtCommand);
                                        String text = txtCommand.getText().toString().split(" ")[0];
                                        String btnText = ((Button) view).getText().toString()
                                                .replace(" ", "-");
                                        if (btnText.contains(text)) {
                                            txtCommand.setText("");
                                            txtCommand.append(btnText);
                                        } else {
                                            txtCommand.setText(text.trim() + " ");
                                            txtCommand.append(btnText);
                                            txtCommand.append("");
                                        }
                                    }
                                });
                                keywords.addView(btn);
                            }
                        }
                    }
                    if (keywords.getChildCount() == 0) {
                        TextView tv = new TextView(getApplicationContext());
                        tv.setText("No predictions");
                        keywords.addView(tv);
                    }
                } else {
                    keywords.removeAllViews();
                    for (String word : commands) {
                        Button btn = new Button(getApplicationContext());
                        btn.setText(word);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                TextView txtCommand = findViewById(R.id.txtCommand);
                                txtCommand.setText("");
                                txtCommand.append(((Button)view).getText() + " ");
                                txtCommand.requestFocus();
                                HorizontalScrollView scrPredict = findViewById(R.id.scrPredict);
                                scrPredict.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        HorizontalScrollView scrPredict = findViewById(R.id.scrPredict);
                                        scrPredict.fullScroll(ScrollView.FOCUS_LEFT);
                                    }
                                });
                            }
                        });
                        keywords.addView(btn);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
        Switch predict = findViewById(R.id.swtPredict);
        predict.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                LinearLayout layout = findViewById(R.id.layPredict);
                if (checked) {
                    layout.setVisibility(View.VISIBLE);
                } else {
                    layout.setVisibility(View.GONE);
                }
            }
        });
        findViewById(R.id.layCommon).setVisibility(View.GONE);
        findViewById(R.id.layGame).setVisibility(View.GONE);
        findViewById(R.id.layPredict).setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        quit = menu.findItem(R.id.itemQuit);
        help = menu.findItem(R.id.itemHelp);
        return true;
    }

    public void submit(View view) {
        TextView txtCommand = findViewById(R.id.txtCommand);
        String text = txtCommand.getText().toString().toLowerCase();
        if (wait || text.startsWith("load") || text.startsWith("play")) {
            return;
        }
        String[] splits = text.split(" ");
        if (splits.length > 1) {
            String toAdd = splits[1].replace("-", " ");
            if (used.contains(toAdd)) {
                used.remove(toAdd);
                used.add(0, toAdd);
            }

            toAdd = splits[0].replace("-", " ");
            if (commands.contains(toAdd)) {
                commands.remove(toAdd);
                commands.add(0, toAdd);
            }
        } else {
            String toAdd = text.replace("-", " ");
            if (commands.contains(toAdd)) {
                commands.remove(toAdd);
                commands.add(0, toAdd);
            }
        }
        wait = true;
        Thread t = new Thread(new Command(view, text.trim()));
        t.start();
    }

    public void startNew(View view) {
        TextView txtStart = findViewById(R.id.txtStart);
        String text = txtStart.getText().toString();
        if (wait) {
            return;
        }
        wait = true;
        name = text;
        txtStart.clearFocus();
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
        txtStart.clearFocus();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemQuit:
                TextView txtStart = findViewById(R.id.txtStart);
                txtStart.setText("");
                txtStart.setHint("Enter a new name or load an id");
                recreate();
                return true;

            case R.id.itemReset:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure you want to reset the " +
                        "entire Zork game, along with all players?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        wait = true;
                        Thread t = new Thread(new Command(defaultView, "reset"));
                        t.start();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                builder.create().show();
                return true;

            case R.id.itemHelp:
                wait = true;
                Thread t = new Thread(new Command(defaultView, "help"));
                t.start();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private String processCommand(String command) {
        String result = "Unknown Command";
        try {
            if (player < 0 && !command.equals("reset")) {
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
                    name = "";
                    return "Reset Completed";
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
                        name = result.substring(14, result.length() - 1);
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
            if (result.startsWith("Reset Completed")) {
                TextView txtStart = findViewById(R.id.txtStart);
                txtStart.setText("");
                txtStart.setHint("Enter a new name or load an id");
                recreate();
                return;
            } else if (result.contains("Things in the")) {
                String[] words = result.split("\n");
                for (int i = 1; i < words.length; i++) {
                    if (words[i].length() > 0 && !words[i].contains("Things in the") &&
                            !words[i].contains("---")) {
                        String toAdd = words[i].replaceAll(" x\\d+", "")
                                .toLowerCase();
                        used.remove(toAdd);
                        used.add(0, toAdd);
                    }
                }
            } else if (result.contains("Items in your inventory")) {
                String[] words = result.split("\n");
                for (int i = 4; i < words.length; i++) {
                    if (words[i].length() > 0 && !words[i].contains("Items in your inventory") &&
                            !words[i].contains("---")) {
                        String toAdd = words[i].replaceAll(" x\\d+", "")
                                .replace(" (equipped)", "")
                                .toLowerCase();
                        used.remove(toAdd);
                        used.add(0, toAdd);
                    }
                }
            }
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
                setTitle(name + " - " + player);
                findViewById(R.id.layGame).setVisibility(View.VISIBLE);
                findViewById(R.id.layLogin).setVisibility(View.GONE);
                quit.setVisible(true);
                help.setVisible(true);
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
