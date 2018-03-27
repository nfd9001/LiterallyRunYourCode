package Persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-02-17.
 * Database where I'm stashing Programs
 */
@Database(entities = {Program.class}, version = 1)
public abstract class ProgramDatabase extends RoomDatabase {
    private static ProgramDatabase d;

    public abstract ProgramDao programDao();
}
