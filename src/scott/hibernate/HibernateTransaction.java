package scott.hibernate;


import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import scott.hibernate.HibernateSessionFactory.STRATEGY;
import scott.hibernate.exception.TransactionException;

public class HibernateTransaction implements TransactionStrategy
{
    protected static Logger _logger = Logger
            .getLogger(HibernateTransaction.class);
    private Transaction _tx = null;
    private Session _sess = null;
    private STRATEGY _strategy = null;

    public HibernateTransaction()
    {
        _sess = HibernateSessionFactory.getInstance().getSession();
    }

    public HibernateTransaction(STRATEGY strategy)
    {
        this(strategy, null);
    }

    public HibernateTransaction(STRATEGY strategy, Object o)
    {
        this._strategy = strategy;

        if (strategy == STRATEGY.BY_OBJECT)
        {
            _sess = HibernateSessionFactory.getInstance(strategy).getSession(o);
        } else
        {
            _sess = HibernateSessionFactory.getInstance(strategy).getSession();
        }

    }

    public HibernateTransaction(HibernateSessionFactory factory)
    {
        _sess = factory.getSession();
    }

    public HibernateTransaction(Session session)
    {
        _sess = session;
    }

    public void beginTransaction() throws TransactionException
    {
        _logger.info("[beginTransaction] begin TX");
        if (_tx != null)
        {
            if (_tx.isActive() == true)
            {
                throw new TransactionException(
                        "trying to restart a running transaction");
            } else
            {
                // if the previous TX is null give it up
                _tx = null;
            }
        }
        if (_tx == null)
        {
            _logger.info("[beginTransaction] creating new transaction");
            _tx = _sess.beginTransaction();
        }
        _tx.begin();
    }

    public void commitTransaction() throws TransactionException
    {
        if (_tx == null)
        {
            throw new TransactionException(
                    "trying to commit  a null transaction");
        }
        _logger.info("[commitTransaction] end transaction");
        _tx.commit();
    }

    public void dispose()
    {
        if (_sess != null)
        {
            if (_sess.isOpen())
            {
                if (this._strategy == STRATEGY.NO_CONTROL)
                {
                    _sess.close();
                }
                _sess = null;
                _tx = null;
            }
        }
    }

    public boolean isActiveTransaction()
    {
        if (_tx == null)
            return false;
        return _tx.isActive();
    }

    public boolean isSessionOpen()
    {
        if (_sess == null)
            return false;
        return _sess.isOpen();
    }

    public void rollbackTransaction() throws TransactionException
    {
        if (_tx == null)
        {
            throw new TransactionException(
                    "trying to rollbackl a closed transaction");
        }
        _logger.info("[rollbackTransaction] rollback transaction");
        _tx.rollback();
    }

    public boolean wasRolledBack()
    {
        return _tx.wasRolledBack();
    }

    public boolean wasCommitted()
    {
        return _tx.wasCommitted();
    }

    protected Transaction getTransaction()
    {
        return _tx;
    }

    protected Session getSession()
    {
        return _sess;
    }
}
