package com.karhades.tag_it.main.database;

/**
 * Defines the database schema contract.
 */
public interface TagItDatabaseSchema {

    interface TagTable {

        String NAME = "tags";

        interface Columns {

            String TAG_ID = "tag_id";
            String TITLE = "title";
            String FILE_PATH = "file_path";
            String DIFFICULTY = "difficulty";
            String DISCOVERED = "discovered";
            String DATE_DISCOVERED = "date_discovered";
        }
    }
}
