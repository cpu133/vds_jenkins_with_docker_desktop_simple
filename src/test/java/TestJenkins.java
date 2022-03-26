import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.hamcrest.MatcherAssert;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.testcontainers.shaded.org.hamcrest.Matchers.containsString;

// tag::blog[]
@Testcontainers
@DisplayName("Jenkins")
class TestJenkins {

  public static final int JENKINS_PORT = 8080;

  @Container // <.>
  public static DockerComposeContainer jenkinsServer =
      new DockerComposeContainer(new File("docker-compose.yml"))
          .withExposedService("jenkins", JENKINS_PORT)
          .waitingFor("jenkins", Wait.forLogMessage(".*Jenkins is fully up and running*\\n", 1)); // <.>

  @Test
  @DisplayName("first request to fresh container should return \"Authentication required\"")
  void firstRequestShouldReturnsAuthenticationRequiredTest()
      throws Exception {
    String jenkinsUrl = "http://"
        + jenkinsServer.getServiceHost("jenkins", JENKINS_PORT)
        + ":" +
        jenkinsServer.getServicePort("jenkins", JENKINS_PORT);

    MatcherAssert.assertThat("first request to fresh container should return \"Authentication required\"",
        callHttpEndpoint(jenkinsUrl),
        containsString("Authentication required")// <.>
    );
  }

  private static String callHttpEndpoint(String url) throws IOException, InterruptedException { // <.>
    HttpClient httpClient = HttpClient.newBuilder().build();
    HttpRequest mainRequest = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .build();

    HttpResponse<String> response = httpClient.send(mainRequest, HttpResponse.BodyHandlers.ofString());
    return response.body();
  }

}
// end::blog[]