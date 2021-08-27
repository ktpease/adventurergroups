package ktpweb.adventurergroups.exception;

public class AdminAccountServiceException extends Exception
{
    private Codes code;

    public Codes getCode()
    {
        return code;
    }

    public AdminAccountServiceException(final Codes code, final String message,
        final Throwable cause)
    {
        super(message, cause);
        this.code = code;
    }

    public AdminAccountServiceException(final Codes code, final String message)
    {
        super(message);
        this.code = code;
    }

    public AdminAccountServiceException(final Codes code, final Throwable cause)
    {
        super(cause);
        this.code = code;
    }

    public static enum Codes
    {
        DATABASE_ERROR_READ,
        DATABASE_ERROR_WRITE,
        DATABASE_ERROR_READ_MAPPING,
        NULL_ACCOUNT_OBJECT,
        ACCOUNT_NOT_FOUND,
        ACCOUNT_ALREADY_EXISTS,
        INVALID_USERNAME,
        INVALID_PASSWORD
    }
}