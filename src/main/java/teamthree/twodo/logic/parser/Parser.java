package teamthree.twodo.logic.parser;

import static teamthree.twodo.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static teamthree.twodo.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import teamthree.twodo.logic.commands.AddCommand;
import teamthree.twodo.logic.commands.ClearCommand;
import teamthree.twodo.logic.commands.Command;
import teamthree.twodo.logic.commands.DeleteCommand;
import teamthree.twodo.logic.commands.EditCommand;
import teamthree.twodo.logic.commands.ExitCommand;
import teamthree.twodo.logic.commands.FindCommand;
import teamthree.twodo.logic.commands.HelpCommand;
import teamthree.twodo.logic.commands.HistoryCommand;
import teamthree.twodo.logic.commands.ListCommand;
import teamthree.twodo.logic.commands.LoadCommand;
import teamthree.twodo.logic.commands.MarkCommand;
import teamthree.twodo.logic.commands.OptionsCommand;
import teamthree.twodo.logic.commands.RedoCommand;
import teamthree.twodo.logic.commands.SaveCommand;
import teamthree.twodo.logic.commands.UndoCommand;
import teamthree.twodo.logic.commands.UnmarkCommand;
import teamthree.twodo.logic.commands.exceptions.CommandException;
import teamthree.twodo.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class Parser {

    /**
     * Used for initial separation of command word and args.
     */
    public static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");

    /**
     * Parses user input into command for execution.
     *
     * @param userInput
     *            full user input string
     * @return the command based on the user input
     * @throws ParseException
     *             if the user input does not conform the expected format
     * @throws CommandException
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        switch (commandWord) {

        case AddCommand.COMMAND_WORD:
        case AddCommand.COMMAND_WORD_QUICK:
        case AddCommand.COMMAND_WORD_UNIXSTYLE:
            return new AddCommandParser().parse(arguments);

        case EditCommand.COMMAND_WORD:
        case EditCommand.COMMAND_WORD_UNIXSTYLE:
            return new EditCommandParser().parse(arguments);

        case DeleteCommand.COMMAND_WORD_QUICK:
        case DeleteCommand.COMMAND_WORD_FAST:
        case DeleteCommand.COMMAND_WORD_SHORT:
        case DeleteCommand.COMMAND_WORD:
            return new DeleteCommandParser().parse(arguments);

        case MarkCommand.COMMAND_WORD:
        case MarkCommand.COMMAND_WORD_UNIXSTYLE:
            return new MarkCommandParser().parse(arguments);

        case UnmarkCommand.COMMAND_WORD:
        case UnmarkCommand.COMMAND_WORD_UNIXSTYLE:
            return new UnmarkCommandParser().parse(arguments);

        case ClearCommand.COMMAND_WORD:
            return new ClearCommand();

        case FindCommand.COMMAND_WORD:
        case FindCommand.COMMAND_WORD_UNIXSTYLE:
            return new FindCommandParser().parse(arguments);

        case ListCommand.COMMAND_WORD:
        case ListCommand.COMMAND_WORD_UNIXSTYLE:
            return new ListCommandParser().parse(arguments);

        case UndoCommand.COMMAND_WORD:
        case UndoCommand.COMMAND_WORD_UNIXSTYLE:
            return new UndoCommand();

        case RedoCommand.COMMAND_WORD:
        case RedoCommand.COMMAND_WORD_UNIXSTYLE:
            return new RedoCommand();

        case HistoryCommand.COMMAND_WORD:
            return new HistoryCommand();

        case ExitCommand.COMMAND_WORD:
        case ExitCommand.COMMAND_WORD_SECOND:
        case ExitCommand.COMMAND_WORD_UNIXSTYLE:
            return new ExitCommand();

        case SaveCommand.COMMAND_WORD:
        case SaveCommand.COMMAND_WORD_UNIXSTYLE:
            return new SaveCommandParser().parse(arguments);

        case LoadCommand.COMMAND_WORD:
            return new LoadCommandParser().parse(arguments);

        case OptionsCommand.COMMAND_WORD:
        case OptionsCommand.COMMAND_WORD_UNIXSTYLE:
            return new OptionsCommandParser().parse(arguments);

        case HelpCommand.COMMAND_WORD:
            if (arguments.isEmpty()) {
                return new HelpCommand();
            }
            return new HelpCommandParser().parse(arguments);

        default:
            throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
        }

    }
}
