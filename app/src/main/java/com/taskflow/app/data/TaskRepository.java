package com.taskflow.app.data;
import android.content.Context;
import androidx.lifecycle.*;
import java.util.List;
import java.util.concurrent.*;
public class TaskRepository {
 private static final ExecutorService IO=Executors.newSingleThreadExecutor();
 private final TaskDao dao;
 private final MutableLiveData<String> errors=new MutableLiveData<>();
 public TaskRepository(Context context) { dao=TaskDatabase.get(context).tasks(); }
 public LiveData<List<Task>> observeAll(){return dao.observeAll();}
 public LiveData<String> errors(){return errors;}
 private void run(Runnable action){ IO.execute(()->{try{action.run();}catch(Exception e){errors.postValue("保存失败，请重试："+e.getClass().getSimpleName());}}); }
 public void save(Task t){ Task copy=t.copy(); run(()->{if(copy.id==0)dao.insert(copy);else dao.update(copy);}); }
 public void delete(Task t){run(()->dao.delete(t));}
}
