import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

public class ID3Test {
	public static void main(String[] args) throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotWriteException {
		File m4a = new File("/Users/Alex/Music/iTunes/iTunes Media/Music/OneRepublic/Dreaming Out Loud/03 Stop and Stare.m4a");
		File mp3 = new File("/Users/Alex/Music/iTunes/iTunes Media/Music/Train/California 37/Drive By.mp3");
		// System.out.println("--- M4A File (Stop and Stare) ---");
		// diagnose(m4a);
		System.out.println("\n--- MP3 File (Drive By) ---");
		diagnose(mp3);
	}

	public static void diagnose(File file) throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotWriteException {
		AudioFile f = AudioFileIO.read(file);
		Tag tag = f.getTag();
		tag.deleteField(FieldKey.LYRICS);
		// USLT:       eng test set of lyrics
		//tag.setField(FieldKey.LYRICS, "los lyricos");
		List<String> list = tag.getAll(FieldKey.LYRICS);
		int i = 0;
		for (String field : list) {
			System.out.println((++i) + ": " + field);
		}
		f.commit();
	}
}
