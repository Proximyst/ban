package com.proximyst.ban.commands.helper.exception;

import com.proximyst.ban.commands.helper.BaseCommand;

/**
 * A swallowed exception indicating to the {@link BaseCommand} that the usage should be sent back to the source.
 */
public final class UsageException extends IllegalCommandException {
}
