package Persistence;

import android.arch.persistence.room.*;

import java.util.List;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-02-17.
 * Data Access Object connecting to a sqlite DB representing Program objects
 */
@Dao
public interface ProgramDao {
    @Query("SELECT * FROM program")
    List<Program> getAll();

    @Query("SELECT * FROM program WHERE name LIKE :name")
    Program findByName(String name);

    @Insert
    void insertAll(Program... programs);

    @Update
    void updatePrograms(Program... programs);

    @Delete
    void deletePrograms(Program... programs);
}
