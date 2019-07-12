package com.adi.exam.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class App_Table {

    private Database database;

    public App_Table(Context context) {

        database = new Database(context);

    }

    public boolean isRecordExits(String iwhereClause, String tableName) {

        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                Cursor cursor = db.query(tableName, null, iwhereClause,
                        null, null, null, null);

                if (cursor != null) {

                    if (cursor.getCount() > 0) {

                        cursor.close();
                        db.close();

                        return true;

                    }

                    cursor.close();

                }

                db.close();

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return false;

    }

    public void checkNInsertARecord(JSONObject jsonObject, String tableName, String iwhereClause) {

        try {

            boolean isRecordAvailable = isRecordExits(iwhereClause, tableName);

            if (isRecordAvailable) {

                int rowId = deleteRecord(iwhereClause, tableName);

                if (rowId > 0) {

                    insertSingleRecords(jsonObject, tableName);

                }

            } else {

                insertSingleRecords(jsonObject, tableName);

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public void insertMultipleRecords(JSONArray jsonArray, String tableName) {

        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                for (int i = 0; i < jsonArray.length(); i++) {

                    ContentValues contentValues = new ContentValues();

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Iterator<String> iter = jsonObject.keys();

                    while (iter.hasNext()) {

                        String key = iter.next();

                        try {

                            String value = jsonObject.optString(key);

                            contentValues.put(key, value);

                        } catch (Exception e) {

                            TraceUtils.logException(e);

                        }

                    }

                    long rawId = db.insert(tableName, null, contentValues);

                }

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public long insertSingleRecords(JSONObject jsonObject, String tableName) {

        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                ContentValues contentValues = new ContentValues();

                Iterator<String> iter = jsonObject.keys();

                while (iter.hasNext()) {

                    String key = iter.next();

                    try {

                        String value = jsonObject.optString(key);

                        contentValues.put(key, value);

                    } catch (Exception e) {

                        TraceUtils.logException(e);

                    }

                }

                return db.insert(tableName, null, contentValues);

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return -1;

    }

    public int deleteRecord(String iwhereClause, String tableName) {

        int rowId = -1;

        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                rowId = db.delete(tableName, iwhereClause, null);

                db.close();

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return rowId;

    }

    public int updateRecord(JSONObject jsonObject, String iwhereClause, String tableName) {

        int rowId = -1;

        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                ContentValues contentValues = new ContentValues();

                Iterator<String> iter = jsonObject.keys();

                while (iter.hasNext()) {

                    String key = iter.next();

                    try {

                        String value = jsonObject.optString(key);

                        contentValues.put(key, value);

                    } catch (Exception e) {

                        TraceUtils.logException(e);

                    }

                }

                rowId = db.update(tableName, contentValues, iwhereClause, null);

                db.close();

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return rowId;

    }

    public String[] getColumnNames(String tableName) {

        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                Cursor cursor = db.query(tableName, null, null, null, null, null, null);

                String[] temp = cursor.getColumnNames();

                cursor.close();

                db.close();

                return temp;

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return null;

    }

    public void update(String CID, String path, String mimetype, String status) {

        if (database != null) {

            String iwhereClause = "material_data_id=" + CID;

            SQLiteDatabase db = database.getWritableDatabase();

            ContentValues cv = new ContentValues();

            cv.put("savedpath", path);

            cv.put("mimetype", mimetype);

            cv.put("downloadstatus", status);

            db.update("DOWNLOADQUEUE", cv, iwhereClause, null);

            db.close();

        }

    }

    public void updateDownloadStatus(String CID, String status) {

        if (database != null) {

            String iwhereClause = "material_data_id=" + CID;

            SQLiteDatabase db = database.getWritableDatabase();

            ContentValues cv = new ContentValues();

            cv.put("downloadstatus", status);

            db.update("DOWNLOADQUEUE", cv, iwhereClause, null);

            db.close();

        }

    }

    public void updateErrMsgOnFail(String CID, String errMsg) {

        if (database != null) {

            String iwhereClause = "material_data_id=" + CID;

            SQLiteDatabase db = database.getWritableDatabase();

            ContentValues cv = new ContentValues();

            cv.put("errorMsg", errMsg);

            db.update("DOWNLOADQUEUE", cv, iwhereClause, null);

            db.close();

        }

    }

    public void updateOnFailSavedPath(String CID) {

        if (database != null) {

            String iwhereClause = "material_data_id=" + CID;

            SQLiteDatabase db = database.getWritableDatabase();

            ContentValues cv = new ContentValues();

            cv.put("savedpath", "");

            db.update("DOWNLOADQUEUE", cv, iwhereClause, null);

            db.close();

        }

    }

    public void deleteTrackWithCID(String cid, String uniqueId) {

        if (database != null) {

            SQLiteDatabase db = database.getWritableDatabase();

            db.delete("DOWNLOADQUEUE", "material_data_id = " + cid + " AND uniqid = " + uniqueId, null);

            db.close();

        }

    }

    public JSONObject getTrack(String uniqid) {

        JSONObject jsonObject = null;

        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                Cursor cursor = db.rawQuery("SELECT * FROM DOWNLOADQUEUE WHERE downloadstatus='Q' ORDER BY ROWID ASC LIMIT 1",
                        null);

                if (cursor != null) {

                    while (cursor.moveToNext()) {

                        jsonObject = new JSONObject();

                        String[] resultsColumns = cursor.getColumnNames();

                        for (String key : resultsColumns) {

                            String value = cursor.getString(cursor
                                    .getColumnIndexOrThrow(key));

                            if (value != null)
                                jsonObject.put(key, value);

                        }

                    }

                    cursor.close();

                }

                db.close();

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return jsonObject;

    }
    public JSONObject getAssignmentHistoryList() {

        JSONObject jsonObject = new JSONObject();

        try {

            if (database != null) {
                JSONArray array = new JSONArray();
                SQLiteDatabase db = database.getWritableDatabase();

                Cursor cursor = db.rawQuery("SELECT * FROM ASSIGNMENT",
                        null);

                if (cursor != null) {

                    while (cursor.moveToNext()) {

                        JSONObject obj = new JSONObject();


                        String[] resultsColumns = cursor.getColumnNames();

                        for (String key : resultsColumns) {

                            String value = cursor.getString(cursor
                                    .getColumnIndexOrThrow(key));

                            obj.put(key, value);

                        }
                        array.put(obj);
                    }
                    jsonObject.put("assign_body", array);
                    cursor.close();

                }

                db.close();

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return jsonObject;

    }

    public JSONObject getExamsResult(int exam_id, int student_id) {
        JSONObject jsonObject = null;
        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                Cursor cursor = db.rawQuery("SELECT * FROM STUDENTQUESTIONTIME WHERE student_id='" + student_id + "' and exam_id='" + exam_id + "'",
                        null);

                if (cursor != null) {

                    while (cursor.moveToNext()) {

                        jsonObject = new JSONObject();

                        String[] resultsColumns = cursor.getColumnNames();

                        for (String key : resultsColumns) {

                            String value = cursor.getString(cursor
                                    .getColumnIndexOrThrow(key));

                            if (value != null)
                                jsonObject.put(key, value);

                        }

                    }

                    cursor.close();

                }

                db.close();

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }
        return jsonObject;
    }

    public JSONObject getAssignmentResult(int assign_id, int student_id) {
        JSONObject jsonObject = null;
        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                Cursor cursor = db.rawQuery("SELECT * FROM STUDENTQUESTIONTIME WHERE assignment_id='" + assign_id + "' and student_id='" + student_id + "'",
                        null);

                if (cursor != null) {

                    while (cursor.moveToNext()) {

                        jsonObject = new JSONObject();

                        String[] resultsColumns = cursor.getColumnNames();

                        for (String key : resultsColumns) {

                            String value = cursor.getString(cursor
                                    .getColumnIndexOrThrow(key));

                            if (value != null)
                                jsonObject.put(key, value);

                        }

                    }

                    cursor.close();

                }

                db.close();

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }
        return jsonObject;
    }

    public JSONObject getAssignmentHistoryResult(String id) {
        JSONObject jsonObject = null;
        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                Cursor cursor = db.rawQuery("SELECT * FROM ASSIGNMENTRESULTS WHERE assignment_id='" + id +"'",
                        null);

                if (cursor != null) {

                    while (cursor.moveToNext()) {

                        jsonObject = new JSONObject();

                        String[] resultsColumns = cursor.getColumnNames();

                        for (String key : resultsColumns) {

                            String value = cursor.getString(cursor
                                    .getColumnIndexOrThrow(key));

                            if (value != null)
                                jsonObject.put(key, value);

                        }

                    }

                    cursor.close();

                }

                db.close();

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }
        return jsonObject;
    }
    public JSONArray getRecords(String whereClause, String tableName) {

        JSONArray jsonArray = new JSONArray();

        try {

            if (database != null) {

                SQLiteDatabase db = database.getWritableDatabase();

                Cursor cursor = db.query(tableName, null, whereClause,
                        null, null, null, null);

                if (cursor != null) {

                    while (cursor.moveToNext()) {

                        JSONObject jsonObject = new JSONObject();

                        String[] resultsColumns = cursor.getColumnNames();

                        for (String key : resultsColumns) {

                            String value = cursor.getString(cursor
                                    .getColumnIndexOrThrow(key));

                            if (value != null)
                                jsonObject.put(key, value);

                        }

                        jsonArray.put(jsonObject);

                    }

                    cursor.close();

                }

                db.close();

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return jsonArray;

    }
}
