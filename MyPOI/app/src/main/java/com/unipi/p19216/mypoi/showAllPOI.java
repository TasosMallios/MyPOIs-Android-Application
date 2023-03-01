package com.unipi.p19216.mypoi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class showAllPOI extends AppCompatActivity {

    SQLiteDatabase db;
    List<String> poi_list = new ArrayList<String>();

    ListView listView;
    EditText search_input;
    String edited_title;
    String[] title;
    TextView no_poi_added_yet_message;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_poi);

        no_poi_added_yet_message = findViewById(R.id.no_poi_added_yet_message);
        db = openOrCreateDatabase("DB1.db", MODE_PRIVATE, null);

        // get all saved POIs
        Cursor cursor = db.rawQuery("Select * from POI", null);
        StringBuilder builder = new StringBuilder();

        // while cursor moves to next entry add each POI in poi_list
        while (cursor.moveToNext()) {
            builder.append("Title:").append(cursor.getString(0)).append("\n");
            builder.append("Timestamp:").append(cursor.getString(1)).append("\n");
            builder.append("Location:").append(cursor.getString(2)).append("\n");
            builder.append("Category:").append(cursor.getString(3)).append("\n");
            builder.append("Description:").append(cursor.getString(4)).append("\n");

            poi_list.add(builder.toString());

            // set builder length to 0 after adding each POI in poi_list
            builder.setLength(0);
        }

        // if poi_list is empty, then user has not saved any POI yet
        // so set a message on a textview visible to inform the user that there
        // are no POIs to show.
        if (poi_list.isEmpty()){
            no_poi_added_yet_message.setVisibility(View.VISIBLE);
        }
        else{
            no_poi_added_yet_message.setVisibility(View.INVISIBLE);
        }

        listView = findViewById(R.id.listView);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, poi_list);

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // search bar to search POI
        search_input = findViewById(R.id.search_input);
        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            // on text change show to user the POIs that match title that he typed
            @Override
            public void afterTextChanged(Editable editable) {

                adapter.getFilter().filter("Title:"+editable.toString());
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
        );

        // if user long clicks a POI then open an alert dialog
        // to choose if user wants to edit or delete that POI
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                options("Select", i);

                return true;
            }
        });

    }

    public AlertDialog options(String title, int itemPosition) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final CharSequence[] items = {"Edit", "Delete"};
        builder.setTitle(title)
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        // if user clicks edit
                        if (which == 0) {

                            // get selected POI to string
                            String selectedPOI = (String) (listView.getItemAtPosition(itemPosition));
                            // get the title of the selected POI
                            String s = selectedPOI.split("Title:")[1];
                            String selected_POI_title = s.split("\nTimestamp:")[0];

                            // get the category of the selected POI
                            s = s.split("Category:")[1];
                            String selected_POI_category = s.split("\nDescription:")[0];

                            // get the description of the selected POI
                            String selected_POI_description = s.split("\nDescription:")[1];
                            selected_POI_description = selected_POI_description.split("\n")[0];

                            editPOI(itemPosition, selected_POI_title, selected_POI_category, selected_POI_description);

                        // if user clicks delete
                        } else if (which == 1) {

                            // get selected POI to string
                            String selectedPOI = (String) (listView.getItemAtPosition(itemPosition));
                            // get the title of the selected POI
                            String s = selectedPOI.split("Title:")[1];
                            String selected_POI_title = s.split("\nTimestamp:")[0];

                            // alert dialog to ask user if he really wants to delete that POI
                            AlertDialog.Builder builder = new AlertDialog.Builder(showAllPOI.this);
                            builder.setMessage("Do you really want to delete that POI?");
                            builder.setCancelable(true);
                            // if user clicks "Yes" then delete POI from database
                            builder.setPositiveButton(
                                    "Yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            db.execSQL("DELETE FROM POI WHERE title=(?)", new String[]{selected_POI_title});
                                            Toast.makeText(showAllPOI.this, "POI Deleted.", Toast.LENGTH_SHORT).show();
                                            // refresh activity after edit POI
                                            finish();
                                            startActivity(getIntent());
                                        }
                                    }
                            );
                            builder.setNegativeButton(
                                    "No",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialog.cancel();
                                        }
                                    }
                            );

                            AlertDialog confirm_delete = builder.create();
                            confirm_delete.show();
                        }
                    }
                });
        return builder.show();

    }

    // dialog to edit POI
    public void editPOI(int itemPosition, String selected_POI_title, String selected_POI_category, String selected_POI_description) {

        SpinnerAdapter adapter = new SpinnerAdapter(getApplicationContext());
        title = getResources().getStringArray(R.array.category_array);

        final Dialog dialog = new Dialog(showAllPOI.this, R.style.Dialog);

        dialog.setContentView(R.layout.edit_poi_dialog);
        dialog.setCancelable(true);

        // set the custom dialog components
        final TextView tv = (TextView) dialog.findViewById(R.id.textViewTitle);
        final EditText edittext = (EditText) dialog.findViewById(R.id.editText1);
        edittext.setHint(selected_POI_title);
        edittext.setText(selected_POI_title);
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner1);
        // get spinner position of the selected POI and set it default value on dialog
        Button button = (Button) dialog.findViewById(R.id.button1);
        spinner.setAdapter(adapter);
        int i;
        int spinnerPosition=0;
        for (i = 0; i < adapter.getCount(); i++) {

            if (adapter.getItem(i).equals(selected_POI_category)) {
                spinnerPosition=i;

            }
        }

        spinner.setSelection(spinnerPosition);

        final EditText edittext2 = (EditText) dialog.findViewById(R.id.editText2);
        edittext2.setHint(selected_POI_description);
        edittext2.setText(selected_POI_description);

        dialog.show();

        // on okay button click save changes
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if user tries to change title and description to null
                // set title and description on their previous value
                if (edittext.getText().toString().trim().length()==0){
                    edittext.setText(selected_POI_title);
                }
                else if(edittext2.getText().toString().trim().length()==0){
                    edittext2.setText(selected_POI_description);
                }
                else{
                    // update table on the database
                    ContentValues cv = new ContentValues();
                    cv.put("title",edittext.getText().toString());
                    cv.put("category",spinner.getSelectedItem().toString());
                    cv.put("description", edittext2.getText().toString());
                    db.update("POI", cv, "title=?" , new String[]{selected_POI_title});
                    Toast.makeText(showAllPOI.this, "Changes saved!", Toast.LENGTH_SHORT).show();

                    // refresh activity after edit POI
                    finish();
                    startActivity(getIntent());
                }
            }
        });


    }

    // spinner adapter for the edit POI dialog
    public class SpinnerAdapter extends BaseAdapter {
        Context context;
        private LayoutInflater mInflater;

        public SpinnerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return title.length;
        }

        @Override
        public Object getItem(int position) {

            return (title[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ListContent holder;
            View v = convertView;
            if (v == null) {
                mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                v = mInflater.inflate(R.layout.row_textview, null);
                holder = new ListContent();
                holder.text = (TextView) v.findViewById(R.id.textView1);

                v.setTag(holder);
            } else {
                holder = (ListContent) v.getTag();
            }

            holder.text.setText(title[position]);

            return v;
        }

    }

    static class ListContent {
        TextView text;
    }

    public void showMessage (String title, String text){
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(text)
                .show();
    }

}