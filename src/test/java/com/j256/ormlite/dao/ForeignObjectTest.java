package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class ForeignObjectTest extends BaseCoreTest {

	@Test
	public void testQuestionAndAnswers() throws Exception {
		Dao<Question, Object> questionDao = createDao(Question.class, true);
		Dao<Answer, Object> answerDao = createDao(Answer.class, true);

		Question question = new Question();
		question.name = "some question";
		assertEquals(1, questionDao.create(question));

		Answer answer1 = new Answer();
		answer1.val = 1234313123;
		answer1.question = question;
		assertEquals(1, answerDao.create(answer1));

		Answer answer2 = new Answer();
		answer2.val = 345543;
		answer2.question = question;
		assertEquals(1, answerDao.create(answer2));

		Question questionResult = questionDao.queryForId(question.id);
		assertNull(questionResult.bestAnswer);
		assertNotNull(questionResult.answers);
		assertEquals(2, questionResult.answers.size());
		questionResult.bestAnswer = answer2;
		assertEquals(1, questionDao.update(questionResult));

		Answer answerResult = answerDao.queryForId(answer1.id);
		assertNotNull(answerResult.question);
		assertEquals(question.id, answerResult.question.id);
		assertEquals(question.name, answerResult.question.name);
		assertNotNull(answerResult.question.answers);
		assertEquals(2, answerResult.question.answers.size());

		questionResult = questionDao.queryForId(question.id);
		assertNotNull(questionResult.bestAnswer);
		assertEquals(answer2.id, questionResult.bestAnswer.id);
		assertEquals(answer2.val, questionResult.bestAnswer.val);
		assertEquals(question.id, questionResult.bestAnswer.question.id);
		assertEquals(question.name, questionResult.bestAnswer.question.name);
		assertNotNull(questionResult.bestAnswer.question.bestAnswer);
		assertEquals(answer2.id, questionResult.bestAnswer.question.bestAnswer.id);
		assertEquals(0, questionResult.bestAnswer.question.bestAnswer.val);
	}

	@Test
	public void testCreateOrUpdate() throws Exception {
		Dao<Question, Object> questionDao = createDao(Question.class, true);
		Dao<Answer, Object> answerDao = createDao(Answer.class, true);

		// not failed
		Question question1 = new Question();
		question1.name = "some question";

		Answer answer1 = new Answer();
		answer1.val = 1234313123;
		assertEquals(1, answerDao.create(answer1));

		question1.bestAnswer = answer1;
		assertEquals(1, questionDao.create(question1));

		questionDao.refresh(question1);
		assertNotNull(question1.bestAnswer);

		// create new question
		Question question2 = new Question();
		question2.name = "some question2";
		assertEquals(1, questionDao.createOrUpdate(question2).getNumLinesChanged());

		Answer answer2 = new Answer();
		answer2.val = 87879723;
		assertEquals(1, answerDao.create(answer2));

		question2.bestAnswer = answer2;
		assertEquals(1, questionDao.createOrUpdate(question2).getNumLinesChanged());

		Question result = questionDao.queryForId(question2.id);
		assertNotNull(result);
		assertNotNull(result.bestAnswer);
		assertEquals(answer2.id, result.bestAnswer.id);

		questionDao.refresh(question2);
		assertNotNull(question2.bestAnswer);
		assertEquals(answer2.id, question2.bestAnswer.id);
	}

	@Test
	public void testForeignAutoCreate() throws Exception {
		Dao<Parent, Long> parentDao = createDao(Parent.class, true);
		createTable(Child.class, true);

		Parent parent = new Parent();
		Child child = new Child();
		parent.child = child;

		assertEquals(1, parentDao.create(parent));
		Parent result = parentDao.queryForId(parent.id);
		assertNotNull(result);
		assertNotNull(result.child);
		assertEquals(child.id, result.child.id);
	}

	protected static class Question {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Answer bestAnswer;
		@ForeignCollectionField(eager = true)
		ForeignCollection<Answer> answers;
	}

	protected static class Answer {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(unique = true)
		int val;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Question question;
	}

	public static class Parent {
		@DatabaseField(columnName = "_id", generatedId = true)
		public long id;

		@DatabaseField(columnName = "foreignId", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true,
				foreignColumnName = "api_id", canBeNull = false)
		public Child child;
	}

	public static class Child {
		@DatabaseField(columnName = "_id", generatedId = true)
		public long id;

		@DatabaseField(columnName = "api_id")
		public long child_api_id;
	}
}
