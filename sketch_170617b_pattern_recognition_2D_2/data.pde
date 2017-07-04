float[][] inputdata, testData;
//===================================================
// build ar of randomized data
void buildData(int dim2) {
  int numTrainingSamples = 2000;
  int numEvalSamples = 1000;
  print("data ...");
  inputdata = new float[numTrainingSamples][dim2];
  testData = new float[numEvalSamples][dim2];

  for (int i=1; i<inputdata.length; i++)
  for (int j=0; j<inputdata[i].length; j++)
  inputdata[i][j] = inputdata[i-1][j]+random(-1, 1) ;

  for (int i=1; i<testData.length; i++)
  for (int j=0; j<testData[i].length; j++)
  testData[i][j] = testData[i-1][j] + random(-1, 1);

  println(" data generated: ");
  println(" training samples: "+inputdata.length);
  println(" test samples:     "+testData.length);
  displayData(1, 0, testData.length);
}
//===================================================
void displayData(int index, int back, int forward) {
  stroke(100, 100);
  fill(100, 100);
  float scaleMin = min(testData);
  float scaleMax = max(testData);

  for (int i=-back; i<forward-1; i++) {
    if (index+i<1)continue;
    if (index+i>=testData.length)continue;
    for (int j=0; j<testData[index+i].length; j++) {
      line(
        map(i, -back, forward, 0, width),
        map(testData[index+i][j], scaleMin, scaleMax, height, 0),
        map(i-1, -back, forward, 0, width),
        map(testData[index+i-1][j],scaleMin, scaleMax, height, 0)
        );
      }
    }
  }


  //------------------------------------------
  // load data from file
  void importData() {
    int numTrainingSamples = 25000;
    int numEvalSamples = 5500;

    float[][] data, f;
    String lines[] = loadStrings("Data/EURUSD240.csv");
    println(lines.length);
    String tmp[][] = new String[lines.length][0];
    //  println(lines.length+" entries in input file");
    // prepare data array;
    for (int i=0; i<lines.length; i++)
    tmp[i] = split(lines[i], ",");

    tmp = (String[][]) subset(tmp, 1000);

    data = new float[tmp.length][tmp[0].length];
    for (int i=0; i<tmp.length; i++)
    for (int j=0; j<tmp[i].length-2; j++) {
      data[i][j] = Float.parseFloat(tmp[i][j+2]);
    }
    f = new float[numTrainingSamples][2];

    // build training data
    for (int i=0; i<f.length; i++) {
      f[i][0] =  data[i][1];// high
      f[i][1] = data[i][2]; // low
      //f[i][0] = data[i][3]; // close
    }
    inputdata = f;

    // build evaluationData
    tmp = (String[][]) subset(tmp, 1*numTrainingSamples);
    data = new float[tmp.length][tmp[0].length];
    for (int i=0; i<tmp.length; i++)
    for (int j=0; j<tmp[i].length-2; j++) {
      data[i][j] = Float.parseFloat(tmp[i][j+2]);
    }
    f = new float[numEvalSamples][2];
    // build training data
    for (int i=0; i<f.length; i++) {
      f[i][0] = data[i][1];// high
      f[i][1] = data[i][2]; // low
      //f[i][0] = data[i][3]; // close
    }
    testData = f;
    displayData(1, 0, testData.length);
  }
