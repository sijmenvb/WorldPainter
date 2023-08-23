/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.tools;

import org.pepsoft.util.FileUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.exporting.JavaWorldExporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.SecureRandom;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import static org.pepsoft.worldpainter.Dimension.Anchor.NORMAL_DETAIL;
import static org.pepsoft.worldpainter.exporting.WorldExportSettings.EXPORT_EVERYTHING;

/**
 *
 * @author pepijn
 */
public class Timings {
    public static void main(String[] args) throws IOException, ProgressReceiver.OperationCancelled, ClassNotFoundException {
        Random random = new SecureRandom();
//        final Configuration defaultConfig = new Configuration();
        final World2 world;
        try (ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(args[0])))) {
            world = (World2) in.readObject();
        }
        long totalDuration = 0;
        for (int i = 0; i < 5; i++) {
//            final World2 world = WorldFactory.createDefaultWorld(defaultConfig, random.nextLong());
            world.getDimension(NORMAL_DETAIL).getTileFactory().setSeed(random.nextLong());
            final JavaWorldExporter exporter = new JavaWorldExporter(world, EXPORT_EVERYTHING);
            System.out.println("Starting export of world " + world.getName() + " " + i + " (seed: " + world.getDimension(NORMAL_DETAIL).getSeed() + ")");
            File baseDir = new File(System.getProperty("user.dir"));
            String name = world.getName() + ' ' + i;
            File worldDir = new File(baseDir, FileUtils.sanitiseName(name));
            if (worldDir.isDirectory()) {
                FileUtils.deleteDir(worldDir);
            }
            long start = System.currentTimeMillis();
            exporter.export(baseDir, name, null, null);
            long duration = System.currentTimeMillis() - start;
            System.out.println("Exporting world took " + (duration / 1000f) + " s");
            totalDuration += duration;
        }
        System.out.println("Average duration: " + (totalDuration / 5000f) + " s");
    }
}