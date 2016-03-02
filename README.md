# Image-Classifier
Simple image classifier that determines whether the selected image is a regular image or graph. This project uses Java as well as a machine learning library known as Weka. 

Basic Overview
-Image is gray-scaled and broken down into a histogram.
-Three different algorithms are run on the resulting histogram to generate feature vectors.
-Feature vectors of the image will be compared to the trained data in dataset.arff using the KNN algorithm in Weka.
-The classified image will be added to the dataset in order to create a better classification model.

