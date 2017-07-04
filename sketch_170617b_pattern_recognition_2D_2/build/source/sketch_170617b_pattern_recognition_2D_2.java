import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class sketch_170617b_pattern_recognition_2D_2 extends PApplet {

PatternMatcher2D PM; // the prime minister of patterns! :D
int currentIndex=-1;

int rightPrediction=0, falsePrediction=0;

//===================================================
public void setup() {
  size(1400, 500);
  importData();
  //buildData(2);
  frameRate(10000);
  PM = new PatternMatcher2D(5, 2, 1); // (pattern length, target offset)
  PM.minScore = 60;
  PM.cleanup = true;
  PM.learn(inputdata);
}
//===================================================
public void draw() {
  currentIndex++;
  if (currentIndex>=testData.length-10)noLoop();
  frame.setTitle((int)frameRate+" cps "+currentIndex);

  Pattern[] p = PM.testPattern(testData, currentIndex);
  frameRate(p.length > 5 ? 2 : 100000);
  if (p.length==0) return; // to speed tings up...!

  // display data and results...:
  background(255);
  displayData(1, 0, testData.length);
  line(map(currentIndex, 0, testData.length, 0, width), 0, map(currentIndex, 0, testData.length, 0, width), height);
  stroke(0);
  fill(0);
  text(p.length, 10, 20);


  int c;
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
public float percentChange(float a, float b) {
  return 100.0f*(b-a)/abs(a);
}
//===================================================
public float[] percentChange(float[] ar) {
  float[] ret = new float[ar.length-1];
  for (int i=0; i<ret.length; i++) {
    ret[i] = percentChange(ar[i], ar[i+1]);
  }
  return ret;
}
//===================================================
public float min(float[][] ar) {
  float ret = 2e31f;
  for (int i=0; i<ar.length; i++)
  for (int j=0; j<ar[i].length; j++)
  ret = ar[i][j]<ret ? ar[i][j] : ret;
  return ret;
}
//===================================================
public float max(float[][]ar) {
  float ret = -2e31f;
  for (int i=0; i<ar.length; i++)
  for (int j=0; j<ar[i].length; j++)
  ret = ar[i][j]>ret ? ar[i][j] : ret;
  return ret;
}
//===================================================

//===================================================
class PatternMatcher2D {
  int patternLength1, patternLength2;
  int futureOffset;
  float minScore = 10;
  Pattern[] pattern;
  boolean cleanup = false;
  //------------------------------------------
  PatternMatcher2D(int pl1, int pl2, int fo) {
    pattern = new Pattern[0];
    patternLength1 = pl1;
    patternLength2 = pl2;
    futureOffset = fo;
  }
  //------------------------------------------
  // should i filter all the duplicate patterns? -> group all similar pattern into one?
  // that would speed up things enormously, but might worsen the results? -> test!
  public void learn(float[][] ar) {
    float[][] pat = new float[patternLength1][patternLength2];
    float[] res = new float[patternLength2];
    for (int i=patternLength1; i<ar.length-futureOffset; i++) {
      pattern = (Pattern[]) expand(pattern, pattern.length+1);
      pattern[pattern.length-1] = new Pattern();
      pattern[pattern.length-1].setValues(getPattern(ar,i));
      pattern[pattern.length-1].setResult(getResult(ar,i));
    }
    if(cleanup)cleanup();
    println(pattern.length+" pattern learned");
  }
  //------------------------------------------
  public void cleanup() {
    // i should somehow adjust/keep/take into account the target of the other patterns, no? ... -,-
    int index = 0, i=0;
    int removed = 0;
    float currentScore;
    float testScore;
    Pattern testPattern;
    float[][] results = new float[0][0];
    float currentResult=0;
    int right,wrong;
    boolean remove;
    println(pattern.length);
    while (index<pattern.length-1) {

      i=index+1;
      testPattern = pattern[index];
      results = new float[0][0];
      currentResult=0;
      right = 0;wrong = 0;
      while (i<pattern.length) {
        currentScore = testPattern.getScore(pattern[i]);
        if (currentScore>minScore) {
          // add result to results-ar
          results = (float[][]) expand(results, results.length+1);
          results[results.length-1] = pattern[i].result;
          // similar -> remove!
          arrayCopy(pattern, i, pattern, i-1, pattern.length-i);
          pattern = (Pattern[])shorten(pattern);
          //println("duplicate pattern removed");
          removed++;
        }
        i++;
      }
      // update expected result (there's probably a smarter way to do this)
      // would be good: first: test if they go to the same direction most of the time
      // if not: remove the pattern all together! (only keep high probability pattern!)
      remove = false;
      if(results.length>0){
        for(int k=0;k<patternLength2;k++){
          for(int j=0;j<results.length;j++){
            if(results[j][k]>0) right++;
            else                wrong++;
            currentResult+=results[j][k];
          }
          // only act if more than half of the instances point in the same direction!
          if(right/2>wrong){
            pattern[index].result[k]+=currentResult;
            pattern[index].result[k] /= (results.length+1);
          }
          else{
            remove=true;
          }
        }
      }
      // is this actually correct!?
      if(remove && index<pattern.length){
        //arrayCopy(src, srcPosition, dst, dstPosition, length)
        arrayCopy(pattern, index+1, pattern, index, pattern.length-(index+1));
        pattern = (Pattern[])shorten(pattern);
        index--;
        //println("pattern discarded");
        removed++;
      }
      index++;
    }
    println(removed+" pattern removed");
  }
  //------------------------------------------
  public float[][] getPattern(float[][] ar, int index) {
    float[][] ret = new float[patternLength1][patternLength2];
    for (int i=0; i<patternLength1; i++) {
      for (int j=0; j<patternLength2; j++) {
        //ret[i][j] = percentChange(ar[index-patternLength1+i][j], ar[index-patternLength1+i+1][j]);
        ret[i][j] = ar[index-patternLength1+i+1][j]-ar[index-patternLength1+i][j];
      }
    }
    return ret;
  }
  //------------------------------------------
  public float[] getResult(float[][] ar, int index) {
    float[] ret = new float[patternLength2];
    for (int j=0; j<patternLength2; j++)
    //  ret[j] = percentChange(ar[index][j], ar[index+futureOffset][j]);
      ret[j] = ar[index+futureOffset][j]-ar[index][j];
    return ret;
  }
  //------------------------------------------
  public Pattern[] testPattern(float[][] ar, int index) {
    index+=patternLength1;
    float avgScore = 0;
    float[] prediction =new float[patternLength2];
    float thisScore=0;
    int weight = 0;

    float[][] testPattern = getPattern(ar, index); //percentChange(subset(ar,index-patternLength,patternLength+1));


    Pattern[] matchingPatternAr = new Pattern[0];
    for (int i=0; i<pattern.length; i++) {
      thisScore=pattern[i].getScore(testPattern);
      if (thisScore > minScore) {
        //prediction += pattern[i].getResult();
        weight++;
        // add pattern to found-pattern ar!
        matchingPatternAr = (Pattern[]) expand(matchingPatternAr, matchingPatternAr.length+1);
        matchingPatternAr[matchingPatternAr.length-1] = pattern[i];
      }
    }
    return matchingPatternAr;
  }

  //------------------------------------------
  public float getTarget(Pattern[] p) {
    float ret = 0;
    for (int i=0; i<p.length; i++)
    for (int j=0; j<p[i].dim2; j++)
    ret+= p[i].result[j];
    return ret/p.length;
  }
}
float[][] inputdata, testData;
//===================================================
public void buildData(int dim2) {
  print("data ...");
  inputdata = new float[2000][dim2];
  testData = new float[1000][dim2];

  for (int i=1; i<inputdata.length; i++)
  for (int j=0; j<inputdata[i].length; j++)
  inputdata[i][j] = inputdata[i-1][j]+random(-1, 1) ;

  for (int i=1; i<testData.length; i++)
  for (int j=0; j<testData[i].length; j++)
  testData[i][j] = testData[i-1][j] + random(-1, 1);

  println(" loaded: ");
  println(" training samples: "+inputdata.length);
  println(" test samples:     "+testData.length);
  displayData(1, 0, testData.length);
}
//===================================================
public void displayData(int index, int back, int forward) {
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
        /*  ellipse(
        map(i, -back, forward, 0, width),
        map(testData[index+i][j], min(testData), max(testData), height, 0),
        2, 2);
        */
      }
    }
  }


  //------------------------------------------
  public void importData() {
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

//===================================================
class Pattern {
  float[][] values;
  float result[];
  float minSimilarity = 0;
  int dim1, dim2;
  //------------------------------------------
  Pattern(float[][] ar, float f[]) {
    setValues(ar);
    setResult(f);
  }
  Pattern(){}
  public void setValues(float[][] ar){
    values = ar;
    dim1 = ar.length;
    dim2 = ar[0].length;
  }
  public void setResult(float[] res){
    result = res;
  }
  //------------------------------------------
  public float getScore(float [][] ar) {
    float score = 0;
    float sim   = 0;

    for (int i=0; i<values.length; i++) {
      for (int j=0; j<values[i].length; j++) {
        sim = 100.0f - abs(percentChange(values[i][j], ar[i][j]));
        if (sim < minSimilarity)return 0; // why?
        score+=sim;
      }
    }
    return score/(dim1*dim2);
  }
  //------------------------------------------
  public float getScore(Pattern currentPat) {
    float score = 0;
    float sim   = 0;

    for (int i=0; i<values.length; i++) {
      for (int j=0; j<values[i].length; j++) {
        sim = 100.0f - abs(percentChange(values[i][j], currentPat.values[i][j]));
        if (sim < minSimilarity) return 0; // why?
        score+=sim;
      }
    }
    return score/(dim1*dim2);
  }
  //------------------------------------------
  public float[] getResult() {return result;  }
  //------------------------------------------
  public float[][] getValues() {return values;  }
  //------------------------------------------
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch_170617b_pattern_recognition_2D_2" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
