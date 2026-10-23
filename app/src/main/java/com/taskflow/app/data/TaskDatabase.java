package com.taskflow.app.data;
import android.content.Context;
import androidx.room.*;
@Database(entities={Task.class},version=1,exportSchema=true)
public abstract class TaskDatabase extends RoomDatabase {
 public abstract TaskDao tasks();
 private static volatile TaskDatabase instance;
 public static TaskDatabase get(Context context) {
  if(instance==null) synchronized(TaskDatabase.class) {
   if(instance==null) instance=Room.databaseBuilder(context.getApplicationContext(),TaskDatabase.class,"taskflow.db").build();
  }
  return instance;
 }
}
