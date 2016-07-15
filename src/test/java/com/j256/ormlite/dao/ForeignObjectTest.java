package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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
	public void testCreateOrUpdateLoop() throws Exception {
		Dao<Question, Object> questionDao = createDao(Question.class, true);
		Dao<Answer, Object> answerDao = createDao(Answer.class, true);

		Answer answer = new Answer();
		answer.val = 1234313123;
		assertEquals(1, answerDao.create(answer));

		Question[] questions = new Question[10];
		for (int i = 0; i < questions.length; i++) {
			// not failed
			questions[i] = new Question();
			questions[i].name = "name_" + i;
			questions[i].bestAnswer = answer;
			assertTrue(questionDao.createOrUpdate(questions[i]).isCreated());
		}

		for (int i = 0; i < questions.length; i++) {
			// not failed
			Question result = questionDao.queryForId(questions[i].id);
			assertNotNull(result);
			assertNotNull(result.bestAnswer);
			assertEquals(answer.id, result.bestAnswer.id);
		}
	}

	@Test
	public void testForeignAutoCreate() throws Exception {
		Dao<Parent, Integer> parentDao = createDao(Parent.class, true);
		Dao<Child, Integer> childDao = createDao(Child.class, true);

		Parent parent = new Parent();
		Child child = new Child();
		parent.child = child;
		// no create here, it's handled by auto-create

		assertEquals(1, parentDao.create(parent));
		Parent result = parentDao.queryForId(parent.id);
		assertNotNull(result);
		assertNotNull(result.child);
		assertEquals(child.id, result.child.id);

		Child childResult = childDao.queryForId(child.id);
		assertNotNull(childResult);
		assertEquals(child.id, childResult.id);
	}

	@Test
	public void testCreateOrUpdateMultiple2() throws Exception {
		Dao<Child, Integer> childDao = createDao(Child.class, true);
		Dao<Parent, Object> parentDao = createDao(Parent.class, true);

		Child child = new Child();
		child.api_id = 1;
		// no create because of auto-create

		Parent parent1 = new Parent();
		parent1.child = child;
		assertTrue(parentDao.createOrUpdate(parent1).isCreated());
		Parent parent2 = new Parent();
		parent2.child = child;
		assertTrue(parentDao.createOrUpdate(parent2).isCreated());

		assertEquals(1, childDao.countOf());

		List<Parent> results = parentDao.queryForAll();
		assertNotNull(results);
		assertEquals(2, results.size());
		Parent result = results.get(0);
		assertEquals(1, result.id);
		assertNotNull(result.child);
		assertEquals(child.id, result.child.id);
		result = results.get(1);
		assertEquals(2, result.id);
		assertNotNull(result.child);
		assertEquals(child.id, result.child.id);
	}

	@Test
	public void testCreateOrUpdateMultipleLoop() throws Exception {
		Dao<Child, Integer> childDao = createDao(Child.class, true);
		Dao<Parent, Object> parentDao = createDao(Parent.class, true);

		Child child = new Child();
		child.api_id = 1;
		// no create because of auto-create

		Parent[] parents = new Parent[10];
		for (int i = 0; i < parents.length; i++) {
			parents[i] = new Parent();
			parents[i].child = child;
			assertTrue(parentDao.createOrUpdate(parents[i]).isCreated());
		}

		assertEquals(1, childDao.countOf());
		assertEquals(10, parentDao.countOf());

		for (int i = 0; i < parents.length; i++) {
			Parent result = parentDao.queryForId(parents[i].id);
			assertNotNull(result);
			assertNotNull(result.child);
			assertEquals(child.id, result.child.id);
		}
	}

	@Test
	public void testCreateOrUpdateMultipleLoopManualId() throws Exception {
		Dao<Child, Integer> childDao = createDao(Child.class, true);
		Dao<Parent, Object> parentDao = createDao(Parent.class, true);

		// should have no entries
		assertEquals(0, childDao.countOf());

		Parent[] parents = new Parent[10];
		for (int i = 0; i < parents.length; i++) {
			parents[i] = new Parent();
			Child child = new Child();
			child.id = i + 1;
			parents[i].child = child;
			assertTrue(parentDao.createOrUpdate(parents[i]).isCreated());
		}

		assertEquals(0, childDao.countOf());
		assertEquals(10, parentDao.countOf());
	}

	@Test
	public void testCreateOrUpdateManualId() throws Exception {
		Dao<Child, Integer> childDao = createDao(Child.class, true);
		Dao<Parent, Object> parentDao = createDao(Parent.class, true);

		// this group doesn't exists in database
		Child child1 = new Child();
		child1.id = 1;

		List<Parent> parents = new ArrayList<Parent>();
		for (int i = 0; i < 5; i++) {
			Parent parent = new Parent();
			parent.child = child1;
			parents.add(parent);
		}

		// this group doesn't exists in database
		Child child2 = new Child();
		child2.id = 2;

		for (int i = 0; i < 5; i++) {
			Parent parent = new Parent();
			parent.child = child2;
			parents.add(parent);
		}

		for (Parent parent : parents) {
			assertTrue(parentDao.createOrUpdate(parent).isCreated());
		}

		assertEquals(0, childDao.countOf());
		assertEquals(parents.size(), parentDao.countOf());
	}

	/* ====================================================== */

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
		@DatabaseField(generatedId = true)
		public int id;

		@DatabaseField(columnName = "foreignId", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true,
				foreignColumnName = "api_id", canBeNull = false)
		public Child child;
	}

	public static class Child {
		@DatabaseField(generatedId = true)
		public int id;

		@DatabaseField
		public int api_id;
	}
}
