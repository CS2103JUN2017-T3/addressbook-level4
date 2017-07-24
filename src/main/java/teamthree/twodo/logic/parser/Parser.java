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

//@@author A0107433N
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
        CommandParser parser;

        switch (commandWord) {
        case ClearCommand.COMMAND_WORD:
            return new ClearCommand();

        case UndoCommand.COMMAND_WORD:
        case UndoCommand.COMMAND_WORD_FAST:
            return new UndoCommand();

        case RedoCommand.COMMAND_WORD:
        case RedoCommand.COMMAND_WORD_FAST:
            return new RedoCommand();

        case HistoryCommand.COMMAND_WORD:
            return new HistoryCommand();

        case ExitCommand.COMMAND_WORD:
        case ExitCommand.COMMAND_WORD_SECOND:
        case ExitCommand.COMMAND_WORD_FAST:
        case ExitCommand.COMMAND_WORD_FAST_SECOND:
            return new ExitCommand();

        case HelpCommand.COMMAND_WORD:
            if (arguments.isEmpty()) {
                return new HelpCommand();
            }
            parser = new HelpCommandParser();
            break;

        case AddCommand.COMMAND_WORD:
        case AddCommand.COMMAND_WORD_QUICK:
        case AddCommand.COMMAND_WORD_FAST:
            parser = new AddCommandParser();
            break;

        case EditCommand.COMMAND_WORD:
        case EditCommand.COMMAND_WORD_FAST:
            parser = new EditCommandParser();
            break;

        case DeleteCommand.COMMAND_WORD_QUICK:
        case DeleteCommand.COMMAND_WORD_FAST:
        case DeleteCommand.COMMAND_WORD_SHORT:
        case DeleteCommand.COMMAND_WORD:
            parser = new DeleteCommandParser();
            break;

        case MarkCommand.COMMAND_WORD:
        case MarkCommand.COMMAND_WORD_FAST:
            parser = new MarkCommandParser();
            break;

        case UnmarkCommand.COMMAND_WORD:
        case UnmarkCommand.COMMAND_WORD_FAST:
            parser = new UnmarkCommandParser();
            break;

        case FindCommand.COMMAND_WORD:
        case FindCommand.COMMAND_WORD_FAST:
            parser = new FindCommandParser();
            break;

        case ListCommand.COMMAND_WORD:
        case ListCommand.COMMAND_WORD_FAST:
            parser = new ListCommandParser();
            break;

        case SaveCommand.COMMAND_WORD:
        case SaveCommand.COMMAND_WORD_FAST:
            parser = new SaveCommandParser();
            break;

        case LoadCommand.COMMAND_WORD:
            parser = new LoadCommandParser();
            break;

        case OptionsCommand.COMMAND_WORD:
        case OptionsCommand.COMMAND_WORD_SECOND:
        case OptionsCommand.COMMAND_WORD_FAST:
            parser = new OptionsCommandParser();
            break;

        default:
            throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
        }
        return parser.parse(arguments);
    }
}
