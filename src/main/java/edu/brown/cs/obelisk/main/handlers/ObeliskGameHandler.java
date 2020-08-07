package edu.brown.cs.obelisk.main.handlers;

import com.google.common.collect.ImmutableMap;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import java.util.Map;

public class ObeliskGameHandler implements TemplateViewRoute {
  @Override
  public ModelAndView handle(Request req, Response res) {
    // int gameId = Integer.parseInt(req.params(":gameId"));
    Map<String, Object> variables = ImmutableMap.of("title",
            "Obelisk", "cssPath", "/");
    return new ModelAndView(variables, "game.ftl");
  }
}
