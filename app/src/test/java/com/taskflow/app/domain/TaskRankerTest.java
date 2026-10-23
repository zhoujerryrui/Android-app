package com.taskflow.app.domain;
import com.taskflow.app.data.Task;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class TaskRankerTest {
 private Task task(int p,String s,Long due){Task t=new Task();t.priority=p;t.status=s;t.dueAt=due;return t;}
 @Test public void overdueOutranksHighWithoutDue(){assertTrue(TaskRanker.score(task(1,"TODO",0L),1)>TaskRanker.score(task(3,"TODO",null),1));}
 @Test public void completedAlwaysLast(){assertEquals(-1,TaskRanker.score(task(3,"DONE",0L),1));}
 @Test public void urgencyChangesWithTime(){Task t=task(2,"TODO",4*86400000L);assertTrue(TaskRanker.score(t,2*86400000L)>TaskRanker.score(t,0));}
 @Test public void boundaryAndProgress(){assertEquals(500,TaskRanker.score(task(2,"TODO",86400000L),0));assertEquals(530,TaskRanker.score(task(2,"IN_PROGRESS",86400000L),0));}
 @Test public void searchAndFilterCombine(){Task a=task(1,"TODO",null);a.title="Release";Task b=task(3,"DONE",null);b.description="release";assertEquals(1,TaskRanker.select(Arrays.asList(a,b)," RELEASE ","TODO",0).size());assertEquals(0,TaskRanker.select(Arrays.asList(a,b),"absent","ALL",0).size());}
 @Test public void equalScoresUseDeadline(){Task a=task(2,"TODO",800L),b=task(2,"TODO",900L);assertSame(a,TaskRanker.select(Arrays.asList(b,a),"","ALL",0).get(0));}
}
