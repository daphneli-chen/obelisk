package edu.brown.cs.obelisk.main.handlers;

import com.google.common.collect.ImmutableMap;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import java.util.Map;

/**
 * Handler for obelisk home page.
 */
public class ObeliskLandingHandler implements TemplateViewRoute {
  @Override
  public ModelAndView handle(Request req, Response res) {
    Map<String, Object> variables = ImmutableMap.of("title",
            "Obelisk");
    return new ModelAndView(variables, "obelisk-landing.ftl");
  }
}
