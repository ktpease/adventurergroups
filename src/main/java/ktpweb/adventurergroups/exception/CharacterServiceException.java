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
        DATABASE_ERROR_READ_MAPPING,
        NULL_CHARACTER_OBJECT,
        NULL_CHARACTER_GROUP_OBJECT,
        CHARACTER_NOT_FOUND,
        INSTANCE_NOT_FOUND,
        INVALID_INSTANCE_OBJECT,
        INSTANCE_INACTIVE,
        CREATOR_NOT_FOUND,
        MAINTAINER_NOT_FOUND,
        INVALID_MAINTAINER_OBJECT,
        CHARACTER_GROUP_NOT_FOUND,
        INVALID_CHARACTER_GROUP_OBJECT,
    }
}