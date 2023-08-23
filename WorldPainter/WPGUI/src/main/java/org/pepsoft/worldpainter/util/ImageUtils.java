/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.pepsoft.worldpainter.util;

import com.google.common.collect.ImmutableSet;
import org.pepsoft.util.DesktopUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.pepsoft.util.swing.MessageUtils.beepAndShowError;

/**
 * GUI utility methods for working with images.
 *
 * @author pepijn
 */
public final class ImageUtils {
    private ImageUtils() {
        // Prevent instantiation
    }
    
    public static BufferedImage loadImage(Component parent, File file) {
        if (file.isFile() && file.canRead()) {
            logger.info("Loading image");
            try {
                return ImageIO.read(file);
            } catch (IOException e) {
                logger.error("I/O error while loading image " + file, e);
                beepAndShowError(parent, "An error occurred while loading the image.\nIt may not be a valid or supported image file, or the file may be corrupted.", "Error Loading Image");
            } catch (RuntimeException | Error e) {
                logger.error(e.getClass().getSimpleName() + " while loading image " + file, e);
                beepAndShowError(parent, "An error occurred while loading the image.\nThere may not be enough available memory, or the image may be too large.", "Error Loading Image");
            }
        }
        return null;
    }

    public static File selectImageForOpen(Window parent, String imageType, File selectedFile) {
        return FileUtils.selectFileForOpen(parent, "Select " + imageType, ((selectedFile != null) && selectedFile.exists()) ? selectedFile : DesktopUtils.getPicturesFolder(), getFileFilter());
    }

    public static File selectImageForSave(Window parent, String imageType, File selectedFile) {
        return FileUtils.selectFileForSave(parent, "Export as " + imageType, ((selectedFile != null) && selectedFile.exists()) ? selectedFile : DesktopUtils.getPicturesFolder(), getFileFilter());
    }

    private static FileFilter getFileFilter() {
        final Set<String> extensions = ImmutableSet.copyOf(ImageIO.getReaderFileSuffixes());
        final StringBuilder sb = new StringBuilder("Supported image formats (");
        boolean first = true;
        for (String extension: extensions) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append("*.");
            sb.append(extension);
        }
        sb.append(')');
        final String description = sb.toString();
        return new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String filename = f.getName();
                int p = filename.lastIndexOf('.');
                if (p != -1) {
                    String extension = filename.substring(p + 1).toLowerCase();
                    return extensions.contains(extension);
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public String getExtensions() {
                return String.join(";", extensions);
            }
        };
    }

    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);
}