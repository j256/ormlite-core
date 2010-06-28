package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class StatementExecutorTest extends BaseOrmLiteTest {

	@Test(expected = SQLException.class)
	public void testUpdateIdNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		noIdDao.updateId(noId, "something else");
	}

	@Test(expected = SQLException.class)
	public void testRefreshNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		noIdDao.refresh(noId);
	}

	@Test(expected = SQLException.class)
	public void testDeleteNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		noIdDao.delete(noId);
	}

	@Test(expected = SQLException.class)
	public void testDeleteObjectsNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		ArrayList<NoId> noIdList = new ArrayList<NoId>();
		noIdList.add(noId);
		noIdDao.delete(noIdList);
	}

	@Test(expected = SQLException.class)
	public void testDeleteIdsNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		ArrayList<Object> noIdList = new ArrayList<Object>();
		noIdList.add(noId);
		noIdDao.deleteIds(noIdList);
	}

	protected static class NoId {
		@DatabaseField
		String stuff;
	}
}
