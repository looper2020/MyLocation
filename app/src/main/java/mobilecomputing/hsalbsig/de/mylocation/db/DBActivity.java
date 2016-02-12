package mobilecomputing.hsalbsig.de.mylocation.db;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;

import com.google.android.gms.identity.intents.AddressConstants;

import java.util.ArrayList;
import java.util.List;

import mobilecomputing.hsalbsig.de.mylocation.R;
import mobilecomputing.hsalbsig.de.mylocation.dao.DaoMaster;
import mobilecomputing.hsalbsig.de.mylocation.dao.DaoSession;
import mobilecomputing.hsalbsig.de.mylocation.dao.Marker;
import mobilecomputing.hsalbsig.de.mylocation.dao.MarkerDao;
import mobilecomputing.hsalbsig.de.mylocation.dao.Track;
import mobilecomputing.hsalbsig.de.mylocation.dao.TrackDao;


public class DBActivity extends AppCompatActivity{


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        ScrollView sv = (ScrollView) findViewById(R.id.scrollView);
        final ListView view = (ListView) findViewById(R.id.listView);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,"location-db",null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();

        final MarkerDao markerDao = daoSession.getMarkerDao();
        final TrackDao trackDao = daoSession.getTrackDao();
        List<Track> list = trackDao.loadAll();


        if(list != null && view != null){
            final ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(this,R.layout.item,list);
            view.setAdapter(adapter);
            view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Track track = adapter.getItem(position);
                    Intent intent = new Intent();
                    intent.putExtra("trackID", track.getId());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
            view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(final AdapterView<?> arg0, View arg1,
                                               final int pos, long id) {

                    Log.v("long clicked","pos: " + pos);
                    final Track track = adapter.getItem(pos);
                    Log.v("track selected", "track: " + track.getName());

                    //DialogInterface.OnClickListener dialogClickListener = new CustomDialogListener(track, (DBActivity)view.getContext());
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    adapter.remove(track);
                                    for(Marker marker: track.getTrack()){
                                        markerDao.delete(marker);
                                    }
                                    trackDao.delete(track);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    //builder.setMessage(getString(R.string.delete_dialogue) + "" + track.getName()).setPositiveButton(R.string.yes, dialogClickListener)
                    //        .setNegativeButton(R.string.no, dialogClickListener).show();
                    builder.setMessage(getString(R.string.delete_dialogue) + "" + track.getName()).setPositiveButton(R.string.yes, listener)
                            .setNegativeButton(R.string.no, listener).show();



                    return true;
                }
            });
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
/*
    class CustomDialogListener implements DialogInterface.OnClickListener{
        Track track = null;
        DBActivity actvt = null;
        public CustomDialogListener(Track track, DBActivity actvt){
            this.track = track;
            this.actvt = actvt;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    Log.v("track: ", track.getName() + " deleted!");
                    track.delete();
                    actvt.finish();
                    actvt.startActivity(getIntent());
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Log.v("track: ", track.getName() + " not deleted!");
                    break;
            }
        }
    }
    */
}
