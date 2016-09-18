package com.karhades.tag_it.main.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.karhades.tag_it.main.model.NfcTag;

/**
 * Helper class to create an NfcTag using the Cursor object.
 */
public class NfcTagCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public NfcTagCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public NfcTag getNfcTag() {
        String tagId = getString(getColumnIndex(TagItDatabaseSchema.TagTable.Columns.TAG_ID));
        String title = getString(getColumnIndex(TagItDatabaseSchema.TagTable.Columns.TITLE));
        String filePath = getString(getColumnIndex(TagItDatabaseSchema.TagTable.Columns.FILE_PATH));
        String difficulty = getString(getColumnIndex(TagItDatabaseSchema.TagTable.Columns.DIFFICULTY));
        int discovered = getInt(getColumnIndex(TagItDatabaseSchema.TagTable.Columns.DISCOVERED));
        String dateDiscovered = getString(getColumnIndex(TagItDatabaseSchema.TagTable.Columns.DATE_DISCOVERED));

        return new NfcTag(tagId, title, filePath, difficulty, discovered != 0, dateDiscovered);
    }
}
