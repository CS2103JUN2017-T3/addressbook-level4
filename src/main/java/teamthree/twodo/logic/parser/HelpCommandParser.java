package teamthree.twodo.logic.parser;

import static teamthree.twodo.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;

import teamthree.twodo.logic.commands.AddCommand;
import teamthree.twodo.logic.commands.DeleteCommand;
import teamthree.twodo.logic.commands.EditCommand;
import teamthree.twodo.logic.commands.FindCommand;
import teamthree.twodo.logic.commands.HelpCommand;
import teamthree.twodo.logic.commands.ListCommand;
import teamthree.twodo.logic.parser.exceptions.ParseException;

//@@author A0107433N
// Parses input arguments and creates a new HelpCommand object
public class HelpCommandParser {

    public HelpCommandParser() {
    }

    public HelpCommand parse(String args) throws ParseException {
        switch (args.trim()) {
        case AddCommand.COMMAND_WORD:
        case AddCommand.COMMAND_WORD_QUICK:
        case AddCommand.COMMAND_WORD_UNIXSTYLE:
            return new HelpCommand(AddCommand.MESSAGE_USAGE);
        case EditCommand.COMMAND_WORD:
        case EditCommand.COMMAND_WORD_UNIXSTYLE:
            return new HelpCommand(EditCommand.MESSAGE_USAGE);
        case DeleteCommand.COMMAND_WORD:
        case DeleteCommand.COMMAND_WORD_QUICK:
        case DeleteCommand.COMMAND_WORD_SHORT:
        case DeleteCommand.COMMAND_WORD_FAST:
            return new HelpCommand(DeleteCommand.MESSAGE_USAGE);
        case ListCommand.COMMAND_WORD:
        case ListCommand.COMMAND_WORD_UNIXSTYLE:
            return new HelpCommand(ListCommand.MESSAGE_USAGE);
        case FindCommand.COMMAND_WORD:
        case FindCommand.COMMAND_WORD_UNIXSTYLE:
            return new HelpCommand(FindCommand.MESSAGE_USAGE);
        case FindCommand.COMMAND_WORD_HISTORY:
            return new HelpCommand(HelpCommand.MESSAGE_HISTORY_USAGE);
        default:
            throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
        }

    }

}
