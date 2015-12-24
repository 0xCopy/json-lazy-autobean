package com.colinalworth.gwt.beans.vm;

import com.colinalworth.gwt.beans.shared.ByteSplittable;
import com.colinalworth.gwt.beans.shared.Results;
import com.colinalworth.gwt.beans.shared.ResultsBean;
import com.colinalworth.gwt.beans.shared.ShallowSplittable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gwt.user.client.Random;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class ThruputTest {
    private static final int WARMUP_SECONDS = 10;


    private List<Path> files;
    private boolean readGuids = true;

    //the way this test chops wood, the faster your cpu, the slower the unit test.
    private ByteBuffer removeFastCpuPenalty(Path file) throws IOException {
        if(!cache.containsKey(file)){
            ByteBuffer wrap = ByteBuffer.wrap(Files.readAllBytes(file));
            cache.put(file, wrap);}
        return (ByteBuffer) cache.get(file).rewind();
    }

    @Before
    public void getPaths() {
        String[] paths = {
                "target/test-classes/5k/",
                "target/test-classes/5k+whitespace/",
                "target/test-classes/20k/",
                "target/test-classes/20k+whitespace/",
        };
        String property = System.getProperty("test.json", "");
        if (!property.isEmpty()) {
            paths = property.split(Pattern.quote(String.valueOf(File.pathSeparatorChar)));
            //TODO find a better way to read a few sample properties from user-provided data
            readGuids = false;
        }

        files = new ArrayList<>();
        for (String path : paths) {
            files.add(Paths.get(path));
        }
    }

    public void warmupBBSplit() throws IOException {
        long start = System.currentTimeMillis();
        while (start + 1000 * WARMUP_SECONDS > System.currentTimeMillis()) {
            final List<String> guids = new ArrayList<>(4 * 5 * 20);
            for (Path path : files) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ByteBuffer bb = removeFastCpuPenalty(file);
                        ByteSplittable splittable = new ByteSplittable(bb);
                        if (readGuids) {
                            guids.add(splittable.get("guid").asString());
                        }

                        return FileVisitResult.CONTINUE;
                    }

                });
            }
        }
    }

    @Test
    public void testBBSplit() throws IOException {
        warmupBBSplit();

        for (Path path : files) {
            final double[] bytes = {0};
            final double[] time = {0};
            final List<String> guids = new ArrayList<>();
            while (time[0] < 1000) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ByteBuffer bb = removeFastCpuPenalty(file);
                        bytes[0] += bb.limit();
                        long start = System.currentTimeMillis();
                        ByteSplittable splittable = new ByteSplittable(bb);
                        if (readGuids) {
                            guids.add(splittable.get("guid").asString());
                            guids.add(splittable.get("items").get(4).get("guid").asString());
                        }
                        time[0] += System.currentTimeMillis() - start;

                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            System.out.println("ByteBufferSplittable on " + path);
            System.out.println(bytes[0] + " bytes in " + (time[0] / 1000.0) + " seconds, " + bytes[0] / time[0] * 1000.0 / 1024.0 / 1024.0 + "mb/second");
            System.out.println();
        }

    }

    public void warmupBBAutoBean() throws Exception {
        long start = System.currentTimeMillis();
        final Results.ABF abf = AutoBeanFactorySource.create(Results.ABF.class);
        while (start + 1000 * WARMUP_SECONDS > System.currentTimeMillis()) {
            final List<String> guids = new ArrayList<>(4 * 5 * 20);
            for (Path path : files) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ByteBuffer bb = removeFastCpuPenalty(file);
                        ByteSplittable splittable = new ByteSplittable(bb);
                        Results results = AutoBeanCodex.decode(abf, Results.class, splittable).as();
                        if (readGuids) {
                            guids.add(results.getGuid());
                        }

                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }

    @Test
    public void testBBAutoBean() throws Exception {
        warmupBBAutoBean();

        final Results.ABF abf = AutoBeanFactorySource.create(Results.ABF.class);
        for (Path path : files) {
            final double[] bytes = {0};
            final double[] time = {0};
            final List<String> guids = new ArrayList<>();
            while (time[0] < 1000) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ByteBuffer bb = removeFastCpuPenalty(file);
                        bytes[0] += bb.limit();
                        long start = System.currentTimeMillis();
                        ByteSplittable splittable = new ByteSplittable(bb);
                        Results results = AutoBeanCodex.decode(abf, Results.class, splittable).as();
                        if (readGuids) {
                            guids.add(results.getGuid());
                            guids.add(splittable.get("items").get(4).get("guid").asString());
                        }
                        time[0] += System.currentTimeMillis() - start;

                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            System.out.println("ByteBuffer AutoBean on " + path);
            System.out.println(bytes[0] + " bytes in " + (time[0] / 1000.0) + " seconds, " + bytes[0] / time[0] * 1000.0 / 1024.0 / 1024.0 + "mb/second");
            System.out.println();
        }

    }


    public void warmupGsonRaw() throws IOException {
        final JsonParser gson = new JsonParser();
        long start = System.currentTimeMillis();
        while (start + 1000 * WARMUP_SECONDS > System.currentTimeMillis()) {
            final List<String> guids = new ArrayList<>(4 * 5 * 20);
            for (Path path : files) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ByteBuffer bb = removeFastCpuPenalty(file);

                        String json = new String(bb.array());
                        JsonElement outer = gson.parse(json);
                        if (readGuids) {
                            guids.add(outer.getAsJsonObject().get("guid").getAsString());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }
    @Test
    public void testGsonRaw() throws IOException {
        warmupGsonRaw();
        final JsonParser gson = new JsonParser();

        for (Path path : files) {
            final double[] bytes = {0};
            final double[] time = {0};
            final List<String> guids = new ArrayList<>();
            while (time[0] < 1000) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                        ByteBuffer bb = removeFastCpuPenalty(file);
                        byte[] ba = bb.array();
                        bytes[0] += ba.length;
                        String json = new String(ba);
                        long start = System.currentTimeMillis();
                        JsonElement outer = gson.parse(json);
                        if (readGuids) {
                            guids.add(outer.getAsJsonObject().get("guid").getAsString());
                            guids.add(outer.getAsJsonObject().get("items").getAsJsonArray().get(4).getAsJsonObject().get("guid").getAsString());
                        }
                        time[0] += System.currentTimeMillis() - start;
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            System.out.println("Gson's JsonParser on " + path);
            System.out.println(bytes[0] + " bytes in " + (time[0] / 1000.0) + " seconds, " + bytes[0] / time[0] * 1000.0 / 1024.0 / 1024.0 + "mb/second");
            System.out.println();
        }
    }

    public void warmupGsonBean() throws IOException {
        final Gson gson = new GsonBuilder().create();
        long start = System.currentTimeMillis();
        while (start + 1000 * WARMUP_SECONDS > System.currentTimeMillis()) {
            final List<String> guids = new ArrayList<>(4 * 5 * 20);
            for (Path path : files) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ByteBuffer bb = removeFastCpuPenalty(file);
                        String json = new String(String.valueOf(StandardCharsets.UTF_8.decode(bb)));
                        ResultsBean results = gson.fromJson(json, ResultsBean.class);
                        if (readGuids) {
                            guids.add(results.getGuid());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }

    @Test
    public void testGsonBean() throws IOException {
        warmupGsonBean();
        final Gson gson = new GsonBuilder().create();

        for (Path path : files) {
            final double[] bytes = {0};
            final double[] time = {0};
            final List<String> guids = new ArrayList<>();
            while (time[0] < 1000) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ByteBuffer bb = removeFastCpuPenalty(file);

                        byte[] ba = bb.array();
                        bytes[0] += ba.length;
                        String json = new String(ba);
                        long start = System.currentTimeMillis();
                        ResultsBean results = gson.fromJson(json, ResultsBean.class);
                        if (readGuids) {
                            guids.add(results.getGuid());
                            guids.add(results.getItems().get(4).getGuid());
                        }
                        time[0] += System.currentTimeMillis() - start;
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            System.out.println("Gson.fromJson to bean on " + path);
            System.out.println(bytes[0] + " bytes in " + (time[0] / 1000.0) + " seconds, " + bytes[0] / time[0] * 1000.0 / 1024.0 / 1024.0 + "mb/second");
            System.out.println();
        }
    }

    public void warmupLazySplit() throws IOException {
        long start = System.currentTimeMillis();
        while (start + 1000 * WARMUP_SECONDS > System.currentTimeMillis()) {
            final List<String> guids = new ArrayList<>(4 * 5 * 20);
            for (Path path : files) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ByteBuffer bb = removeFastCpuPenalty(file);
                        ShallowSplittable.create(bb);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }

    @Test
    public void testLazySplit() throws IOException {
//        warmupLazySplit();

        for (Path path : files) {
            final double[] bytes = {0};
            final double[] time = {0};
            final List<String> guids = new ArrayList<>();
            while (time[0] < 1000) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        ByteBuffer bb = removeFastCpuPenalty(file);
                        bytes[0] += bb.limit();
                        long start = System.currentTimeMillis();
                        Splittable splittable = ShallowSplittable.create(bb);
                        if (readGuids) {
                            guids.add(splittable.get("guid").asString());
                            guids.add(splittable.get("items").get(4).get("guid").asString());
                        }
                        time[0] += System.currentTimeMillis() - start;

                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            System.out.println("ShallowSplittable on " + path);
            System.out.println(bytes[0] + " bytes in " + (time[0] / 1000.0) + " seconds, " + bytes[0] / time[0] * 1000.0 / 1024.0 / 1024.0 + "mb/second");
        }
    }
    final Map<Path,ByteBuffer> cache=new HashMap<>();

    static long ignored=new java.util.Random().nextInt()&0xffffffff;

    @Test
        public void testPlaceboSplit() throws IOException {
            for (Path path : files) {
                final double[] bytes = {0};
                final double[] time = {0};
                final List<String> guids = new ArrayList<>();
                while (time[0] < 1000) Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                        ByteBuffer bb = removeFastCpuPenalty(file);
                        bytes[0] += bb.limit();
                        long start = System.currentTimeMillis();

                        while (bb.hasRemaining() ) ignored+=bb.get()&0xff;
                        time[0] += System.currentTimeMillis() - start;
                        return FileVisitResult.CONTINUE;
                    }
                });
                System.out.println("Placebo on " + path);
                System.out.println(bytes[0] + " bytes in " + (time[0] / 1000.0) + " seconds, " + bytes[0] / time[0] * 1000.0 / 1024.0 / 1024.0 + "mb/second");
                System.out.println("unoptimizable sum: "+ignored);
            }

        }

    }
