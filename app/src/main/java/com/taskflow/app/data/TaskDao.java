package com.taskflow.app.data;
import androidx.room.*;
import androidx.lifecycle.LiveData;
import java.util.List;
@Dao public interface TaskDao {
 @Query("SELECT * FROM tasks") LiveData<List<Task>> observeAll();
 @Insert long insert(Task task);
 @Update void update(Task task);
 @Delete void delete(Task task);
}
