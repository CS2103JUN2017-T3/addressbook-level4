package teamthree.twodo.logic.parser;

import static teamthree.twodo.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_DEADLINE_END;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_DEADLINE_START;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_NAME;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_NOTIFICATION_PERIOD;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;

import teamthree.twodo.commons.core.index.Index;
import teamthree.twodo.commons.exceptions.IllegalValueException;
import teamthree.twodo.logic.commands.AddCommand;
import teamthree.twodo.logic.commands.exceptions.CommandException;
import teamthree.twodo.logic.parser.exceptions.ParseException;
import teamthree.twodo.model.tag.Tag;
import teamthree.twodo.model.task.Deadline;
import teamthree.twodo.model.task.Description;
import teamthree.twodo.model.task.Name;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.Task;
import teamthree.twodo.model.task.TaskWithDeadline;
//@@author A0124399W
/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser {
    public static final int NUM_ARGS_FOR_CATEGORY_OP = 3;

    /**
     * Parses the given {@code String} of arguments in the context of the
     * AddCommand and returns an AddCommand object for execution.
     *
     * @throws ParseException
     *             if the user input does not conform the expected format
     * @throws CommandException
     */
    public AddCommand parse(String args) throws ParseException {

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_DEADLINE_START,
                PREFIX_DEADLINE_END, PREFIX_NOTIFICATION_PERIOD, PREFIX_DESCRIPTION, PREFIX_TAG);
        if (isCategoryOperation(args)) {
            return prepareCategoryAddCommand(args);
        }
        checkForValidityOfCommand(argMultimap);
        try {
            Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME)).get();
            Description description = ParserUtil.parseDescription(argMultimap.getValue(PREFIX_DESCRIPTION))
                    .orElse(new Description("No Description."));
            Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
            if (argumentContainsDeadline(argMultimap)) {
                Deadline deadline = ParserUtil.parseDeadlineForAdd(argMultimap.getValue(PREFIX_DEADLINE_START),
                        argMultimap.getValue(PREFIX_DEADLINE_END), argMultimap.getValue(PREFIX_NOTIFICATION_PERIOD))
                        .get();
                ReadOnlyTask task = new TaskWithDeadline(name, deadline, description, tagList);
                return new AddCommand(task);
            }
            ReadOnlyTask task = new Task(name, description, tagList);
            return new AddCommand(task);

        } catch (IllegalValueException ive) {
            throw new ParseException(ive.getMessage(), ive);
        }
    }
    //Catch invalid command arguments
    private void checkForValidityOfCommand(ArgumentMultimap argMultimap) throws ParseException {
        if (!arePrefixesPresent(argMultimap, PREFIX_NAME)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        } else if (invalidDeadlineDeclaration(argMultimap)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
    }
    /**
     * Returns AddCommand for adding category.
     */
    private AddCommand prepareCategoryAddCommand(String args) throws ParseException {
        String[] splitArgs = args.trim().split(" ");
        String tagName = splitArgs[1].trim();
        String[] indicesAsString = splitArgs[2].trim().split(",");
        ArrayList<Index> indices = new ArrayList<>();
        for (String idx : indicesAsString) {
            try {
                indices.add(ParserUtil.parseIndex(idx.trim()));
            } catch (IllegalValueException ive) {
                throw new ParseException(ive.getMessage(), ive);
            }
        }
        return new AddCommand(tagName, indices);
    }
    //Returns true if arguments category operation prefix
    private boolean isCategoryOperation(String args) {
        String[] splitArgs = args.trim().split(" ");
        if (splitArgs.length != NUM_ARGS_FOR_CATEGORY_OP) {
            return false;
        }
        for (int i = 0; i < splitArgs.length; i++) {
            if (splitArgs[i].trim().equals(PREFIX_CATEGORY.toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean argumentContainsDeadline(ArgumentMultimap argMultimap) {
        return argMultimap.getValue(PREFIX_DEADLINE_END).isPresent();
    }

    /**
     *
     * @param argMultimap
     * @return if start time or notification period are specified without the
     *         end time being specified
     */
    private boolean invalidDeadlineDeclaration(ArgumentMultimap argMultimap) {
        return (arePrefixesPresent(argMultimap, PREFIX_DEADLINE_START)
                || arePrefixesPresent(argMultimap, PREFIX_NOTIFICATION_PERIOD))
                && !arePrefixesPresent(argMultimap, PREFIX_DEADLINE_END);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional}
     * values in the given {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
