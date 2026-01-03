package fr.azures04.mods.multiauth.exceptions;

import com.mojang.authlib.exceptions.AuthenticationException;

public class MissingTexturePropertiesException extends AuthenticationException {

	private static final long serialVersionUID = 5057194793527053557L;

	public MissingTexturePropertiesException() {
    }

    public MissingTexturePropertiesException(final String message) {
        super(message);
    }

    public MissingTexturePropertiesException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MissingTexturePropertiesException(final Throwable cause) {
        super(cause);
    }
}
