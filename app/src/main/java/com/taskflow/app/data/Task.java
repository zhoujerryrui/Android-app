package com.taskflow.app.data;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
@Entity(tableName="tasks")
public class Task {
 @PrimaryKey(autoGenerate=true) public long id;
 @NonNull public String title="";
 @NonNull public String description="";
 @NonNull public String status="TODO";
 public int priority=2;
 public Long dueAt;
 public long createdAt=System.currentTimeMillis();
 public Task() {}
 public Task copy() { Task t=new Task(); t.id=id;t.title=title;t.description=description;t.status=status;t.priority=priority;t.dueAt=dueAt;t.createdAt=createdAt;return t; }
}
