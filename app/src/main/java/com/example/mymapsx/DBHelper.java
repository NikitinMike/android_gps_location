package com.example.mymapsx;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBHelper extends SQLiteOpenHelper {

    public static final String myDB = "myDBMap";
    private static final int dbVersion = 26;
    final String LOG_TAG = "myDBLogs";
    final String mytable = "myTable";
    SQLiteDatabase db;

    boolean getDBRecord(double latitude,double longitude){
//        if(db.isOpen()){
            Cursor c = db.rawQuery("SELECT id,datetime FROM "+mytable+" WHERE "
                    +"latitude = "+latitude+" AND " +"longitude = "+longitude, null);
            if (c.moveToFirst()){
                do {
                    // Passing values
                    long id = c.getLong(0);
                    long time = c.getLong(1);
                    // Do something Here with values
//                    Log.d(LOG_TAG, "row: ID = " + id+" "+time);
                } while(c.moveToNext());
                return true;
            }
            c.close();
//        }
        return false;
    }

    long saveDBRecord(long time,double latitude,double longitude) {
//        if(!db.isOpen())return 0;
        if(getDBRecord(latitude,longitude)) return 0L;
        // создаем объект для данных
        ContentValues cv = new ContentValues();
//        cv.put("name", "name");
//        cv.put("email", "email");
        cv.put("datetime", time);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        // вставляем запись и получаем ее ID
        long rowID = db.insert(mytable, null, cv);
        Log.d(LOG_TAG, "row inserted, ID = " + rowID+" "+cv);
        return rowID;
    }

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, myDB, null, dbVersion);
        // подключаемся к БД
        db = getWritableDatabase();
//        clearDBData();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
//        db.execSQL("DROP TABLE IF EXISTS "+mytable);
        // создаем таблицу с полями
        db.execSQL("CREATE TABLE "+mytable+" ("
                + "id integer primary key autoincrement,"
                + "datetime integer,"
                + "latitude real,"
                + "longitude real"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+mytable);
        onCreate(db);
    }

    void clearDBData() {
        db.execSQL("DROP TABLE IF EXISTS "+mytable);
        Log.d(LOG_TAG, "--- Clear mytable: ---");
        // удаляем все записи
        int clearCount = db.delete(mytable, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
    }

    void getDBData() {
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c = db.query(mytable, null, null, null, null, null, null);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        Log.d(LOG_TAG,"----------------------------------------------------------------");
        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int datetimeColIndex = c.getColumnIndex("datetime");
            int latitudeColIndex = c.getColumnIndex("latitude");
            int longitudeColIndex = c.getColumnIndex("longitude");
//            int nameColIndex = c.getColumnIndex("name");
//            int emailColIndex = c.getColumnIndex("email");
            do {
                // получаем значения по номерам столбцов и пишем все в лог
                Log.d(LOG_TAG,
                        "ID = " + c.getInt(idColIndex)
                                +", time = " + c.getLong(datetimeColIndex)
                                +", latitude = " + c.getDouble(latitudeColIndex)
                                +", longitude = " + c.getDouble(longitudeColIndex)
//                        ", name = " + c.getString(nameColIndex) +
//                        ", email = " + c.getString(emailColIndex)
                );
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
    }

}
