package com.taskflow.app.ui;
import android.app.Application;
import android.os.*;
import androidx.lifecycle.*;
import com.taskflow.app.data.*;
import com.taskflow.app.domain.TaskRanker;
import java.util.*;
public class TaskViewModel extends AndroidViewModel {
 private final TaskRepository repository;
 public final MediatorLiveData<List<Task>> visible=new MediatorLiveData<>();
 public final MutableLiveData<String> keyword=new MutableLiveData<>("");
 public final MutableLiveData<String> filter=new MutableLiveData<>("ALL");
 private List<Task> all=Collections.emptyList();
 private final Handler timer=new Handler(Looper.getMainLooper());
 private final Runnable tick=new Runnable(){public void run(){refresh();timer.postDelayed(this,60000);}};
 public TaskViewModel(Application app){super(app);repository=new TaskRepository(app);
  visible.addSource(repository.observeAll(),list->{all=list;refresh();});
  visible.addSource(keyword,q->refresh());visible.addSource(filter,s->refresh());timer.postDelayed(tick,60000);
 }
 public void refresh(){visible.setValue(TaskRanker.select(all,keyword.getValue(),filter.getValue(),System.currentTimeMillis()));}
 public LiveData<String> errors(){return repository.errors();}
 public void save(Task t){repository.save(t);}
 public void delete(Task t){repository.delete(t);}
 public void advance(Task t){Task next=t.copy();next.status="TODO".equals(t.status)?"IN_PROGRESS":"IN_PROGRESS".equals(t.status)?"DONE":"TODO";save(next);}
 @Override protected void onCleared(){timer.removeCallbacks(tick);}
}
