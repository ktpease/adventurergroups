package ktpweb.adventurergroups.exception;

public class CharacterServiceException extends Exception
{
    private Codes code;

    public Codes getCode()
    {
        return code;
    }

    public CharacterServiceException(final Codes code, final String message,
        final Throwable cause)
    {
        super(message, cause);
        this.code = code;
    }

    public CharacterServiceException(final Codes code, final String message)
    {
        super(message);
        this.code = code;
    }

    public CharacterServiceException(final Codes code, final Throwable cause)
    {
        super(cause);
        this.code = code;
    }

    public static enum Codes
    {
        DATABASE_ERROR_READ,
        DATABASE_ERROR_WRITE,
        NULL_CHARACTER_OBJECT
    }
}