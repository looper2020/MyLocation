package mobilecomputing.hsalbsig.de.mylocation;

import android.app.Activity;
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

/**
 * Created by Michael on 18.01.2016.
 */
public class DBActivity extends AppCompatActivity{


    private ScrollView sv;


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        this.sv = (ScrollView) findViewById(R.id.scrollView);
        final ListView view = (ListView) findViewById(R.id.listView);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,"location-db",null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();

        MarkerDao markerDao = daoSession.getMarkerDao();
        TrackDao trackDao = daoSession.getTrackDao();
        List<Track> list = trackDao.loadAll();


        if(list != null && view != null){
            final ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(this,R.layout.item,list);
            view.setAdapter(adapter);
            view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Track track = adapter.getItem(position);
                    Intent intent = new Intent();
                    intent.putExtra("trackID",track.getId());
                    setResult(Activity.RESULT_OK,intent);
                    finish();
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_db, menu);
        return true;
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
}
