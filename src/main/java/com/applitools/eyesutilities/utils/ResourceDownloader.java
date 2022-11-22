package com.applitools.eyesutilities.utils;

import org.apache.http.client.methods.CloseableHttpResponse;

import com.applitools.eyesutilities.obj.contexts.ResultsAPIContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ResourceDownloader {

    private final ResultsAPIContext context;

    public ResourceDownloader(ResultsAPIContext context) {
        this.context = context;
    }

    public BufferedImage getImage(URL url) throws InterruptedException, IOException {
        return getImage(url.toString());
    }

    public BufferedImage getImage(String url) throws InterruptedException, IOException {
        try (CloseableHttpResponse response = ApiCallHandler.sendGetRequest(url, context)) {
            try (InputStream is = response.getEntity().getContent()) {
                BufferedImage image = ImageIO.read(is);
                if (null != image) {
                    return image;
                } else {
                    System.out.println("Error message: Failed to download image");
                }
            }
        }
        return null;
    }
}
