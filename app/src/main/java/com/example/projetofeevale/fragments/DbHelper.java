package com.example.projetofeevale.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.projetofeevale.fragments.FormCreatePin.Pin;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Crud.db";
    private static final int DATABASE_VERSION = 4;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE Pins (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "imagem BLOB NOT NULL," + // Alterado para BLOB para armazenar bytes da imagem
                "endereco TEXT NOT NULL, tipo TEXT NOT NULL," +
                " dataHora TEXT NOT NULL, titulo TEXT NOT NULL, descricao TEXT NOT NULL);";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if needed
    }

    public long insertPin(Pin pin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("imagem", pin.getImagemBytes()); // Use o método getImagemBytes() para obter os bytes da imagem
        values.put("endereco", pin.getEndereco());
        values.put("tipo", pin.getTipo());
        values.put("dataHora", pin.getDataHora());
        values.put("titulo", pin.getTitulo());
        values.put("descricao", pin.getDescricao());
        long newRowId = db.insert("Pins", null, values);
        db.close();
        return newRowId;
    }

    public List<Pin> getData() {
        List<Pin> pinList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Pins", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
                byte[] imagemBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("imagem")); // Recupera os bytes da imagem
                String endereco = cursor.getString(cursor.getColumnIndexOrThrow("endereco"));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"));
                String dataHora = cursor.getString(cursor.getColumnIndexOrThrow("dataHora"));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
                String descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"));

                Pin pin = new Pin(id, imagemBytes, endereco, tipo, dataHora, titulo, descricao);
                pinList.add(pin);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pinList;
    }
}