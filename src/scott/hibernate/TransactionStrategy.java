package scott.hibernate;


import scott.hibernate.exception.TransactionException;




public interface TransactionStrategy
{
    
    public boolean isActiveTransaction();
    public void beginTransaction() throws TransactionException;
    public void commitTransaction() throws TransactionException ;
    public void rollbackTransaction() throws TransactionException ;
    public void dispose();
    
    
}
