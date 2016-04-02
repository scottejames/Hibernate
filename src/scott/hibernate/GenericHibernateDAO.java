package scott.hibernate;


import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;

public abstract class GenericHibernateDAO<T, ID extends Serializable>
{
        protected static Logger _logger = Logger
            .getLogger(GenericHibernateDAO.class);
    private Class<T> persistentClass;

    private HibernateTransaction  _tx = null;


    
    @SuppressWarnings("unchecked")
    public GenericHibernateDAO(HibernateTransaction transaction)
    {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
   
            _tx = transaction;
    }

    protected Session getSession() throws HibernateException
    {
        if (_tx == null)
        {
            _logger.error("Trying to get session on DAO ( " + this + ") but transaction  is NULL");

            throw new HibernateException("Transaction is null,  warning!");

        }
        if (_tx.getSession().isOpen() == false)
        {
            
            _logger.error("Trying to get session on DAO ( " + this + ") but session is closed");
            throw new HibernateException("Session  is closed,  warning!");

        }
        return _tx.getSession();
    }

    public Class<T> getPersistentClass()
    {
        return persistentClass;
    }

    @SuppressWarnings("unchecked")
    public T findById(ID id, boolean lock)
    {
        T entity;
        if (lock)
            entity = (T) getSession().get(getPersistentClass(), id,
                    LockMode.UPGRADE);
        else
            entity = (T) getSession().get(getPersistentClass(), id);

        return entity;
    }

    public List<T> findAll()
    {
        return findByCriteria();
    }

    @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String[] excludeProperty)
    {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example = Example.create(exampleInstance);

        if (excludeProperty != null)
        {
            for (String exclude : excludeProperty)
            {
                example.excludeProperty(exclude);
            }

        }
        crit.add(example);

        return crit.list();
    }

    public List<T> findByExample(T exampleInstance)
    {
        return this.findByExample(exampleInstance, null);
    }

    public T makePersistent(T entity)
    {
        getSession().saveOrUpdate(entity);
        return entity;
    }

    public List<T> makePersistent(List<T> entity)
    {
        for (T element : entity)
        {
            makePersistent(element);
        }

        return entity;
    }

    public void makeTransient(T entity)
    {
        getSession().delete(entity);
    }

    public void makeTransient(List<T> entity)
    {
        for (T element : entity)
        {
            getSession().delete(element);
        }

    }

    public void flush()
    {
        getSession().flush();
    }

    public void clear()
    {
        getSession().clear();
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion)
    {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion)
        {
            crit.add(c);
        }
        return crit.list();
    }

  

}