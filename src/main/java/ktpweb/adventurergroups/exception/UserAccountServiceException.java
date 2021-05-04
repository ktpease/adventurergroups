package ktpweb.adventurergroups.exception;

public class UserAccountServiceException extends Exception
{
    private Codes code;

    public Codes getCode()
    {
        return code;
    }

    public UserAccountServiceException(final String message,
        final Throwable cause, final Codes code)
    {
        super(message, cause);
        this.code = code;
    }

    public UserAccountServiceException(final String message, final Codes code)
    {
        super(message);
        this.code = code;
    }

    public UserAccountServiceException(final Throwable cause, final Codes code)
    {
        super(cause);
        this.code = code;
    }

    public static enum Codes
    {
        ACCOUNT_EXISTS,
        BAD_USERNAME,
        BAD_PASSWORD
    }
}