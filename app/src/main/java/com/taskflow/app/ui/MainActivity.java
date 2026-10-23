package com.taskflow.app.ui;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.text.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import com.taskflow.app.data.Task;
import com.taskflow.app.domain.TaskRanker;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
 private TaskViewModel vm;
 private LinearLayout list;
 private TextView count;
 private final int blue=Color.rgb(49,94,234), ink=Color.rgb(25,37,65), muted=Color.rgb(110,122,145);
 private final SimpleDateFormat date=new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.CHINA);
 private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n);}
 private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
 private TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);return v;}
 private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(blue);b.setTextSize(12);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(4),0,dp(4),0);return b;}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);vm=new ViewModelProvider(this).get(TaskViewModel.class);
  LinearLayout root=column();root.setPadding(dp(20),dp(16),dp(20),0);root.setBackgroundColor(0xFFF5F7FC);setContentView(root);
  TextView brand=text("TASKFLOW  /  工作空间",12,blue);brand.setLetterSpacing(.12f);root.addView(brand);
  TextView title=text("把重要的事，向前推进",26,ink);title.setTypeface(null,Typeface.BOLD);title.setPadding(0,dp(18),0,dp(6));root.addView(title);
  root.addView(text("任务管理 · 本地保存 · 专注当下",13,muted));
  Button add=button("＋ 创建任务");add.setTextColor(Color.WHITE);add.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue));root.addView(add,new LinearLayout.LayoutParams(-1,dp(54)) );add.setOnClickListener(v->edit(null));
  EditText search=new EditText(this);search.setSingleLine();search.setTextSize(15);search.setHint("搜索任务标题或描述");search.setText(vm.keyword.getValue());root.addView(search);
  String[] labels={"全部任务","待处理 · TODO","进行中 · IN_PROGRESS","已完成 · DONE"};String[] states={"ALL","TODO","IN_PROGRESS","DONE"};
  Spinner filter=new Spinner(this);filter.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels));root.addView(filter,new LinearLayout.LayoutParams(-1,dp(48)));filter.setSelection(Arrays.asList(states).indexOf(vm.filter.getValue()));
  filter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int pos,long id){vm.filter.setValue(states[pos]);}public void onNothingSelected(AdapterView<?> p){}});
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int c,int a){}public void onTextChanged(CharSequence s,int start,int before,int c){vm.keyword.setValue(s.toString());}public void afterTextChanged(Editable e){}});
  count=text("",12,muted);count.setPadding(0,dp(8),0,dp(12));root.addView(count);
  ScrollView scroll=new ScrollView(this);list=column();scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  vm.visible.observe(this,tasks->{list.removeAllViews();count.setText(tasks.size()+" 个任务  ·  按紧急评分自动排序");if(tasks.isEmpty()){TextView empty=text("暂无任务\n点击「创建任务」开始，或调整搜索条件",16,muted);empty.setPadding(0,dp(48),0,0);list.addView(empty);}else for(Task t:tasks)card(t);});
  vm.errors().observe(this,error->{if(error!=null)Toast.makeText(this,error,Toast.LENGTH_LONG).show();});
 }
 @Override protected void onResume(){super.onResume();if(vm!=null)vm.refresh();}
 private void card(Task t){
  LinearLayout c=column();c.setPadding(dp(16),dp(14),dp(16),dp(12));c.setBackground(bg(Color.WHITE,16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);list.addView(c,lp);
  String status="TODO".equals(t.status)?"待处理":"IN_PROGRESS".equals(t.status)?"进行中":"已完成";
  c.addView(text(status+"   /   "+new String[]{"","低优先级","中优先级","高优先级"}[t.priority],12,blue));
  TextView title=text(t.title,19,ink);title.setTypeface(null,Typeface.BOLD);title.setPadding(0,dp(8),0,dp(4));c.addView(title);
  if(!t.description.isEmpty())c.addView(text(t.description,14,muted));
  boolean overdue=t.dueAt!=null&&t.dueAt<System.currentTimeMillis()&&!"DONE".equals(t.status);
  TextView meta=text((t.dueAt==null?"无截止时间":(overdue?"已逾期 · ":"截止 · ")+date.format(new Date(t.dueAt)))+"  |  评分 "+TaskRanker.score(t,System.currentTimeMillis()),11,overdue?0xFFCA4444:muted);meta.setPadding(0,dp(10),0,0);c.addView(meta);
  LinearLayout actions=new LinearLayout(this);c.addView(actions);
  Button next=button("TODO".equals(t.status)?"开始任务":"IN_PROGRESS".equals(t.status)?"标记完成":"重新打开");actions.addView(next,new LinearLayout.LayoutParams(0,-2,1.5f));next.setOnClickListener(v->vm.advance(t));
  Button edit=button("编辑");actions.addView(edit,new LinearLayout.LayoutParams(0,-2,1));edit.setOnClickListener(v->edit(t));
  Button delete=button("删除");actions.addView(delete,new LinearLayout.LayoutParams(0,-2,1));delete.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("删除任务？").setMessage(t.title+"\n删除后无法恢复。").setNegativeButton("取消",null).setPositiveButton("删除",(d,w)->vm.delete(t)).show());
 }
 private void edit(Task original){
  Task draft=original==null?new Task():original.copy();
  LinearLayout form=column();form.setPadding(dp(22),dp(8),dp(22),dp(8));
  EditText title=new EditText(this);title.setHint("任务标题（必填，最多100字）");title.setText(draft.title);title.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});form.addView(title);
  EditText desc=new EditText(this);desc.setHint("任务描述");desc.setText(draft.description);desc.setMinLines(2);desc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2000)});form.addView(desc);
  form.addView(text("优先级",13,muted));Spinner priority=new Spinner(this);priority.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"低","中","高"}));priority.setSelection(draft.priority-1);form.addView(priority);
  Button due=button(draft.dueAt==null?"设置截止日期与时间":date.format(new Date(draft.dueAt)));form.addView(due);
  due.setOnClickListener(v->{Calendar cal=Calendar.getInstance();if(draft.dueAt!=null)cal.setTimeInMillis(draft.dueAt);new android.app.DatePickerDialog(this,(d,y,m,day)->{cal.set(y,m,day);new android.app.TimePickerDialog(this,(time,h,min)->{cal.set(Calendar.HOUR_OF_DAY,h);cal.set(Calendar.MINUTE,min);cal.set(Calendar.SECOND,0);cal.set(Calendar.MILLISECOND,0);draft.dueAt=cal.getTimeInMillis();due.setText(date.format(cal.getTime()));},cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),true).show();},cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show();});
  Button clear=button("清除截止时间");form.addView(clear);clear.setOnClickListener(v->{draft.dueAt=null;due.setText("设置截止日期与时间");});
  ScrollView scroll=new ScrollView(this);scroll.addView(form);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle(original==null?"创建任务":"编辑任务").setView(scroll).setNegativeButton("取消",null).setPositiveButton("保存",null).create();
  dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String value=title.getText().toString().trim();if(value.isEmpty()){title.setError("请输入任务标题");return;}draft.title=value;draft.description=desc.getText().toString().trim();draft.priority=priority.getSelectedItemPosition()+1;vm.save(draft);dialog.dismiss();}));dialog.show();
 }
}
