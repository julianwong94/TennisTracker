package com.example.davidgeisinger.tennistracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeScreen extends AppCompatActivity {


    //Harish im making a comment for you
    private ListView lv;
    ArrayAdapter<StatsPackage> arrayAdapter;
    StatsPackageDataAdapter statsAdapter;
    List<StatsPackage> your_array_list;
    ImageButton forehandButton;
    ImageButton backhandButton;
    ImageButton serveButton;
    ImageButton volleyButton;
    Button seeOverviewButton;

    MyDBHandler dbHandler = new MyDBHandler(this);

    String whichStroke;
//    DataReceiver dataReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        whichStroke = "f";
        setTheSwingListeners();
        setTheOverviewListener();
        lv = (ListView) findViewById(R.id.listy);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("broadcastStats"));
//        dataReceiver = new DataReceiver();
//        registerReceiver(mMessageReceiver, new IntentFilter("broadcastStats"));
        your_array_list = new ArrayList<StatsPackage>();
        arrayAdapter = new ArrayAdapter<StatsPackage>(
                this,
                android.R.layout.simple_list_item_1,
                your_array_list);
        lv.setAdapter(arrayAdapter);
        Intent service = new Intent(this, PhoneListenerService.class);
        startService(service);
        Log.d("start", "service");
        String message;
        Bundle mybundle = getIntent().getExtras();
        if (mybundle != null) {
            if (mybundle.getString("phone_data") != null) {
                message = mybundle.getString("phone_data");
                putInDB(message, dbHandler);
            }
            if (mybundle.getString("stroke") != null) {
                Log.d("GETTINGTOHERE", mybundle.getString("stroke"));
                whichStroke = mybundle.getString("stroke");
            }
        }

        StatsPackage addthis = new StatsPackage("November 24th", "67$26$14$9", "f", "37");
        //StatsPackage addthis1 = new StatsPackage("November 26th", "7$6$1$12", "f", "37");
        //StatsPackage addthis2 = new StatsPackage("November 25th", "63$21$14$2", "f", "37");
        Log.d("Start", addthis.time);
        dbHandler.addEntry(dbHandler, addthis);
        //dbHandler.addEntry(dbHandler, addthis1);
        //dbHandler.addEntry(dbHandler, addthis2);
        Log.d("Start", "HILLEZ");

        StatsPackage check_this = dbHandler.findEntry(addthis);
        Log.d("Chekcing", check_this.time);
        populateListView(whichStroke, dbHandler);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                StatsPackage real_item = (StatsPackage) item;
                String toBePassed = real_item.date + "!" + real_item.stats + "!" + real_item.stroke + "!" + real_item.time;
                Intent intent = new Intent(HomeScreen.this, ShowSession.class);
                intent.putExtra("string_passed", toBePassed);
                startActivity(intent);

            }


        });
    }

    public void populateListView(String stroke, MyDBHandler db) {
        ArrayList<StatsPackage> items = db.findMany(stroke);
        arrayAdapter.clear();
        if (items.get(0) != null) {
            Log.d("MYSIZE", Integer.toString(items.size()));
            for(int i = 0; i < items.size(); i++) {
                Log.d("whatimentering", items.get(i).date + items.get(i).stats + items.get(i).stroke + items.get(i).time);
                arrayAdapter.insert(items.get(i), i);
            }
        } else {
            // do something to tell them there are no entries
        }
        Log.d("FINISHED", "OK");

        // we must iterate over all entries in database that correspond to the stroke
        //we are on
    }

    public void setTheSwingListeners() {
        forehandButton = (ImageButton) findViewById(R.id.forehandPic);
        backhandButton = (ImageButton) findViewById(R.id.backhandPic);
        serveButton = (ImageButton) findViewById(R.id.servePic);
        volleyButton = (ImageButton) findViewById(R.id.volleyPic);

        // if they choose forehand
        forehandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichStroke = "f";
                populateListView(whichStroke, dbHandler);

            }
        });
        // if they choose backhand
        backhandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichStroke = "b";
                populateListView(whichStroke, dbHandler);

            }
        });

        // if they choose serve
        serveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichStroke = "s";
                populateListView(whichStroke, dbHandler);
            }
        });

        // if they choose volley
        volleyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichStroke = "v";
                populateListView(whichStroke, dbHandler);
            }
        });

    }

    public void setTheOverviewListener() {
        seeOverviewButton = (Button) findViewById(R.id.overviewButton);

        // set its listener
        seeOverviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*String superlongstr = "";
                for (int i = 0; i < your_array_list.size(); i++) {
                    superlongstr += your_array_list.get(i) + "$";
                }*/

                // This makes it so clicking to see an overview without data will not crash, we
                // have to choose what to do here
                if (your_array_list.size() == 0) {
                    return;
                }
                Intent intent = new Intent(HomeScreen.this, OverviewActivity.class);


                intent.putExtra("string_to_overview", whichStroke);
                startActivity(intent);
            }
        });
    }



    public void putInDB(String message, MyDBHandler dbHandler) {
        //parse message
        String [] arr = message.split("!");
        StatsPackage temp_sp = new StatsPackage(arr[0], arr[1], arr[2], arr[3]);
        dbHandler.addEntry(dbHandler, temp_sp);

        //actually populate the DB
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extra = intent.getExtras();
            double in = extra.getDouble("in");
            double longg = extra.getDouble("long");
            double wide = extra.getDouble("wide");
            double net = extra.getDouble("net");
            Log.d("RECEIVED", "HELLO");
            arrayAdapter.notifyDataSetChanged();
//            StatsPackage newEntry = new StatsPackage(magnitude, location, longitude, latitude);
//            pastEarthquakes.add(0, newEntry);
//            dataAdapter.notifyDataSetChanged();
//            if (extra.getString("type") == "notify") {
//                new SendToWatchThread("/message_path", formatMessage(newEntry)).start();
//                openMap(newEntry);
//            }
        }
    };

    private class StatsPackageDataAdapter extends ArrayAdapter<StatsPackageDataAdapter> {

        HashMap<StatsPackageDataAdapter, Integer> mIdMap = new HashMap<StatsPackageDataAdapter, Integer>();

        public StatsPackageDataAdapter(Context context, List<StatsPackageDataAdapter> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            StatsPackageDataAdapter data = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_home_screen, parent, false);
            }
//            // Lookup view for data population
//            TextView location = (TextView) convertView.findViewById(R.id.location);
//            TextView magnitude = (TextView) convertView.findViewById(R.id.magnitude);
//            // Populate the data into the template view using the data object
//            location.setText(data.getLocation());
//            magnitude.setText(Double.toString(data.getMagnitude()));
            // Return the completed view to render on screen
            return convertView;
        }
    }

}
