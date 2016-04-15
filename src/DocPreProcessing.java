import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;
import java.lang.Math;
import org.tartarus.snowball.ext.englishStemmer;


public class DocPreProcessing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final String defaultStopFile = "StopWords.txt";
		final String defaultTextFile = "Documents.txt";
		final String defaultOutputFile = "ProcessedOutput.txt";
		final int defaultThreshold = 10;
		
		String printOutput = "t";													//Default print output to 0, so it prints out if no args are provided
		
		int ReviewCount = 0;
		TreeSet<String> stopWords = new TreeSet<String>();
		HashMap<String, Integer> wordCloud = new HashMap<String, Integer>();
		HashMap<String, Integer> documentFreq = new HashMap<String, Integer>();
		HashMap<String, Double> TF_IDF = new HashMap<String, Double>();
		HashSet<String> alreadyCounted = new HashSet<String>(); 
		englishStemmer stemmer = new englishStemmer();
		
		if (args.length > 0){
			if("f".equals(args[0].toLowerCase())){								//Check if there is a flag to not print data, if not then ignore
				printOutput = "f";
			}
		}
		
		try{
			File file1 = new File(defaultStopFile);
			File file2 = new File(defaultTextFile);
			
			
			if ((!file1.exists())&(!file2.exists())){
				throw new FileNotFoundException();
			}
			BufferedReader br = new BufferedReader(new FileReader(file1));
			String Line;
			while((Line = br.readLine()) != null){
				if (Line.startsWith("//")){ 									//Allows for comments and descriptions on the Stop Word Document
					continue;
				}
				stopWords.add(Line.trim());
			}
			br.close();
			br = new BufferedReader(new FileReader(file2));
			Line = "";
			String[] words;
			
			File outputFile = new File(defaultOutputFile);
			if(!outputFile.exists()) {
				outputFile.createNewFile();
			}
			
			String ProcessedString = "";
			
			while((Line = br.readLine()) != null){								//Each line is a single review.
				ReviewCount++;													//Increase the number of documents. In this case reviews
				Line = Line.replaceAll("<br>", " "); 							//Get rid of HTML line breaks
				Line = Line.replaceAll("[^a-zA-Z ]", "").toLowerCase(); 		//Get rid of non Alphabetic Characters
				words = Line.split("\\s");
				
				
				for(String word : words){
					stemmer.setCurrent(word);											//Pass the word to the English Stemmer
					stemmer.stem();														//Stem the word
					String wordStem = stemmer.getCurrent();
					
					if ("".equals(wordStem) | stopWords.contains(word)){				//Use the actual word for stopword checking in case the stem is not listed there
						continue;
					} else if(wordCloud.containsKey(wordStem)){ 						//Check if there already exists an instance of the word in the frequency count
						
						wordCloud.put(wordStem, wordCloud.get(wordStem) +1); 			//If an instance already exists, increment the count by one.
						
						if(!alreadyCounted.contains(wordStem)){ 						//Check if this word has already been counted for this Review/Document
							
							documentFreq.put(wordStem, documentFreq.get(wordStem) +1); 	//If the word has not been counted yet, increment the count by one. Since the word exists on the
																			   			//already exists on the cloud, we can assume it also has at least one instance in the Set.
							alreadyCounted.add(wordStem);								//Add the word to the already counted set, so it does not get double counted.
						}
						
						ProcessedString += word + " ";
						
					} else {													//If the word has never been encountered before, then just create instances in the Cloud and frequency
						ProcessedString += word + " ";							//maps and add it to the already counted set.
						wordCloud.put(wordStem, 1);
						documentFreq.put(wordStem, 1);
						alreadyCounted.add(wordStem);
					}
				}
				
				ProcessedString += "\r\n";
				alreadyCounted.clear();											//Clear the alreadyCounted set after each iteration.
			}
			
			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(ReviewCount + "\r\n");
			bw.write(ProcessedString.trim());
			
			br.close();
			bw.close();
			
		} catch (FileNotFoundException e){
			System.out.println("File was not found");
			System.exit(0);
		} catch (IOException e){
			System.out.println("File could not be read");
			System.exit(0);
		}
		TreeMap<String, Integer> sortedMap = sortMapByValue(wordCloud);			//Sort Map by value
		
		for(String key : sortedMap.keySet()){
			int frequency = wordCloud.get(key);
			double documentFreqCount = documentFreq.get(key);
			double IDF = Math.log(ReviewCount/documentFreqCount);
			
			TF_IDF.put(key, frequency*IDF);
			if ("t".equals(printOutput)){															//Print the data out only if requested.
				if(frequency >= defaultThreshold){
					System.out.println(key + "," + frequency);
				}
			}
			
		}
		
		
		
		TreeMap<String, Double> sortedTF_IDF = sortMapByValueDouble(TF_IDF);
		
		if("t".equals(printOutput)){																//Print the data out only if requested.
			System.out.println("=======================================================");
			System.out.println("End of Term Frequency, Start of TF-IDF");
			System.out.println("=======================================================");
			
			for (String key : sortedTF_IDF.keySet()){
				 System.out.println(key + ",\"" + String.format(Locale.ENGLISH, "%.2f", TF_IDF.get(key)) +"\"");
			}
		}
		
		
	}
	
	public static TreeMap<String, Integer> sortMapByValue(HashMap<String, Integer> map){
		Comparator<String> comparator = new ValueComparator(map);
		//TreeMap is a map sorted by its keys. 
		//The comparator is used to sort the TreeMap by keys. 
		TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
		result.putAll(map);
		return result;
	}

	public static TreeMap<String, Double> sortMapByValueDouble(HashMap<String, Double> map){
		Comparator<String> comparator = new ValuComparatorDouble(map);
		//TreeMap is a map sorted by its keys. 
		//The comparator is used to sort the TreeMap by keys. 
		TreeMap<String, Double> result = new TreeMap<String, Double>(comparator);
		result.putAll(map);
		return result;
	}
}
