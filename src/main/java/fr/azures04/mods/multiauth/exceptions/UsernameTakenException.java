package fr.azures04.mods.multiauth.exceptions;

import com.mojang.authlib.exceptions.AuthenticationException;

public class UsernameTakenException extends AuthenticationException {
	private static final long serialVersionUID = -7817093099928177712L;

	public UsernameTakenException() {
    }

    public UsernameTakenException(final String message) {
        super(message);
    }

    public UsernameTakenException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UsernameTakenException(final Throwable cause) {
        super(cause);
    }
}
