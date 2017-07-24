package teamthree.twodo.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import teamthree.twodo.commons.core.Messages;
import teamthree.twodo.commons.core.index.Index;
import teamthree.twodo.commons.core.options.Alarm;
import teamthree.twodo.commons.core.options.AutoMark;
import teamthree.twodo.commons.exceptions.IllegalValueException;
import teamthree.twodo.commons.util.StringUtil;
import teamthree.twodo.model.tag.Tag;
import teamthree.twodo.model.task.Deadline;
import teamthree.twodo.model.task.Description;
import teamthree.twodo.model.task.Name;

/**
 * Contains utility methods used for parsing strings in the various *Parser
 * classes
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index is not a non-zero unsigned integer.";
    public static final String MESSAGE_INSUFFICIENT_PARTS = "Number of parts must be more than 1.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it.
     * Leading and trailing white spaces will be trimmed.
     *
     * @throws IllegalValueException
     *             if the specified index is invalid (not non-zero unsigned
     *             integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws IllegalValueException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new IllegalValueException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * All Parse Methods below @return non-empty Optional objects
     */

    /**
     * Parses a {@code Optional<String> name} into an {@code Optional<Name>} if
     * {@code name} is present.
     */
    public static Optional<Name> parseName(Optional<String> name) throws IllegalValueException {
        requireNonNull(name);
        return name.isPresent() ? Optional.of(new Name(name.get())) : Optional.empty();
    }

    /**
     * Parses a {@code Optional<String> phone} into an {@code Optional
     * <Deadline>} if {@code phone} is present.
     */
    public static Optional<Deadline> parseDeadlineForAdd(Optional<String> startTime, Optional<String> endTime,
            Optional<String> notificationPeriod) throws IllegalValueException {
        requireNonNull(startTime);
        requireNonNull(endTime);
        requireNonNull(notificationPeriod);
        // This method should only be called when end time is specified by user
        assert (endTime.isPresent());
        String end = endTime.get();
        String start = startTime.isPresent() ? startTime.get() : endTime.get();
        String notification = notificationPeriod.isPresent() ? notificationPeriod.get() : Deadline.NULL_VALUE;
        return correctUserInputAndGetDeadline(start, end, notification);
    }

    /**
     * Parses {@code Optional<String> startTime, endTime and notificationPeriod}
     * into {@code Optional
     * <Deadline>} if they are present is present.
     */
    public static Optional<Deadline> parseDeadlineForEdit(Optional<String> startTime, Optional<String> endTime,
            Optional<String> notificationPeriod) throws IllegalValueException {
        requireNonNull(startTime);
        requireNonNull(endTime);
        requireNonNull(notificationPeriod);
        //short circuit if all are empty
        if (allNotPresent(startTime, endTime, notificationPeriod)) {
            return Optional.empty();
        }
        String start = startTime.isPresent() ? startTime.get() : Deadline.NULL_VALUE;
        String end = endTime.isPresent() ? endTime.get() : Deadline.NULL_VALUE;
        String notification = notificationPeriod.isPresent() ? notificationPeriod.get() : Deadline.NULL_VALUE;

        return correctUserInputAndGetDeadline(start, end, notification);
    }

    /**
     * Sends user input for autocorrect operations and returns a deadline using
     * the autocorrected Strings if the operations do not return empty.
     * Otherwise, returns deadline using user's raw input
     *
     * @param start
     * @param end
     * @param notification
     * @return Optional containing deadline. Will be empty if value is illegal.
     * @throws IllegalValueException
     */

    private static Optional<Deadline> correctUserInputAndGetDeadline(String start, String end, String notification)
            throws IllegalValueException {
        Optional<String> temp;
        String correctedStart = (temp = parseAndCorrectDayFromUserDeadline(start)).isPresent() ? temp.get() : start;
        String correctedEnd = (temp = parseAndCorrectDayFromUserDeadline(end)).isPresent() ? temp.get() : end;
        return Optional.of(new Deadline(correctedStart, correctedEnd, notification));
    }

    //@@author A0107433N
    /**
     * Parses {@code Optional<String> startTime and endTime} into
     * {@code Optional <Deadline>} if they are present.
     */
    public static Optional<Deadline> parseDeadlineForList(Optional<String> startTime, Optional<String> endTime)
            throws IllegalValueException {
        requireNonNull(startTime);
        requireNonNull(endTime);
        //short circuit if all are empty
        if (allNotPresent(startTime, endTime)) {
            return Optional.empty();
        }
        String start = startTime.isPresent() ? startTime.get() : Deadline.NULL_VALUE;
        String end = endTime.isPresent() ? endTime.get() : Deadline.NULL_VALUE;
        return Optional.of(new Deadline(start, end, Deadline.NULL_VALUE));
    }

    /**
     *
     * Returns true if all the Optionals are empty
     *
     */
    @SafeVarargs
    private static boolean allNotPresent(Optional<String>... times) {
        for (Optional<String> time : times) {
            if (time.isPresent()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses a {@code Optional<String> address} into an {@code Optional
     * <Description>} if {@code address} is present.
     */
    public static Optional<Description> parseDescription(Optional<String> description) throws IllegalValueException {
        requireNonNull(description);
        return description.isPresent() ? Optional.of(new Description(description.get())) : Optional.empty();
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws IllegalValueException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(new Tag(tagName));
        }
        return tagSet;
    }

    /**
     * Attepmts to autocorrect the user input. If it fails to find a close
     * match, it will return an empty Optional.
     *
     * @param dateTime
     * @return Optional containing autocorrected dateTime
     */
    public static Optional<String> parseAndCorrectDayFromUserDeadline(String dateTime) {
        Matcher matcher = Pattern.compile(Deadline.DAY_PARSE_REGEX).matcher(dateTime.trim());
        if (matcher.find()) {
            String[] dayAndPrefix = matcher.group().split(" ");
            String day = extractDay(dayAndPrefix);
            Optional<String> autoCorrectedDay = StringUtil.getAutoCorrectedDay(day);
            if (day.length() > Deadline.MIN_WORD_LENGTH_FOR_DAY && autoCorrectedDay.isPresent()) {
                return Optional.of(getIntegratedDateString(matcher, autoCorrectedDay, dayAndPrefix));
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    /**
     * Creates the full date string after autocorrection.
     * @param matcher
     * @param autoCorrectedDay
     * @param dayAndPrefix
     * @return String autocorrected date string
     */
    private static String getIntegratedDateString(Matcher matcher, Optional<String> autoCorrectedDay,
            String[] dayAndPrefix) {
        StringBuilder builder = new StringBuilder("");
        for (int i = 0; i < dayAndPrefix.length; i++) {
            if (i == dayAndPrefix.length - 1) {
                builder.append(matcher.replaceFirst(autoCorrectedDay.get() + " "));
            } else {
                builder.append(dayAndPrefix[i] + " ");
            }
        }
        return builder.toString();
    }

    /**
     * Returns the extracted day from user input for deadline
     *
     * @param dayAndPrefix
     *            containing the split String for deadline
     * @return the day part of the string e.g.thursday
     *
     *         Assumes the user enters future tense as next thursday instead of
     *         thursday next
     */
    private static String extractDay(String[] dayAndPrefix) {
        return dayAndPrefix.length > 1 ? dayAndPrefix[dayAndPrefix.length - 1] : dayAndPrefix[0];
    }

    //@@author A0139267W
    /**
     * Parses a {@code Optional<String> alarm} into an {@code Optional
     * <Alarm>} if {@code alarm} is present.
     */
    public static Optional<Alarm> parseAlarm(Optional<String> alarm) throws IllegalValueException {
        requireNonNull(alarm);
        return alarm.isPresent() ? Optional.of(new Alarm(alarm.get().toLowerCase())) : Optional.empty();
    }

    /**
     * Parses a {@code Optional<String> autoMark} into an {@code Optional
     * <AutoMark>} if {@code autoMark} is present.
     */
    public static Optional<AutoMark> parseAutoMark(Optional<String> autoMark) throws IllegalValueException {
        requireNonNull(autoMark);
        if (autoMark.isPresent()) {
            String inputToLowerCase = autoMark.get().toLowerCase();
            if (!inputToLowerCase.equals("false") && !inputToLowerCase.equals("true")) {
                throw new IllegalValueException(Messages.MESSAGE_INVALID_AUTOMARK_ARGUMENT);
            }
        }
        return autoMark.isPresent() ? Optional.of(new AutoMark(Boolean.parseBoolean(autoMark.get())))
                : Optional.empty();
    }

}
