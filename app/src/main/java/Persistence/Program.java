package Persistence;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * @author Alexander Ronsse-Tucherov
 *
 * Room Entity class to persistently store programs
 */
@Entity
public class Program {
    @PrimaryKey
    @NonNull
    private String name;

    @NonNull
    private String source;


    public Program(String name, String source){
        this.name = name;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
