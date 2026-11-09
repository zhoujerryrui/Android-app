import unittest
from unittest.mock import patch
import server
class ValidationTests(unittest.TestCase):
 def draft(self,**extra):return dict(title='完成登录',priority=2,description='',subtasks=[],due_at=None,clarification='',**extra)
 def test_draft(self):self.assertEqual(server.validate_draft(self.draft())['title'],'完成登录')
 def test_timezone(self):
  d=self.draft();d['due_at']='2026-11-10T18:00:00+08:00';self.assertIsInstance(server.validate_draft(d)['dueAt'],int)
 def test_naive_date(self):
  d=self.draft();d['due_at']='2026-11-10T18:00:00'
  with self.assertRaises(ValueError):server.validate_draft(d)
 def test_bad_priority(self):
  for x in [True,0,4,'2']:
   d=self.draft();d['priority']=x
   with self.assertRaises(ValueError):server.validate_draft(d)
 def test_bad_title(self):
  for x in ['',None,'a'*101]:
   d=self.draft();d['title']=x
   with self.assertRaises(ValueError):server.validate_draft(d)
 def test_clarify(self):self.assertEqual(server.validate_draft({'clarification':'哪天？'})['clarification'],'哪天？')
 def test_empty_summary(self):self.assertEqual(server.execute('/summary',{'message':'总结','tasks':[]})['task_ids'],[])
 def test_unknown_reference(self):
  with patch.object(server,'model_json',return_value={'summary':'任务','task_ids':[99]}):
   with self.assertRaises(ValueError):server.execute('/summary',{'message':'总结','tasks':[{'id':1,'title':'真实任务','status':'TODO'}]})
 def test_summary(self):
  with patch.object(server,'model_json',return_value={'summary':'任务尚未完成','task_ids':[1]}):self.assertEqual(server.execute('/summary',{'message':'总结','tasks':[{'id':1,'title':'真实任务','status':'TODO'}]})['task_ids'],[1])
 def test_no_key(self):
  with patch.dict('os.environ',{'DEEPSEEK_API_KEY':''}):
   with self.assertRaises(RuntimeError):server.model_json('json',{})
 def test_length(self):
  with self.assertRaises(ValueError):server.execute('/draft',{'message':'x'*2001})
 def test_subtasks(self):
  d=self.draft();d['subtasks']=['x']*11
  with self.assertRaises(ValueError):server.validate_draft(d)
if __name__=='__main__':unittest.main()
