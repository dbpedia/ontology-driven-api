import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dbpedia.api.model.ApiVersion;
import dbpedia.api.model.EntityRequestModel;
import dbpedia.api.model.Filter;
import dbpedia.api.model.ResourceModel;
import dbpedia.api.model.ValueRequestModel;
import dbpedia.api.versioning.SemanticVersionSet;
import dbpedia.api.versioning.SemanticVersionSet.Builder;
import dbpedia.api.versioning.VersionHandler;
import dbpedia.api.versioning.VersionLoader;
import dbpedia.api.versioning.VersionNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.Test;

public class VersioningTest {

  static Map<ResourceModel, ResourceModel> r1_0_1 = new HashMap<ResourceModel, ResourceModel>();
  static Map<ResourceModel, ResourceModel> r1_1_3 = new HashMap<ResourceModel, ResourceModel>();
  static Map<ResourceModel, ResourceModel> r1_2_0 = new HashMap<ResourceModel, ResourceModel>();
  static Map<String, String> prefixReplacementsInV1_2_0 = new HashMap<>();

  static {
    r1_0_1.put(
        new ResourceModel("dbp", "prop1"),
        new ResourceModel("dbo", "prop2")
    );
    r1_0_1.put(
        new ResourceModel("dbo", "prop2"),
        new ResourceModel("dbr", "prop3")
    );
    r1_0_1.put(
        new ResourceModel("dbr", "old-resource"),
        new ResourceModel("dbr", "new_resource")
    );
    r1_2_0.put(
        new ResourceModel("xyz", "abcdef"),
        new ResourceModel("dbr", "prop3")
    );
    r1_2_0.put(
        new ResourceModel("dbr", "prop3"),
        new ResourceModel("foaf", "foafprop")
    );
    r1_1_3.put(
        new ResourceModel("dbo", "bla1"),
        new ResourceModel("dbo", "newClass")
    );
    r1_1_3.put(
        new ResourceModel("dbr", "bla1"),
        new ResourceModel("dbr", "blablabla")
    );
    prefixReplacementsInV1_2_0.put("old_prefix", "new_prefix");
    prefixReplacementsInV1_2_0.put("someOtherPrefix", "blablabla");
  }


  ApiVersion v1_0_0 = emptyApiVersion(1, 0, 0);
  ApiVersion v1_0_1 = new ApiVersion(1, 0, 1, r1_0_1, new HashMap<>());
  ApiVersion v1_1_3 = new ApiVersion(1, 1, 3, r1_1_3, new HashMap<>());
  ApiVersion v1_1_0 = emptyApiVersion(1, 1, 0);
  ApiVersion v1_2_0 = new ApiVersion(1, 2, 0, r1_2_0, prefixReplacementsInV1_2_0);
  ApiVersion v2_0_1 = emptyApiVersion(2, 0, 1);
  ApiVersion v3_2_3 = emptyApiVersion(3, 2, 3);

  SemanticVersionSet.Builder<ApiVersion> builder = new Builder<>();
  SemanticVersionSet<ApiVersion> versions = builder.add(v1_0_0)
      .add(v1_0_1)
      .add(v1_1_3)
      .add(v1_1_0)
      .add(v1_2_0)
      .add(v2_0_1)
      .add(v3_2_3)
      .build();

  VersionHandler handler = new VersionHandler(versions);

  private static ApiVersion emptyApiVersion(int major, int minor, int patch) {
    return new ApiVersion(major, minor, patch, new HashMap<>(), new HashMap<>());
  }

  @Test
  public void modelAndIteratorTest() throws VersionNotFoundException {
    assertTrue(versions.get(1, 0, 0) == v1_0_0);
    assertTrue(versions.get(1, 1, 3) == v1_1_3);
    assertTrue(versions.get(2, 0, 1) == v2_0_1);

    //make sure that the VersionNotFoundException is thrown
    boolean inCatch = false;
    try {
      versions.get(0, 0, 0);
    } catch (VersionNotFoundException e) {
      inCatch = true;
    }
    assertTrue(inCatch);

    Iterator<ApiVersion> iterator = versions.iterateToLatest(v1_1_0).iterator();
    assertTrue(iterator.next() == v1_1_3);
    assertTrue(iterator.next() == v1_2_0);
    assertTrue(iterator.next() == v2_0_1);
    assertTrue(iterator.next() == v3_2_3);
    assertTrue(!iterator.hasNext());

  }

