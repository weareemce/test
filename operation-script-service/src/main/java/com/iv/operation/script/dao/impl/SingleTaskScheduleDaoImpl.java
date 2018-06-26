package com.iv.operation.script.dao.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.iv.common.dto.ObjectPageDto;
import com.iv.jpa.util.hibernate.HibernateCallBack;
import com.iv.jpa.util.hibernate.HibernateTemplate;
import com.iv.operation.script.dto.ScheduleQueryDto;
import com.iv.operation.script.entity.SingleTaskScheduleEntity;

@Repository
public class SingleTaskScheduleDaoImpl implements ISingleTaskScheduleDao {

	@Override
	public SingleTaskScheduleEntity save(SingleTaskScheduleEntity entity) throws RuntimeException {
		return (SingleTaskScheduleEntity) HibernateTemplate.execute(new HibernateCallBack() {

			@Override
			public Object doInHibernate(Session ses) throws HibernateException {
				ses.saveOrUpdate(entity);
				return entity;
			}
		});
	}

	@Override
	public SingleTaskScheduleEntity selectById(int id) throws RuntimeException {
		return (SingleTaskScheduleEntity) HibernateTemplate.execute(new HibernateCallBack() {

			@Override
			public Object doInHibernate(Session ses) throws HibernateException {
				return ses.get(SingleTaskScheduleEntity.class, id);
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SingleTaskScheduleEntity> selectByTaskId(int taskId) throws RuntimeException {
		return (List<SingleTaskScheduleEntity>) HibernateTemplate.execute(new HibernateCallBack() {

			@Override
			public Object doInHibernate(Session ses) throws HibernateException {
				return ses.createQuery("from SingleTaskScheduleEntity s where s.singleTask.id=?")
						.setParameter(0, taskId).list();
			}
		});
	}

	@Override
	public void delById(int id) throws RuntimeException {
		HibernateTemplate.execute(new HibernateCallBack() {

			@Override
			public Object doInHibernate(Session ses) throws HibernateException {
				ses.delete(ses.load(SingleTaskScheduleEntity.class, id));
				return null;
			}
		});

	}

	@Override
	public int delByTaskId(int taskId) throws RuntimeException {

		return (int) HibernateTemplate.execute(new HibernateCallBack() {

			@Override
			public Object doInHibernate(Session ses) throws HibernateException {
				return ses.createQuery("delete from SingleTaskScheduleEntity s where s.singleTask.id=?")
						.setParameter(0, taskId).executeUpdate();
			}
		});
	}

	@Override
	public ObjectPageDto selectPage(ScheduleQueryDto queryDto) throws RuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

}
