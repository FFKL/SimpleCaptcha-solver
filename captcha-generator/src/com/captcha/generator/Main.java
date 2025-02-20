package com.captcha.generator;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import nl.captcha.Captcha;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;
import nl.captcha.gimpy.RippleGimpyRenderer;
import nl.captcha.text.renderer.ColoredEdgesWordRenderer;

public class Main {
    private static final String OUTPUT_DIR = "./captchas";
    private static final String SAMPLE_DIR = "./sample";
    private static final String CSV_FILE_PATH = "./captchas.csv";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: <clean | generate <num> | sample <num>>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "clean":
                cleanDirectory(OUTPUT_DIR);
                break;
            case "generate":
                if (args.length < 2) {
                    System.out.println("Please provide the number of captchas to generate");
                    return;
                }
                try {
                    int numCaptchas = Integer.parseInt(args[1]);
                    generateCaptchas(numCaptchas, OUTPUT_DIR, CSV_FILE_PATH);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number of captchas: " + args[1]);
                }
                break;
            case "sample":
                if (args.length < 2) {
                    System.out.println("Please provide the number of samples to generate");
                    return;
                }
                try {
                    int numSamples = Integer.parseInt(args[1]);
                    cleanDirectory(SAMPLE_DIR);
                    generateCaptchas(numSamples, SAMPLE_DIR, null);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number of samples: " + args[1]);
                }
                break;
            default:
                System.out.println("Unknown command: " + args[0]);
        }
    }

    private static void cleanDirectory(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            System.out.println("Directory does not exist: " + dirPath);
            return;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("Directory is already clean: " + dirPath);
            return;
        }

        for (File file : files) {
            if (!file.delete()) {
                System.err.println("Failed to delete: " + file.getName());
            }
        }

        System.out.println("Cleaned directory: " + dirPath);
    }

    private static void generateCaptchas(int numCaptchas, String outputDir, String csvFilePath) {
        new File(outputDir).mkdirs(); // Ensure output directory exists
        BufferedWriter writer = null;
        try {
            if (csvFilePath != null) {
                writer = new BufferedWriter(new FileWriter(csvFilePath));
                writer.write("uniq_id,captcha_answer\n");
            }
            List<Color> colorsList = Arrays.asList(Color.BLACK, Color.BLUE);
            FontLoader fontLoader = new FontLoader();
            List<Font> baseFontsList = fontLoader.loadFontsFromDirectory("./fonts", 50f);
            List<Font> fontsList = new ArrayList<Font>();
            float[] sizes = { 45f, 50f, 55f, 60f };
            for (Font font : baseFontsList) {
                for (float size : sizes) {
                    fontsList.add(font.deriveFont(size));
                }
            }

            for (int i = 0; i < numCaptchas; i++) {
                String uniqId = UUID.randomUUID().toString();
                String fileName = outputDir + "/" + uniqId + ".png";

                Random random = new Random();
                float strokeWidth = 0.1f + (random.nextInt(7) * 0.1f);

                Captcha captcha = new Captcha.Builder(200, 50)
                        .addText(new ColoredEdgesWordRenderer(
                                colorsList,
                                fontsList.isEmpty() ? null : fontsList,
                                strokeWidth))
                        .gimp(new RippleGimpyRenderer())
                        .addBackground(new GradiatedBackgroundProducer())
                        .addNoise()
                        .build();

                ImageIO.write(captcha.getImage(), "PNG", new File(fileName));

                if (writer != null) {
                    writer.write(uniqId + "," + captcha.getAnswer() + "\n");
                }

                System.out.println((i + 1) + " | Generated: " + fileName + " | Answer: " + captcha.getAnswer());
            }

            if (writer != null) {
                writer.close();
                System.out.println("Captcha generation completed. CSV saved at: " + csvFilePath);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
