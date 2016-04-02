package scott.hibernate;


import java.util.List;
import scott.hibernate.HibernateSessionFactory.STRATEGY;
import scott.hibernate.exception.TransactionException;
import scott.mvc.BaseView;
import scott.mvc.IReciever;
import scott.mvc.View;
import scott.mvc.ViewEvent;
import scott.mvc.BaseView.VIEW_EVENT;

public class ViewBoundTransaction extends HibernateTransaction implements
        IReciever<ViewEvent>
{

    protected View _view = null;
    public ViewBoundTransaction(BaseView view)
    {
        super(STRATEGY.BY_OBJECT);
        view.attachReciever(this);
    }

    public void received(List<ViewEvent> events)
    {
        try
        {
            _logger.info("[recieved] recieved event list size: " + events.size());
            for (ViewEvent event : events)
            {
                _logger.info("[recieved] processing event: " + event);
                if (event.event instanceof VIEW_EVENT)
                {
                    switch ((VIEW_EVENT) event.event)
                    {
                    case OPENED:
                        beginTransaction();
                        break;
                    case CLOSED:
                        if (isActiveTransaction()==true)
                            rollbackTransaction();
                        dispose();

                        break;
                    case OK_SELECTED:
                        commitTransaction();
                        break;
                    case CANCEL_SELECTED:
                        if (isActiveTransaction()==true)
                            rollbackTransaction();
                        break;
    
                    default:
                        break;
                    }
                }
            }
        }
        catch (TransactionException e)
        {
            _logger.error(e.toString());
        }
    }

}
