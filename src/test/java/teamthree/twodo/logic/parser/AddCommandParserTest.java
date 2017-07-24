package teamthree.twodo.logic.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static teamthree.twodo.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_DEADLINE_END;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_DEADLINE_START;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_NAME;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_TAG;
import static teamthree.twodo.testutil.EditCommandTestUtil.VALID_DESCRIPTION_EVENT;
import static teamthree.twodo.testutil.EditCommandTestUtil.VALID_DESCRIPTION_MOD;
import static teamthree.twodo.testutil.EditCommandTestUtil.VALID_END_DATE;
import static teamthree.twodo.testutil.EditCommandTestUtil.VALID_NAME_CSMOD;
import static teamthree.twodo.testutil.EditCommandTestUtil.VALID_NAME_EVENT;
import static teamthree.twodo.testutil.EditCommandTestUtil.VALID_START_DATE;
import static teamthree.twodo.testutil.EditCommandTestUtil.VALID_TAG_WORK;

import org.junit.Test;

import teamthree.twodo.logic.commands.AddCommand;
import teamthree.twodo.logic.commands.Command;
import teamthree.twodo.logic.parser.exceptions.ParseException;
import teamthree.twodo.model.task.Task;
import teamthree.twodo.testutil.FloatingTaskBuilder;
import teamthree.twodo.testutil.TaskWithDeadlineBuilder;

//@@author A0124399W-reused
public class AddCommandParserTest {
    private static final String NAME_DESC_MOD = PREFIX_NAME + VALID_NAME_CSMOD;
    private static final String NAME_DESC_EVENT = PREFIX_NAME + VALID_NAME_EVENT;
    private static final String DEADLINE_DESC_MOD = " " + PREFIX_DEADLINE_END + VALID_END_DATE;
    private static final String DEADLINE_DESC_EVENT = " " + PREFIX_DEADLINE_START + VALID_START_DATE + " "
            + PREFIX_DEADLINE_END + VALID_END_DATE;
    private static final String DESC_MOD = " " + PREFIX_DESCRIPTION + VALID_DESCRIPTION_MOD;
    private static final String DESC_EVENT = " " + PREFIX_DESCRIPTION + VALID_DESCRIPTION_EVENT;
    private static final String TAG_DESC_WORK = " " + PREFIX_TAG + VALID_TAG_WORK;
    private static final String MESSAGE_INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            AddCommand.MESSAGE_USAGE);

    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parseMissingPartsFailure() {
        // no prefix
        assertParseFailure(VALID_NAME_CSMOD, MESSAGE_INVALID_FORMAT);

        // no end date
        assertParseFailure(NAME_DESC_MOD + " " + PREFIX_DEADLINE_START + VALID_START_DATE, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parseEventSuccess() throws Exception {
        String userInput = " " + NAME_DESC_EVENT + DEADLINE_DESC_EVENT + TAG_DESC_WORK + DESC_EVENT;
        Task expected = new TaskWithDeadlineBuilder().withName(VALID_NAME_EVENT)
                .withEventDeadline(VALID_START_DATE, VALID_END_DATE).withDescription(VALID_DESCRIPTION_EVENT)
                .withTags(VALID_TAG_WORK).build();
        AddCommand expectedCommand = new AddCommand(expected);
        assertParseSuccess(userInput, expectedCommand);
    }

    @Test
    public void parseFloatSuccess() throws Exception {
        String userInput = " " + NAME_DESC_MOD + TAG_DESC_WORK + DESC_MOD;
        Task expected = new FloatingTaskBuilder().withName(VALID_NAME_CSMOD).withDescription(VALID_DESCRIPTION_MOD)
                .withTags(VALID_TAG_WORK).build();
        AddCommand expectedCommand = new AddCommand(expected);
        assertParseSuccess(userInput, expectedCommand);
    }

    @Test
    public void parseDeadlineSuccess() throws Exception {
        String userInput = " " + NAME_DESC_MOD + DEADLINE_DESC_MOD + TAG_DESC_WORK + DESC_MOD;
        Task expected = new TaskWithDeadlineBuilder().withName(VALID_NAME_CSMOD).withDeadline(VALID_END_DATE)
                .withDescription(VALID_DESCRIPTION_MOD).withTags(VALID_TAG_WORK).build();
        AddCommand expectedCommand = new AddCommand(expected);
        assertParseSuccess(userInput, expectedCommand);
    }

    /**
     * Asserts the parsing of {@code userInput} is unsuccessful and the error
     * message equals to {@code expectedMessage}
     */
    private void assertParseFailure(String userInput, String expectedMessage) {
        try {
            parser.parse(userInput);
            fail("An exception should have been thrown.");
        } catch (ParseException pe) {
            assertEquals(expectedMessage, pe.getMessage());
        }

    }

    /**
     * Asserts the parsing of {@code userInput} is successful and the result
     * matches {@code expectedCommand}
     */
    private void assertParseSuccess(String userInput, AddCommand expectedCommand) throws Exception {
        Command command = parser.parse(userInput);
        assertTrue(expectedCommand.equals(command));
    }
}
