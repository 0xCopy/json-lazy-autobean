package com.colinalworth.gwt.beans.shared;

import org.junit.Test;

import java.nio.ByteBuffer;

public class ByteSplittableTest {

  @Test
  public void testSimple() {
    ByteBuffer json1 = ByteBuffer.wrap("{}".getBytes());
    ByteBuffer json2 = ByteBuffer.wrap("[]".getBytes());
    ByteBuffer json3 = ByteBuffer.wrap("1".getBytes());
    ByteBuffer json4 = ByteBuffer.wrap("true".getBytes());
    ByteBuffer json5 = ByteBuffer.wrap("false".getBytes());
    ByteBuffer json6 = ByteBuffer.wrap("\"a1\"".getBytes());

    assert new ByteSplittable(json1).isKeyed();
    assert new ByteSplittable(json2).isIndexed();
    assert new ByteSplittable(json3).isNumber();
    assert new ByteSplittable(json4).isBoolean();
    assert new ByteSplittable(json5).isBoolean();
    assert new ByteSplittable(json6).isString();
  }

  @Test
  public void testFindByKey() {
    ByteBuffer json1 = ByteBuffer.wrap("{\"a\":1, \"ab\":2, \"abc\":3, \"z\":[]}".getBytes());
    ByteBuffer json2 = ByteBuffer.wrap("{\"abc\":1, \"ab\":2, \"a\":3, \"z\":[]}".getBytes());

    ByteSplittable split1 = new ByteSplittable(json1);
    assert split1.get("a").asNumber() == 1;
    assert split1.get("ab").asNumber() == 2;
    assert split1.get("abc").asNumber() == 3;
    assert split1.get("z").isIndexed();
    ByteSplittable split2 = new ByteSplittable(json2);
    assert split2.get("abc").asNumber() == 1;
    assert split2.get("ab").asNumber() == 2;
    assert split2.get("a").asNumber() == 3;
    assert split2.get("z").isIndexed();

    ByteBuffer json3 = ByteBuffer.wrap("{\"\":\"123\", \"a\":\"\"}".getBytes());
    ByteSplittable split = new ByteSplittable(json3);
    assert split.isKeyed();
    assert split.get("").isString();
    assert split.get("").asString().equals("123") : split.get("").asString();

    assert split.get("a").asString().equals("") : split.get("a").asString();
  }

  @Test
  public void testFindByIndex() {
    ByteBuffer json1 = ByteBuffer.wrap("[0, {\"moreitems\":[false, {}, 0]}, 1, true, \"a\"]".getBytes());

    ByteSplittable split = new ByteSplittable(json1);

    assert split.isIndexed();
    assert split.get(0).asNumber() == 0;
    assert split.get(1).isKeyed();
    //noinspection PointlessBooleanExpression
    assert split.get(1).get("moreitems").get(0).asBoolean() == false;
    assert split.get(1).get("moreitems").get(1).isKeyed();
    assert split.get(1).get("moreitems").get(2).asNumber() == 0;
    assert split.get(2).asNumber() == 1;
    //noinspection PointlessBooleanExpression
    assert split.get(3).asBoolean() == true;
    assert split.get(4).asString().equals("a");

  }

  @Test
  public void testNestedObjects() {
    ByteBuffer json1 = ByteBuffer.wrap("{\"obj\":{\"a\":[{}, 1, \"abc\", true, {\"foo\":2}]}}".getBytes());
    ByteSplittable split = new ByteSplittable(json1);

    assert split.get("obj").isKeyed();
    assert split.get("obj").get("a").isIndexed();
    assert split.get("obj").get("a").get(0).isKeyed();
    assert split.get("obj").get("a").get(1).asNumber() == 1;
    assert split.get("obj").get("a").get(2).asString().equals("abc");
    //noinspection PointlessBooleanExpression
    assert split.get("obj").get("a").get(3).asBoolean() == true;
    assert split.get("obj").get("a").get(4).get("foo").asNumber() == 2;
  }

  @Test
  public void testFindMissingKeys() {
    ByteBuffer json1 = ByteBuffer.wrap("{\"obj\":{\"a\":[{}, null, 1, \"abc\", true, {\"foo\":2}]}, \"n\":null}".getBytes());
    ByteSplittable split = new ByteSplittable(json1);

    assert split.get("foo") == null;
    assert split.isNull("foo");
    assert split.get("obj").get("asdf") == null;
    assert split.get("obj").isNull("asdf");
    assert split.get("obj").get("a").get(0).get("asdf") == null;
    assert split.get("obj").get("a").get(0).isNull("asdf");

    assert split.get("n") == null;
    assert split.isNull("n");
    assert split.get("obj").get("a").get(1) == null;
    assert split.get("obj").get("a").isNull(1);
  }
}
