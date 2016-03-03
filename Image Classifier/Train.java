import java.util.ArrayList;
import java.util.Scanner;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;


public class Train{
	
	public String train(String image) throws Exception {
		//Initial training code won't work unless you change all directories
		//you will need the change the path of all the initial training images in path.txt 
		//I'd suggest leaving this code commented out and use the pre-trained arff file
		//this.createArff(); //initial training code
		//this.arffWriter(); //initial training code
		double[] histogram = createHistogram(new File(image));
		this.addTestCase(histogram);
		
		BufferedReader datafile = new BufferedReader(new FileReader("dataset.arff"));
		Instances data = new Instances(datafile);
		data.setClassIndex(data.numAttributes()-1);
		datafile.close();
		Instance testCase = data.instance(data.numInstances()-1);
		
		IBk knn = new IBk();
		knn.buildClassifier(data);
		double prediction = knn.classifyInstance(testCase);
		String s = null;
		if(prediction == 0){
			s = "Not Graph";
		}else{
			s = "Graph";
		}
		System.out.println(s);
		this.updateClassification(prediction);
		return s;
	}
	//breaks down the histogram of the image we want to classify
	//and write it arff format
	private void addTestCase(double[] histogram){
		try {
			File file = new File("dataset.arff");
			FileWriter writer = new FileWriter(file, true);
			double instance1 = this.getClusters(histogram);
			double instance2 = this.getLargestSpike(histogram);
			double instance3 = this.getDifference(histogram);
			String testCase = instance1+ "," + instance2+ "," + instance3+ ",?\n";
			writer.write(testCase);
			writer.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//creates a grayscale histogram from the given image
	protected static double[] createHistogram(File file){
		try {
			BufferedImage image = ImageIO.read(file);
			//get dimensions
			int width = image.getWidth();
			int height = image.getHeight();
			ArrayList<Integer> newValues = new ArrayList<Integer>();
			for (int row = 0; row < width; row++) {
				for (int col = 0; col < height; col++) {
					//get color values
					Color c = new Color(image.getRGB(row, col));
					//multiply RGB colors by .299, .587, .114 respectively
					//gray scaling the images for better efficiency  
					int grayValue = (int) (.299 * c.getRed() + .587 * c.getGreen() + .114 * c
							.getBlue()); 
					newValues.add(grayValue);
				}
			}
			double[] histogram = new double[256];
			//for each index color value encountered, increment the value at the index
			for (Integer grayValue : newValues) {
				int pixel = grayValue;
				histogram[pixel] += 1;
			}
			return histogram;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//get average difference between each gray-scaled histogram value
	//getting the distance tells us if there is a gradual or sharp change in pixels
	//sharp change = graph characteristic 
	private double getDifference(double[] histogram) {
		double totalDifference = 0;
		int count = 0;
		for (int i = 1; i < 256; i++) {
			//looks for differences in previous neighbor
			if (histogram[i - 1] != histogram[i]) {
				// add difference to total
				totalDifference += Math.abs(histogram[i] - histogram[i - 1]);
				count++;
			}
		}
		totalDifference /= count;
		return totalDifference;
	}
	//if a color dominates the image there is a higher change it would be graph  
	private double getLargestSpike(double[] histogram) {
		double largestColorSpike = 0;
		for (int i = 0; i < 256; i++) {
			if (histogram[i] > largestColorSpike){
				largestColorSpike = histogram[i];
			}
		}
		return largestColorSpike;
	}
	//if there are clusters of the same color on the image it might be a graph
	//if the histogram color (histogram[i]) is greater than 200 it is considered a cluster
	private double getClusters(double[] histogram){
		double region = 0;
		for(int i = 0; i < 256; i++){
			if(histogram[i] > 200){
				region++;
			}
		}
		return region;
	}

	private void createArff() {
		try {
			File file = new File("dataset.arff");
			FileWriter writer = new FileWriter(file);
			String s = "@RELATION Dataset\n\n@ATTRIBUTE value1 real\n@ATTRIBUTE value2 real\n"
					+ "@ATTRIBUTE value3 real\n@ATTRIBUTE type {natural,unnatural}\n\n@DATA\n";
			writer.write(s);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//read path file for path to images
	//build histogram for each image and write in .arff format
	private void arffWriter() {
		try {
			File paths = new File("paths.txt");
			Scanner in = new Scanner(paths);
			String s = in.nextLine();
			File file = new File("dataset.arff");
			FileWriter writer = new FileWriter(file, true);
			boolean natural = true;
			String st = "";
			do {
				if (s.equals("")) {
					s = in.nextLine();
					natural = false;

				}
				double[] histogram = createHistogram(new File(s));
				double att1 = this.getClusters(histogram);
				double att2 = this.getLargestSpike(histogram);
				double att3 = this.getDifference(histogram);
				if (natural == true) {
					st = att1 + "," + att2 + "," + att3 + "," + "natural\n";
				} else {
					st = att1 + "," + att2 + "," + att3 + "," + "unnatural\n";
				}
				writer.write(st);
				s = in.nextLine();
			} while (in.hasNextLine());

			writer.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateClassification(double value) {
		File file;
		String newFile = "";
		try {
			file = new File("dataset.arff");
			Scanner in = new Scanner(file);
			while (in.hasNextLine()) {
				String s = in.nextLine();
				if (!s.contains("?")) {
					newFile += s + "\n";
				} else {
					String rep = (value == 0) ? "natural" : "unnatural";
					s = s.replace("?", rep);
					newFile += s + "\n";

				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			file = new File("dataset.arff");
			FileWriter writer = new FileWriter(file);
			writer.write(newFile);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Train train = new Train();
		try {
			//*************************Change Image Here***********************************
			String img = "extestimg3.png";
			JFrame myWindow = new JFrame();
			myWindow.setSize(400, 320);
			myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			BufferedImage pic = ImageIO.read(new File(img));
			JLabel testImg = new JLabel(new ImageIcon(pic));
			myWindow.getContentPane().add(testImg);
			myWindow.setLocation(600, 200);
			myWindow.setTitle(train.train(img));
			myWindow.setVisible(true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
