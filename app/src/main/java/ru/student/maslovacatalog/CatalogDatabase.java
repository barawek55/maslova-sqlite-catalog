package ru.student.maslovacatalog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CatalogDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "catalog.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public CatalogDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public void prepareDatabase() throws IOException {
        File destination = context.getDatabasePath(DATABASE_NAME);
        if (destination.exists()) {
            return;
        }
        File directory = destination.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Не удалось создать каталог базы данных");
        }
        try (InputStream source = context.getAssets().open(DATABASE_NAME);
             OutputStream target = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = source.read(buffer)) != -1) {
                target.write(buffer, 0, count);
            }
        } catch (IOException error) {
            destination.delete();
            throw error;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    }
}
