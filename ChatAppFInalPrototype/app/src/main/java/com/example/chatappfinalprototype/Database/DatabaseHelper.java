package com.example.chatappfinalprototype.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.chatappfinalprototype.Model.Contact;
import com.example.chatappfinalprototype.Model.Message;
import com.example.chatappfinalprototype.Model.User;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "log9999";

    public DatabaseHelper(Context context) {
        super(context, "ChatApp", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contact (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, number TEXT)");
        db.execSQL("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT, name Text, number TEXT, image TEXT, room INTEGER)");
        db.execSQL("CREATE TABLE pendingmessage (id INTEGER PRIMARY KEY AUTOINCREMENT, receiver TEXT, data TEXT, time TEXT, type TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS contact");
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS pendingmessage");
        onCreate(db);
    }

    public void addMessage(String receiver, String data, String time, String type) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("receiver", receiver);
        contentValues.put("data", data);
        contentValues.put("time", time);
        contentValues.put("type", type);
        database.insert("pendingmessage", null, contentValues);
        database.close();
    }

    public ArrayList<Message> fetchALLMessage() {
//        switch receiver with uuid
        ArrayList<Message> messageArrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM pendingmessage", null);
        while (cursor.moveToNext()) {
            Message message = new Message();
            message.setSenderId(cursor.getString(1));
            message.setData(cursor.getString(2));
            message.setTimeStamp(cursor.getString(3));
            message.setType(cursor.getString(4));

            messageArrayList.add(message);
            Log.d("log9999", "fetchALLUser: -> " + message.toString());
        }
        database.close();
        Log.d("log9999", "fetchALLUser: size -> " + messageArrayList.size());
        return messageArrayList;
    }


    public void addUser(String uuid, String name, String number, String image) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", uuid);
        contentValues.put("name", name);
        contentValues.put("number", number);
        contentValues.put("image", "image");
        contentValues.put("room", 0);
        database.insert("user", null, contentValues);
        database.close();
    }

    public ArrayList<User> fetchALLUser() {
        ArrayList<User> userArrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user", null);
        while (cursor.moveToNext()) {
            User user = new User();
            user.setUuid(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setNumber(cursor.getString(3));
            user.setImage(cursor.getString(4));
            user.setRoom(cursor.getInt(5));
            userArrayList.add(user);
            Log.d(TAG, "fetchALLUser: -> " + user.toString());
        }
        database.close();
        Log.d(TAG, "fetchALLUser: size -> " + userArrayList.size());
        return userArrayList;
    }

    public User getUser(String number) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE number = " + number, null);
        if (cursor.getCount() == 0) return null;
        User user = new User();
        while (cursor.moveToNext()) {
            user.setUuid(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setNumber(cursor.getString(3));
            user.setImage(cursor.getString(4));
            user.setRoom(cursor.getInt(5));
            break;
        }
        database.close();
        return user;
    }

    public void addContact(String name, String number) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("number", number);
        database.insert("contact", null, contentValues);
        database.close();
    }

    public ArrayList<Contact> fetchAllContacts() {
        ArrayList<Contact> userArrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user", null);
        while (cursor.moveToNext()) {
            Contact contact = new Contact();
            contact.setName(cursor.getString(2));
            contact.setNumber(cursor.getString(3));
            userArrayList.add(contact);
            Log.d(TAG, "fetchALLContact: -> " + contact.toString());
        }
        database.close();
        return userArrayList;
    }

    public Contact getContact(String number) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE number = " + number, null);
        if (cursor.getCount() == 0) return null;
        Contact contact = new Contact();
        while (cursor.moveToNext()) {
            contact.setName(cursor.getString(2));
            contact.setNumber(cursor.getString(3));
            break;
        }
        database.close();
        return contact;
    }

    public ArrayList<Message> readChat(String personUuid) {
        ArrayList<Message> messageArrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE uuid = " + personUuid, null);
        if (cursor.getCount() == 0) Log.d(TAG, "readChat: no user found" + personUuid);

        int room = 0;
        while (cursor.moveToNext()) {
            room = cursor.getInt(5);
            Log.d(TAG, "readChat: room -_ " + room);
            break;
        }
        if (room == 1) {
            Cursor c = database.rawQuery("SELECT * FROM room" + personUuid, null);
            Log.d(TAG, "readChat: count - " + c.getCount());
            while (c.moveToNext()) {
                Message message = new Message(c.getString(1), c.getString(2), c.getString(3), c.getString(4));
                Log.d(TAG, "readChat: -> " + message.toString());
                messageArrayList.add(message);
            }
        }
        database.close();
        return messageArrayList;
    }


    //    TODO fix the string to message
    public void writeChat(Message message, String personUuid) {
        Log.d(TAG, "writeChat: => uuid " + personUuid);

        SQLiteDatabase database = this.getWritableDatabase();
        //
        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE uuid = " + personUuid, null);
        int room = 0;
        while (cursor.moveToNext()) {
            room = cursor.getInt(5);
            Log.d(TAG, "readChat: room -_ " + room);
            break;
        }
        //
        if (room == 0) {
            String CREATE_TABLE = "CREATE TABLE room" + personUuid + " (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT, senderId TEXT, timeStamp TEXT, type TEXT)";
            database.execSQL(CREATE_TABLE);
            ContentValues cv = new ContentValues();
            cv.put("room", 1);
            database.update("user", cv, "uuid = " + personUuid, null);
        }

        ContentValues contentValues = new ContentValues();
        new Message();
        contentValues.put("data", message.getData());
        contentValues.put("senderId", message.getSenderId());
        contentValues.put("timeStamp", message.getTimeStamp());
        contentValues.put("type", message.getType());
        database.insert("room" + personUuid, null, contentValues);
        database.close();
    }


    public void deleteAllMessages(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("pendingmessage", null, null);
    }

    public void createNewTable(String name) {
        SQLiteDatabase database = this.getWritableDatabase();
        String CREATE_TABLE = "CREATE TABLE " + name + " (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT, senderId TEXT, timeStamp TEXT, type TEXT)";
        database.execSQL(CREATE_TABLE);
        database.close();
    }

    public void returnTablesName() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Log.d("log8888", "returnDatabasesName: - Table Name => " + c.getString(0));
                c.moveToNext();
            }
        }
        database.close();
    }
}
