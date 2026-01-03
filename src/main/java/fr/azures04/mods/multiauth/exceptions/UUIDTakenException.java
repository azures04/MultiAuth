package fr.azures04.mods.multiauth.exceptions;

import com.mojang.authlib.exceptions.AuthenticationException;

public class UUIDTakenException extends AuthenticationException {
	private static final long serialVersionUID = -6244837135191140655L;

	public UUIDTakenException() {
    }

    public UUIDTakenException(final String message) {
        super(message);
    }

    public UUIDTakenException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UUIDTakenException(final Throwable cause) {
        super(cause);
    }
}
