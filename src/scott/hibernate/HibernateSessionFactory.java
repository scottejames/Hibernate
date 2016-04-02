package scott.hibernate;


import java.util.HashMap;
import org.apache.log4j.Logger;
import org.hibernate.Session;

public class HibernateSessionFactory
{
    protected static Logger _logger = Logger
    
            .getLogger(HibernateSessionFactory.class);
   public  enum STRATEGY
    {
         BY_OBJECT, BY_THREAD,NO_CONTROL
    };
    private STRATEGY _strategy = STRATEGY.BY_OBJECT;
    private ThreadLocal<Session> _session = null;
    private HashMap<Object,Session> _sessionHash = new HashMap<Object,Session>(); 
    boolean _recycleSessions = false;

    private static HashMap<STRATEGY,HibernateSessionFactory> _instance = new HashMap<STRATEGY,HibernateSessionFactory>();

    private HibernateSessionFactory(STRATEGY strategy)
    {
        _strategy = strategy;
    }

    public static HibernateSessionFactory getInstance()
    {
        return getInstance(STRATEGY.BY_THREAD);
        
    }
    public static HibernateSessionFactory getInstance(STRATEGY strategy)
    {
        if (_instance .get(strategy)== null)
        {
            _instance.put(strategy,new HibernateSessionFactory(strategy));
        }
        return _instance.get(strategy);
    }

    public Session getSession()
    {
        return getSession(null);
    }

    public Session getSession(Object o)
    {
        _logger
                .info("[getSession] obtaining a session context based on strategy: "
                        + _strategy);
        switch (_strategy)
        {
        case BY_OBJECT:
            return getStessionByObject(o);
        case BY_THREAD:
            return getSessionByThread();
        case NO_CONTROL:
            return HibernateUtil.getSessionFactory().openSession();
        default:
            return null;
        }
    }

    private Session recycleSessions(Session session)
    {
        // If set to true then create a new session if the old one is closed
        // This should be used with caution as it will blur the transaction boundaries
        if (_recycleSessions == true)
        {
            if (session.isOpen() == false)
            {
                _logger.info("[recycleSessions] session closed aquiring a new one...");
                return HibernateUtil.getSessionFactory().openSession();
            }
        }
        return session;
    }
    public void closeSession(STRATEGY strat)
    {
        closeSession(strat,null);
    }
    
    public void closeSession(STRATEGY strat,Object o)
    {
        getSession( o).close();
    }

    private Session getSessionByThread()
    {
        _logger.info("getSessionByThread] requesting new session by thread (" + 
                Thread.currentThread().getName() + ")");
        if (_session == null)
        {
            _session = new ThreadLocal<Session>();
            _logger.info("getSessionByThread] creating new session");
            _session.set(HibernateUtil.getSessionFactory().openSession());
        }
        _session.set(recycleSessions(_session.get()));
        
        return _session.get();
    }

    private Session getStessionByObject(Object o)
    {
        Object s = o;
        if (s == null)
            s = this;
        
        _logger.info("getSessionByThread] requesting new session by object (" + 
                s + ")");
        
        Session result = null;
        result = _sessionHash.get(s);
        
        if (result == null)
        {
            _logger.info("[getSessionByThread] creating new session");
            result = HibernateUtil.getSessionFactory().openSession();

        }
        
        _sessionHash.put(s,  recycleSessions(result));
        
        return result;
    }

    public void closeAll()
    {
        
    }
    public STRATEGY getStrategy()
    {
        return _strategy;
    }



    public boolean getRecycleSession()
    {
        return _recycleSessions;
    }

    public void setRecycleSessions(boolean r)
    {
        _recycleSessions = r;
    }
   
}