  @Test
  public void valueRequestReplacementTest() throws VersionNotFoundException {
    ValueRequestModel v1_0_0Request = new ValueRequestModel.Builder()
        .setProperties(TestUtils.setOf(
            new ResourceModel("foaf", "blablabla"),
            new ResourceModel("dbr", "property"),
            new ResourceModel("dbp", "prop1"),
            new ResourceModel("dbo", "prop2"),
            new ResourceModel("dbr", "prop3")
        ))
        .setEntities(TestUtils.setOf("bla1", "bla2", "bla3"))
        .setVersion("1.0.0")
        .build();
    Set<ResourceModel> expectedPropertiesAfterReplacements = TestUtils.setOf(
        new ResourceModel("foaf", "blablabla"),
        new ResourceModel("dbr", "property"),
        new ResourceModel("dbo", "prop2"),
        new ResourceModel("foaf", "foafprop")
    ); //set has only 4 items because the dbr:prop3 occurred 2 times (at v1.2.0)
    Set<String> expectedEntities = TestUtils.setOf("blablabla", "bla2", "bla3");

    assertFalse(handler.canPatchToLatest(v1_0_0Request));
    //apply all patches from v1.0.0 to v3.2.3
    //the controller would not do this because the compability breakes when the major number increases
    //instead, the controller would just use the RequestModel as it is
    ValueRequestModel patchedRequest = handler.patchValueRequestModel(v1_0_0Request);
    assertEquals("3.2.3", patchedRequest.getVersion());
    assertEquals(expectedPropertiesAfterReplacements, patchedRequest.getProperties());
    assertEquals(expectedEntities, patchedRequest.getEntities());
  }

  @Test
  public void entitiyRequestReplacementTest() throws VersionNotFoundException {
    Set<Filter> v1_0_0Filters = TestUtils.setOf(
        new Filter.Builder()
            .setFilterProps(new ResourceModel("dbo", "prop2"))
            .setFilterVal("some value")
            .setFilterOp("=")
            .build(),
        new Filter.Builder() //only filter property gets replaced
            .setFilterProps(new ResourceModel("db-bla", "does-not-change"))
            .setFilterVal("some value")
            .setFilterOp("<")
            .build(),
        new Filter.Builder() //Filter value + property gets replaced
            .setFilterVal("old-resource") //= dbr:old-resource
            .setFilterProps(new ResourceModel("dbr", "bla1"))
            .build(),
        new Filter.Builder() //only prefix gets replaced
            .setFilterVal("doesn't_matter")
            .setPropName("prop2")
            .setPrefix("old_prefix")
            .setFilterOp("=")
            .build()
    );
    Set<Filter> expectedFilters = TestUtils.setOf(
        new Filter.Builder()
            .setFilterProps(new ResourceModel("foaf", "foafprop"))
            .setFilterVal("some value")
            .setFilterOp("=")
            .build(),
        new Filter.Builder()
            .setFilterProps(new ResourceModel("db-bla", "does-not-change"))
            .setFilterVal("some value")
            .setFilterOp("<")
            .build(),
        new Filter.Builder()
            .setFilterVal("new_resource")
            .setFilterProps(new ResourceModel("dbr", "blablabla"))
            .build(),
        new Filter.Builder()
            .setFilterVal("doesn't_matter")
            .setFilterOp("=")
            .setPropName("prop2")
            .setPrefix("new_prefix")
            .build()
    );

    EntityRequestModel v1_0_0Request = new EntityRequestModel.Builder()
        .setFilterList(v1_0_0Filters)
        .setClassname("bla1") //=dbo:bla1; gets replaced to "newClass"
        .setVersion("1.0.0")
        .build();

    EntityRequestModel patchedRequest = handler.patchEntityRequestModel(v1_0_0Request);
    assertTrue(patchedRequest.getVersion().equals("3.2.3"));
    assertEquals("newClass", patchedRequest.getClassName());
    assertEquals(expectedFilters, patchedRequest.getFilterList());
  }

  @Test
  public void loadFromDirTest() throws IOException, VersionNotFoundException {
    ClassLoader classLoader = getClass().getClassLoader();
    File path = new File(
        Objects.requireNonNull(classLoader.getResource("versions/")).getFile());

    VersionLoader loader = new VersionLoader();
    SemanticVersionSet<ApiVersion> versions = loader.loadFromDirectory(path);

    assertTrue(versions.contains(1, 1, 0));
    assertTrue(versions.contains(1, 2, 0));
    assertTrue(versions.contains(1, 2, 1));

    //assert that v 1.1.0 matches with the file test/resources/versions/1_1_0.versions.json
    ApiVersion v1_1_0 = versions.get(1, 1, 0);
    assertEquals("dbo", v1_1_0.getPrefixReplacements().get("old-dbo"));
    assertEquals(
        new ResourceModel("dbp", "numberOfEmployees"),
        v1_1_0.getResourceReplacements().get(
            new ResourceModel("dbp", "numOfEmployees")
        )
    );
    assertEquals(
        new ResourceModel("foaff", "surname"),
        v1_1_0.getResourceReplacements().get(
            new ResourceModel("dbp", "some-property-that-does-not-work-anymore")
        )
    );
  }

}
