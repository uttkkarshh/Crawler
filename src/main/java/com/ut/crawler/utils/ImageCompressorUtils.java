package com.ut.crawler.utils;


import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ImageCompressorUtils {

    /**
     * Compress a BufferedImage into a JPEG with given quality
     *
     * @param inputFile  original image file
     * @param outputFile destination file
     * @param quality    compression quality (0.0f = max compression, 1.0f = best quality)
     * @throws IOException if error during compression
     */
    public static void compressToJpeg(File inputFile, File outputFile, float quality) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);

        // Get a JPEG writer
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG writers found!");
        }
        ImageWriter writer = writers.next();

        // Set compression parameters
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality); // between 0f and 1f
        }

        // Write compressed image
        try (FileImageOutputStream output = new FileImageOutputStream(outputFile)) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    /**
     * Save image as WebP (needs additional WebP plugin: e.g. TwelveMonkeys ImageIO or Google libwebp JNI)
     */
    public static void compressToWebP(File inputFile, File outputFile, float quality) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);

        // ⚠ Requires WebP ImageIO plugin
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("webp");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No WebP writers found! Make sure WebP plugin is on classpath.");
        }
        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        try (FileImageOutputStream output = new FileImageOutputStream(outputFile)) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}
