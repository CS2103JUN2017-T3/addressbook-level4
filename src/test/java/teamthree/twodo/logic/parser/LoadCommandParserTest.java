package teamthree.twodo.logic.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import teamthree.twodo.logic.commands.LoadCommand;

//@@author A0162253M
public class LoadCommandParserTest {

    private static final String VALID_FILENAME = "Desktop\taskbook\2Do.xml";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private LoadCommandParser parser = new LoadCommandParser();

    @Test
    public void parseValidArgsReturnsLoadCommand() throws Exception {
        LoadCommand command = parser.parse(VALID_FILENAME);
        assertEquals(VALID_FILENAME, command.filePath);
    }
}
