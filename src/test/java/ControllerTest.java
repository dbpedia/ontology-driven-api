/*import static junit.framework.Assert.assertEquals;

import dbpedia.api.configuration.Configuration;
import dbpedia.api.controller.Controller;
import dbpedia.api.controller.RequestHandler;
import dbpedia.api.model.EntityRequestModel;
import dbpedia.api.model.Filter;
import dbpedia.api.model.Filter.Bool;
import dbpedia.api.model.LogDataModel;
import dbpedia.api.model.RequestModel;
import dbpedia.api.model.RequestModel.ReturnFormat;
import dbpedia.api.model.RequestModel.Style;
import dbpedia.api.model.ResourceModel;
import dbpedia.api.model.ValueRequestModel;
import dbpedia.api.model.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class ControllerTest {

  HashMap mapped = new HashMap();
  
  public static class RequestHandlerMock implements RequestHandler {

    private RequestModel model;

    @Override

    public ResponseEntity handle(RequestModel request, LogDataModel log) {
      this.model = request;
      return null;
    }

    public RequestModel getModel() {
      return model;
    }
  }


  private Configuration config = new Configuration();

  @Before
  public void init() {
    config.setUriPath("api");
  }

  @Test
  public void testEntityRequest() {
    RequestHandlerMock mock = new RequestHandlerMock();

    List<String> values = new LinkedList<>();
    values.add("someResource,dbo:someproperty");
    List<String> format = new LinkedList<>();
    format.add("jSonLd");
    List<String> pretty = new LinkedList<>();
    pretty.add("nested");
    List<String> limit  = new LinkedList<>();
    limit.add("0");
    List<String> offset  = new LinkedList<>();
    offset.add("320");
    List<String> setOp = new LinkedList<>();
    setOp.add("intersection");
    
    MultiValueMap <String,String>parameters = new LinkedMultiValueMap<>();
    parameters.put("value", values);
    parameters.put("format", format);
    parameters.put("pretty", pretty);
    parameters.put("limit", limit);
    parameters.put("offset", offset);
    parameters.put("setOp", setOp);

    Controller controller = new Controller(mock, config, mapped);
    controller.entityRequest("api",
        "v10",
        "my-secret-key",
        true,
        parameters,
        new StupidHttpServletRequestMock());

    Set<Filter> expectedFilterList = TestUtils.setOf(
        new Filter.Builder().setFilterBool(Bool.AND)
            .setFilterOp(null)
            .setFilterProps(new ResourceModel("dbo", "someproperty"))
            .setFilterVal("someResource")
            .build()
    );

    EntityRequestModel expectedModel = new EntityRequestModel.Builder()
        .setFilterList(expectedFilterList)
        .setFormat(ReturnFormat.JSONLD)
        .setStyle(Style.NESTED)
        .setKey("my-secret-key")
        .setWindow(new Window(320, 0))
        .setVersion("v10")
        .build();

    assertEquals(expectedModel.getFilterList(),
        ((EntityRequestModel) mock.getModel()).getFilterList());
    assertEquals(expectedModel, mock.getModel());

  }

  @Test
  public void testInstancesRequest() {
    RequestHandlerMock mock = new RequestHandlerMock();

    List<String> filter = new LinkedList<>();
    filter.add("dbo:someProperty,lt,someLiteral");
    List<String> format = new LinkedList<>();
    format.add("jSonLd");
    List<String> pretty = new LinkedList<>();
    pretty.add("nested");
    List<String> limit  = new LinkedList<>();
    limit.add("0");
    List<String> offset  = new LinkedList<>();
    offset.add("320");
    List<String> setOp = new LinkedList<>();
    setOp.add("intersection");
    List<String> onlyImportant = new LinkedList<>();
    onlyImportant.add("true");
    MultiValueMap <String,String>parameters = new LinkedMultiValueMap<>();
    parameters.put("filter", filter);
    parameters.put("format", format);
    parameters.put("pretty", pretty);
    parameters.put("limit", limit);
    parameters.put("offset", offset);
    parameters.put("setOp", setOp);
    parameters.put("onlyImportant", onlyImportant);
    
    Controller controller = new Controller(mock, config, mapped);
    controller.instancesRequest("api",
        "v10",
        "aClass",
        "my-secret-key",
        true,
        parameters,
        new StupidHttpServletRequestMock());

    Set<Filter> expectedFilterList = TestUtils.setOf(
        new Filter.Builder().setFilterBool(Bool.AND)
            .setFilterOp("<")
            .setFilterProps(new ResourceModel("dbo", "someProperty"))
            .setFilterVal("someLiteral")
            .build()
    );

    EntityRequestModel expectedModel = new EntityRequestModel.Builder()
        .setFilterList(expectedFilterList)
        .setFormat(ReturnFormat.JSONLD)
        .setStyle(Style.NESTED)
        .setKey("my-secret-key")
        .setWindow(new Window(320, 0))
        .setVersion("v10")
        .setClassname("aClass")
        .setOnlyImporant(Boolean.TRUE)
        .build();

    assertEquals(expectedModel.getFilterList(),
        ((EntityRequestModel) mock.getModel()).getFilterList());
    assertEquals(expectedModel, mock.getModel());

  }



  @Test
  public void testValuesRequest() {
    RequestHandlerMock mock = new RequestHandlerMock();
    Controller controller = new Controller(mock, config, mapped);
    List<String> properties = new LinkedList<>();
    properties.add("foaf:surname");
    properties.add("dbr:blablabla");
    ArrayList<String> entities = new ArrayList<>();
    entities.add("entity1");
    entities.add( "entity2");
    entities.add("entity3");
    List<String> format = new LinkedList<>();
    format.add("TSV");
    List<String> pretty = new LinkedList<>();
    pretty.add("NESTED");
    List<String> limit = new LinkedList<>();
    limit.add("0");
    List<String> offset= new LinkedList<>();
    offset.add("0");
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.put("property",properties);
    map.put("format",format );
    map.put("pretty", pretty);
    map.put("limit", limit);
    map.put("offset", offset);
    controller.valueRequest("api",
        "1.0.0",
            entities,
            "my-secret-key",
        true,
            map,
        new StupidHttpServletRequestMock()
    );
    assertEquals(new ValueRequestModel.Builder()
                .setEntities(TestUtils.setOf("entity1", "entity2", "entity3"))
        .setProperties(TestUtils.setOf(
            new ResourceModel("foaf", "surname"),
            new ResourceModel("dbr", "blablabla")))
        .setFormat(ReturnFormat.TSV)
        .setStyle(Style.NESTED)
        .setKey("my-secret-key")
        .setWindow(new Window(0, 0))
        .setVersion("1.0.0")
        .build(), mock.getModel());

  }


}

*/