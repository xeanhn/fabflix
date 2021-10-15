import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LogProcessor {
  public static void main(String[] args) {
    try {
      File logFile = new File("D:\\SPRING 2021\\CS122B\\Project 5 Demo Logs\\log1.txt");
      Scanner logReader = new Scanner(logFile);

      double totalServletTime = 0;
      double totalJDBCTime = 0;
      double numLines = 0;
      while (logReader.hasNextLine()) {
        String data = logReader.nextLine();
        String[] line_split = data.split(" ");
        totalServletTime += Double.parseDouble(line_split[0]);
        totalJDBCTime += Double.parseDouble(line_split[1]);
        numLines += 1;
      }
      logReader.close();
      double avgServletTime = totalServletTime / numLines; 
      avgServletTime = Math.round(avgServletTime * 100.0) / 100.0; 
      double avgJDBCTime = totalJDBCTime / numLines; 
      avgJDBCTime = Math.round(avgJDBCTime * 100.0) / 100.0;
      System.out.println("Average Servlet Time: " + avgServletTime);
      System.out.println("Average JDBC Time: " + avgJDBCTime);
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }
}