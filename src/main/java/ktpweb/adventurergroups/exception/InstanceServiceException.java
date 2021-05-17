package ktpweb.adventurergroups.exception;

public class InstanceServiceException extends Exception
{
    private Codes code;

    public Codes getCode()
    {
        return code;
    }

    public InstanceServiceException(final Codes code, final String message,
        final Throwable cause)
    {
        super(message, cause);
        this.code = code;
    }

    public InstanceServiceException(final Codes code, final String message)
    {
        super(message);
        this.code = code;
    }

    public InstanceServiceException(final Codes code, final Throwable cause)
    {
        super(cause);
        this.code = code;
    }

    public static enum Codes
    {
        DATABASE_ERROR_READ,
        DATABASE_ERROR_WRITE,
        DATABASE_ERROR_READ_MAPPING,
        NULL_INSTANCE_OBJECT,
        INSTANCE_NOT_FOUND,
        INSTANCE_ALREADY_EXISTS,
        OWNER_NOT_FOUND,
        INVALID_OWNER_OBJECT,
        INVALID_OWNER_ROLE,
        INVALID_SUBDOMAINNAME
    }
}