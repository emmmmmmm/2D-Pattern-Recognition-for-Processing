PatternMatcher2D PM; // the prime minister of patterns! :D
int currentIndex=-1;

int rightPrediction=0, falsePrediction=0;

//===================================================
void setup() {
  size(1400, 500);
  importData();
  //buildData(2);
  frameRate(10000);
  PM = new PatternMatcher2D(5, 2, 1); // (pattern length, target offset)
  PM.minScore = 50; // in percent
  PM.cleanup = false;  // remove duplicate entries
  PM.learn(inputdata); // find patterns in input data
}
//===================================================
void draw() {
  currentIndex++;
  if (currentIndex>=testData.length-10)noLoop();
  frame.setTitle((int)frameRate+" cps "+currentIndex);

  Pattern[] p = PM.testPattern(testData, currentIndex);
  frameRate(p.length > 5 ? 2 : 100000); // slow down if multiple patterns are found!
  if (p.length==0) return; // to speed tings up...!

  // display data and results...:
  background(255);
  displayData(1, 0, testData.length);
  line(map(currentIndex, 0, testData.length, 0, width), 0, map(currentIndex, 0, testData.length, 0, width), height);
  stroke(0);
  fill(0);
  text(p.length, 10, 20);


  color c;
  stroke(0,100);
  line(0, height/2, width, height/2);
  float target = 0;
  float scale = 10000;
  for (int i=0; i<p.length; i++) {
    c = color(random(0, 200), random(0, 200), random(0, 200), 180);

    for (int j=0; j<p[i].dim2; j++) {
      pushMatrix();
      translate(j*200, 0);
      fill(c);
      stroke(c);
      // draw pattern
      for (int k=0; k<p[i].dim1; k++) {
        ellipse(50+k*15, height/2-p[i].getValues()[k][j]*scale, 3, 3);
        if (k>0)line(50+k*15, height/2-p[i].getValues()[k][j]*scale, 50+(k-1)*15, height/2-p[i].getValues()[k-1][j]*scale);
      }
      // draw prediction
      ellipse(50+p[0].dim1*17, height/2-p[i].result[j]*scale, 3, 3);
      // draw Target
      target = percentChange(testData[currentIndex][j], testData[currentIndex+PM.futureOffset][j]);
      target = testData[currentIndex+PM.futureOffset][j]-testData[currentIndex][j];
      fill(0); stroke(0);
      ellipse(50+p[0].dim1*22, height/2-target*scale, 5, 5);

      popMatrix();
      // that only works if there's only 1 pattern found... -,-
      if (p[i].result[j]>0 && target >0 || p[i].result[j]<0 && target<0) rightPrediction++;
      else falsePrediction++;
    }
  }

  if (falsePrediction>0 && rightPrediction>0)
  text((float)rightPrediction/falsePrediction+"("+rightPrediction+" / "+falsePrediction+")", 10, height-20);
}

// utility-functions:
//===================================================
float percentChange(float a, float b) {
  return 100.0*(b-a)/abs(a);
}
//===================================================
float[] percentChange(float[] ar) {
  float[] ret = new float[ar.length-1];
  for (int i=0; i<ret.length; i++) {
    ret[i] = percentChange(ar[i], ar[i+1]);
  }
  return ret;
}
//===================================================
float min(float[][] ar) {
  float ret = 2e31;
  for (int i=0; i<ar.length; i++)
  for (int j=0; j<ar[i].length; j++)
  ret = ar[i][j]<ret ? ar[i][j] : ret;
  return ret;
}
//===================================================
float max(float[][]ar) {
  float ret = -2e31;
  for (int i=0; i<ar.length; i++)
  for (int j=0; j<ar[i].length; j++)
  ret = ar[i][j]>ret ? ar[i][j] : ret;
  return ret;
}
//===================================================
