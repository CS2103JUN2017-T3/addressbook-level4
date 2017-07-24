package teamthree.twodo.logic.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import teamthree.twodo.logic.commands.SaveCommand;

//@@author A0162253M
public class SaveCommandParserTest {

    private static final String VALID_FILENAME = "Documents\taskbook\2Do.xml";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SaveCommandParser parser = new SaveCommandParser();

    @Test
    public void parseValidArgsReturnsSaveCommand() throws Exception {
        SaveCommand command = parser.parse(VALID_FILENAME);
        assertEquals(VALID_FILENAME, command.filePath);
    }

}
