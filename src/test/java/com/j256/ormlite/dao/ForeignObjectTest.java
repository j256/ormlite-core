package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.ForeignCollectionTest.Order;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class ForeignObjectTest extends BaseCoreTest {

	@Test
	public void testQuestionAndAnswers() throws Exception {
		createDao(Order.class, true);
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
		// this is expected since we don't auto-refresh foreign object foreign collection fields
		assertNull(answerResult.question.answers);

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

	protected static class Question {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Answer bestAnswer;
		@ForeignCollectionField(eager = true)
		ForeignCollection<Answer> answers;
		protected Question() {
		}
	}

	protected static class Answer {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(unique = true)
		int val;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Question question;
		protected Answer() {
		}
	}
}
