package com.captcha.generator;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class FontLoader {
  public List<Font> loadFontsFromDirectory(String dirPath, float size) throws IOException, FontFormatException {
    List<Font> fonts = new ArrayList<>();
    File fontDir = new File(dirPath);

    if (!fontDir.exists() || !fontDir.isDirectory()) {
      System.err.println("Font directory does not exist: " + dirPath);
      return fonts;
    }

    File[] fontFiles = fontDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf"));

    if (fontFiles == null || fontFiles.length == 0) {
      System.err.println("No .ttf fonts found in directory: " + dirPath);
      return fonts;
    }

    return this.readFontFiles(fontFiles, size);
  }

  private List<Font> readFontFiles(File[] fontFiles, float size) throws IOException, FontFormatException {
    List<Font> fonts = new ArrayList<>();
    for (File fontFile : fontFiles) {
      try (InputStream is = Files.newInputStream(fontFile.toPath())) {
        Font font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        fonts.add(font);
      }
    }
    return fonts;
  }
}
