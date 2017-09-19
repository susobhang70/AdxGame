package adx.experiments;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import adx.exceptions.AdXException;
import adx.util.Logging;

/**
 * Run one-day experiments.
 * 
 * @author Enrique Areyan Viqueira
 */
public class OneDayExperiments {

  /**
   * Main function.
   * 
   * @param args
   * @throws AdXException
   * @throws UnsupportedEncodingException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws AdXException, FileNotFoundException, UnsupportedEncodingException {
    // Pure agent experiments.
    /*for (int j = 2; j < 11; j++) {
      Logging.log("All WE agents " + j);
      ExperimentFactory.allWEExperiment(j).runExperiment();
      Logging.log("All WF agents " + j);
      ExperimentFactory.allWFExperiment(j).runExperiment();
    }*/
    // Mix of 2 type of agents experiments (except SI v SI which is not interesting).
    for (int j = 1; j < 31; j++) {
      for (int l = 1; l < 31; l++) {
          Logging.log("SI and WE agents (" + j + "," + l + ")");
          ExperimentFactory.SIandWEAgents(j, l).runExperiment();
        /*Logging.log("SI and WF agents (" + j + "," + l + ")");
        ExperimentFactory.SIandWFAgents(j, l).runExperiment();
        Logging.log("WE and WF agents (" + j + "," + l + ")");
        ExperimentFactory.WEandWFAgents(j, l).runExperiment();
        // All 3 types of agents playing.
        for (int k = 1; k < 11; k++) {
          Logging.log("SI and WE and WF agents ("+ j + "," + l + "," + k +")");
          ExperimentFactory.SIandWEandWFAgents(j, l, k).runExperiment();
        }*/
      }
    }
  }
}
