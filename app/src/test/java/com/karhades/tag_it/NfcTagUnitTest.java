package com.karhades.tag_it;

import com.karhades.tag_it.main.model.JsonAttributes;
import com.karhades.tag_it.main.model.NfcTag;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NfcTag unit test.
 */
@RunWith(MockitoJUnitRunner.class)
public class NfcTagUnitTest {

    private static final String TEST_TITLE = "Tag 1";
    private static final String TEST_DIFFICULTY = "Easy";
    private static final String TEST_TAG_ID = "1234";
    private static final String TEST_PICTURE_FILE_PATH = "picture_file_path";
    private static final boolean TEST_DISCOVERED = true;
    private static final String TEST_DATE_DISCOVERED = "13/11/16";

    @Mock
    private JSONObject mMockJsonObject;

    @Mock
    private NfcTag.Factory mMockFactory;

    @Test
    public void nfcTag_creates_correct() throws JSONException {
        // When
        when(mMockJsonObject.getString(JsonAttributes.TITLE)).thenReturn(TEST_TITLE);
        when(mMockJsonObject.getString(JsonAttributes.DIFFICULTY)).thenReturn(TEST_DIFFICULTY);
        when(mMockJsonObject.getString(JsonAttributes.TAG_ID)).thenReturn(TEST_TAG_ID);
        when(mMockJsonObject.getString(JsonAttributes.PICTURE_FILE_PATH)).thenReturn(TEST_PICTURE_FILE_PATH);
        when(mMockJsonObject.getBoolean(JsonAttributes.DISCOVERED)).thenReturn(TEST_DISCOVERED);
        when(mMockJsonObject.has(JsonAttributes.DATE_DISCOVERED)).thenReturn(true);
        when(mMockJsonObject.getString(JsonAttributes.DATE_DISCOVERED)).thenReturn(TEST_DATE_DISCOVERED);

        // Method call
        NfcTag nfcTag = new NfcTag(mMockJsonObject);

        // Verify
        assertThat(nfcTag.getTitle(), is(equalTo(TEST_TITLE)));
        assertThat(nfcTag.getDifficulty(), is(equalTo(TEST_DIFFICULTY)));
        assertThat(nfcTag.getTagId(), is(equalTo(TEST_TAG_ID)));
        assertThat(nfcTag.getPictureFilePath(), is(equalTo(TEST_PICTURE_FILE_PATH)));
        assertThat(nfcTag.isDiscovered(), is(equalTo(TEST_DISCOVERED)));
        assertThat(nfcTag.getDateDiscovered(), is(equalTo(TEST_DATE_DISCOVERED)));
    }

    @Test
    public void toJson_constructor1_returns_correct() throws JSONException {
        // Object creation
        NfcTag nfcTag = new NfcTag(TEST_TITLE, TEST_DIFFICULTY, TEST_TAG_ID);

        // Stubs Factory object.
        nfcTag.setFactory(mMockFactory);

        // When
        when(mMockFactory.getJsonObject()).thenReturn(mMockJsonObject);

        // Method call
        nfcTag.toJson();

        // Verify
        verify(mMockJsonObject).put(JsonAttributes.DIFFICULTY, TEST_DIFFICULTY);
        verify(mMockJsonObject).put(JsonAttributes.TAG_ID, TEST_TAG_ID);
        verify(mMockJsonObject).put(JsonAttributes.TITLE, TEST_TITLE);
    }

    @Test
    public void toJson_constructor2_returns_correct() throws JSONException {
        // Object creation
        NfcTag nfcTag = new NfcTag(TEST_TAG_ID, TEST_TITLE, TEST_PICTURE_FILE_PATH, TEST_DIFFICULTY, TEST_DISCOVERED, TEST_DATE_DISCOVERED);

        // Stubs Factory object.
        nfcTag.setFactory(mMockFactory);

        // When
        when(mMockFactory.getJsonObject()).thenReturn(mMockJsonObject);

        // Method call
        nfcTag.toJson();

        // Verify
        verify(mMockJsonObject).put(JsonAttributes.DIFFICULTY, TEST_DIFFICULTY);
        verify(mMockJsonObject).put(JsonAttributes.TAG_ID, TEST_TAG_ID);
        verify(mMockJsonObject).put(JsonAttributes.TITLE, TEST_TITLE);
        verify(mMockJsonObject).put(JsonAttributes.PICTURE_FILE_PATH, TEST_PICTURE_FILE_PATH);
        verify(mMockJsonObject).put(JsonAttributes.DISCOVERED, TEST_DISCOVERED);
        verify(mMockJsonObject).put(JsonAttributes.DATE_DISCOVERED, TEST_DATE_DISCOVERED);
    }

    @Test
    public void toJson_mutators_returns_correct() throws JSONException {
        // Object creation
        NfcTag nfcTag = new NfcTag();
        nfcTag.setTagId(TEST_TAG_ID);
        nfcTag.setTitle(TEST_TITLE);
        nfcTag.setDifficulty(TEST_DIFFICULTY);
        nfcTag.setPictureFilePath(TEST_PICTURE_FILE_PATH);
        nfcTag.setDiscovered(TEST_DISCOVERED);
        nfcTag.setDateDiscovered(TEST_DATE_DISCOVERED);

        // Stubs Factory object.
        nfcTag.setFactory(mMockFactory);

        // When
        when(mMockFactory.getJsonObject()).thenReturn(mMockJsonObject);

        // Method call
        nfcTag.toJson();

        // Verify
        verify(mMockJsonObject).put(JsonAttributes.DIFFICULTY, TEST_DIFFICULTY);
        verify(mMockJsonObject).put(JsonAttributes.TAG_ID, TEST_TAG_ID);
        verify(mMockJsonObject).put(JsonAttributes.TITLE, TEST_TITLE);
        verify(mMockJsonObject).put(JsonAttributes.PICTURE_FILE_PATH, TEST_PICTURE_FILE_PATH);
        verify(mMockJsonObject).put(JsonAttributes.DISCOVERED, TEST_DISCOVERED);
        verify(mMockJsonObject).put(JsonAttributes.DATE_DISCOVERED, TEST_DATE_DISCOVERED);
    }
}