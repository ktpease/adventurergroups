package ktpweb.adventurergroups.exception;

public class UserAccountServiceException extends Exception
{
    private Codes code;

    public Codes getCode()
    {
        return code;
    }

    public UserAccountServiceException(final Codes code, final String message,
        final Throwable cause)
    {
        super(message, cause);
        this.code = code;
    }

    public UserAccountServiceException(final Codes code, final String message)
    {
        super(message);
        this.code = code;
    }

    public UserAccountServiceException(final Codes code, final Throwable cause)
    {
        super(cause);
        this.code = code;
    }

    public static enum Codes
    {
        DATABASE_ERROR,
        NULL_ACCOUNT_OBJECT,
        ACCOUNT_EXISTS,
        INVALID_USERNAME,
        INVALID_PASSWORD,
        INVALID_CHARACTER_OBJECT,
        INVALID_INSTANCE_OBJECT
    }
}