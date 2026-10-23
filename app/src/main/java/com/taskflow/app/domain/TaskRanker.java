package com.taskflow.app.domain;
import com.taskflow.app.data.Task;
import java.util.*;
public final class TaskRanker {
 private TaskRanker(){}
 public static int score(Task t,long now){
  if("DONE".equals(t.status))return -1;
  int points=t.priority*100 + ("IN_PROGRESS".equals(t.status)?30:0);
  if(t.dueAt!=null){long remaining=t.dueAt-now;
   if(remaining<0)points+=400;
   else if(remaining<=86400000L)points+=300;
   else if(remaining<=3*86400000L)points+=200;
   else if(remaining<=7*86400000L)points+=100;
  }
  return points;
 }
 public static List<Task> select(List<Task> input,String keyword,String status,long now){
  String q=keyword.trim().toLowerCase(Locale.ROOT);
  List<Task> out=new ArrayList<>();
  for(Task t:input)if(("ALL".equals(status)||status.equals(t.status))&&(t.title.toLowerCase(Locale.ROOT).contains(q)||t.description.toLowerCase(Locale.ROOT).contains(q)))out.add(t);
  out.sort(Comparator.<Task>comparingInt(t->score(t,now)).reversed()
   .thenComparingLong(t->t.dueAt==null?Long.MAX_VALUE:t.dueAt)
   .thenComparing(Comparator.comparingLong((Task t)->t.createdAt).reversed()).thenComparingLong(t->t.id));
  return out;
 }
}
