package scott.hibernate.exception;

public class TransactionException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3101528779028646346L;

	public TransactionException(String msg)
    {
        super(msg);
    }
}

