package mobilecomputing.hsalbsig.de.mylocation.dao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import mobilecomputing.hsalbsig.de.mylocation.dao.Marker;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MARKER".
*/
public class MarkerDao extends AbstractDao<Marker, Long> {

    public static final String TABLENAME = "MARKER";

    /**
     * Properties of entity Marker.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Latitude = new Property(1, Double.class, "Latitude", false, "LATITUDE");
        public final static Property Longitude = new Property(2, Double.class, "Longitude", false, "LONGITUDE");
        public final static Property Text = new Property(3, String.class, "Text", false, "TEXT");
        public final static Property MarkerId = new Property(4, Long.class, "MarkerId", false, "MARKER_ID");
    };

    private DaoSession daoSession;

    private Query<Marker> track_TrackQuery;

    public MarkerDao(DaoConfig config) {
        super(config);
    }
    
    public MarkerDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MARKER\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"LATITUDE\" REAL," + // 1: Latitude
                "\"LONGITUDE\" REAL," + // 2: Longitude
                "\"TEXT\" TEXT," + // 3: Text
                "\"MARKER_ID\" INTEGER);"); // 4: MarkerId
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MARKER\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Marker entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Double Latitude = entity.getLatitude();
        if (Latitude != null) {
            stmt.bindDouble(2, Latitude);
        }
 
        Double Longitude = entity.getLongitude();
        if (Longitude != null) {
            stmt.bindDouble(3, Longitude);
        }
 
        String Text = entity.getText();
        if (Text != null) {
            stmt.bindString(4, Text);
        }
 
        Long MarkerId = entity.getMarkerId();
        if (MarkerId != null) {
            stmt.bindLong(5, MarkerId);
        }
    }

    @Override
    protected void attachEntity(Marker entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Marker readEntity(Cursor cursor, int offset) {
        Marker entity = new Marker( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getDouble(offset + 1), // Latitude
            cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2), // Longitude
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // Text
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4) // MarkerId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Marker entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setLatitude(cursor.isNull(offset + 1) ? null : cursor.getDouble(offset + 1));
        entity.setLongitude(cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2));
        entity.setText(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setMarkerId(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Marker entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Marker entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "Track" to-many relationship of Track. */
    public List<Marker> _queryTrack_Track(Long MarkerId) {
        synchronized (this) {
            if (track_TrackQuery == null) {
                QueryBuilder<Marker> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.MarkerId.eq(null));
                track_TrackQuery = queryBuilder.build();
            }
        }
        Query<Marker> query = track_TrackQuery.forCurrentThread();
        query.setParameter(0, MarkerId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getTrackDao().getAllColumns());
            builder.append(" FROM MARKER T");
            builder.append(" LEFT JOIN TRACK T0 ON T.\"MARKER_ID\"=T0.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Marker loadCurrentDeep(Cursor cursor, boolean lock) {
        Marker entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Track track = loadCurrentOther(daoSession.getTrackDao(), cursor, offset);
        entity.setTrack(track);

        return entity;    
    }

    public Marker loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<Marker> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Marker> list = new ArrayList<Marker>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<Marker> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Marker> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
