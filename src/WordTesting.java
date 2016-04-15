import org.tartarus.snowball.ext.englishStemmer;

public class WordTesting {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		englishStemmer stemmer = new englishStemmer();
		stemmer.setCurrent("ties");
		stemmer.stem();
		System.out.println(stemmer.getCurrent());
		stemmer.setCurrent("Car");
		stemmer.stem();
		System.out.println(stemmer.getCurrent());
	}

}
