package edu.brown.cs.obelisk.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.brown.cs.obelisk.game.enemy.Demon;
import edu.brown.cs.obelisk.game.player.Player;
import edu.brown.cs.obelisk.main.websockets.GameWebSocket;
import edu.brown.cs.obelisk.main.websockets.LandingWebSocket;
import edu.brown.cs.obelisk.main.handlers.ObeliskGameHandler;
import edu.brown.cs.obelisk.main.handlers.ObeliskLandingHandler;
import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The Main class of our project. This is where execution begins.
 *
 * @author mlitt2
 */
public final class Main {

  private static final int DEFAULT_PORT = 4567;
  public static final Gson GSON =  new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(Demon.class, new InterfaceJSON())
        .registerTypeAdapter(Player.class, new InterfaceJSON())
        .create();
  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private final String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    // Parse command line arguments
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
          .defaultsTo(DEFAULT_PORT);
    //OptionSet options = parser.parse(args);
    runSparkServer();
    /*
    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }
    */
    // TODO: the final project!
    System.out.println("Obelisk!");
  }

  static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
            templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer() {
    Spark.port(getHerokuAssignedPort());
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine();
    Spark.webSocket("/landingSocket", LandingWebSocket.class);
    Spark.webSocket("/gameSocket", GameWebSocket.class);
    Spark.get("/obelisk", new ObeliskLandingHandler(), freeMarker);
    Spark.get("/obelisk/:gameId", new ObeliskGameHandler(), freeMarker);
  }

  /**
   * Display an error page when an exception occurs in the server.
   *
   * @author mlitt2
   */
  private static class ExceptionPrinter implements ExceptionHandler<Exception> {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }
}
