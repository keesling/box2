/*
 * The MIT License
 *
 * Copyright 2014 BCL Technologies.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.OCR;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.spi.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Block;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.Page;
import com.google.cloud.vision.v1.Paragraph;
import com.google.cloud.vision.v1.Symbol;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.cloud.vision.v1.Word;
import com.google.protobuf.ByteString;
//import com.sun.istack.internal.logging.Logger;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import com.box.sdk.*;
import com.cete.dynamicpdf.merger.MergeDocument;
import com.cete.dynamicpdf.merger.PdfDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;


@SuppressWarnings("serial")
public class OCRServlet extends HttpServlet {

	private static final String USER_ID = "1814333970";
    private static final int MAX_DEPTH = 1;
    public static String message;
	
	@Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String boxFileId = req.getParameter("id");
		resp.setContentType("text/html");
		
              //start box upload
                //Logger.getLogger("com.box.sdk", null).setLevel(Level.OFF);
        		
    			//GoogleCredentials credential = GoogleCredentials.getApplicationDefault();
    			IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(500);
    			Reader reader = new FileReader("src/main/java/config2.json");
    			BoxConfig boxConfig = BoxConfig.readFrom(reader);
    			
    			//CREATE APP USER
    			//BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(
    	        //       boxConfig, accessTokenCache);
    			//System.out.println("connected");
    			//CreateUserParams params = new CreateUserParams();
    			//params.setSpaceAmount(1073741824);
    			//BoxUser.Info user = BoxUser.createAppUser(api, "cak2");
    			
    			//CONNECT TO APP USER
    			BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppUserConnection(USER_ID, boxConfig, accessTokenCache);
    			
    			System.out.println("connected");
    			
                //BoxAPIConnection api = new BoxAPIConnection("N7Wj1ESoyPBxrRdY2csVuFPmL3OCkcMR");
                //BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
                //System.out.format("user created",  "chadkeesling", user.getID());
               // System.out.format("Welcome, %s <%s>!\n\n", userInfo.getName(), userInfo.getLogin());
                
                //BoxFile file = new BoxFile(api, "180570771911");
                //BoxSharedLink.Permissions permissions = new BoxSharedLink.Permissions();
                //permissions.setCanDownload(true);
                //permissions.setCanPreview(true);
                //Date unshareDate = new Date();
                //BoxSharedLink sharedLink = file.createSharedLink (BoxSharedLink.Access.OPEN,null,permissions);
                //System.out.println(sharedLink.getDownloadURL());
                
               
                BoxFile file1 = new BoxFile(api,"180803441500");
                BoxFile.Info info1 = file1.getInfo();
                System.out.println("got file1");
                                
                ByteArrayOutputStream outStream1 = new ByteArrayOutputStream();
                file1.download(outStream1);
                System.out.println("downloaded file1");
                
                                
                byte[] b1 = outStream1.toByteArray();
                ByteString imgBytes = ByteString.copyFrom(b1);
                System.out.println("wrote file1 to byte1");
                
                List<AnnotateImageRequest> requests = new ArrayList<>();
                //ImageSource imgSource = ImageSource.newBuilder().setImageUri(sharedLink.getDownloadURL()).build();
                //ImageSource imgSource = ImageSource.newBuilder().setImageUri("https://ck-demo.box.com/shared/static/knxiu1vvvwjzssjkbx5q8spy84q0kc42.jpg").build();
                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
                AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
                requests.add(request);

                BatchAnnotateImagesResponse response =
                    ImageAnnotatorClient.create().batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                  if (res.hasError()) {
                    System.out.printf("Error: %s\n", res.getError().getMessage());
                    return;
                  }
                  // For full list of available annotations, see http://g.co/cloud/vision/docs
                  TextAnnotation annotation = res.getFullTextAnnotation();
                  for (Page page: annotation.getPagesList()) {
                    String pageText = "";
                    for (Block block : page.getBlocksList()) {
                      String blockText = "";
                      for (Paragraph para : block.getParagraphsList()) {
                        String paraText = "";
                        for (Word word: para.getWordsList()) {
                          String wordText = "";
                          for (Symbol symbol: word.getSymbolsList()) {
                            wordText = wordText + symbol.getText();
                          }
                          paraText = paraText + wordText;
                        }
                        // Output Example using Paragraph:
                        //System.out.println("Paragraph: \n" + paraText);
                        //System.out.println("Bounds: \n" + para.getBoundingBox() + "\n");
                        blockText = blockText + paraText;
                      }
                      pageText = pageText + blockText;
                    }
                  }
                  System.out.println(annotation.getText());
                  
                  Metadata metadata = new Metadata().add("/Text", annotation.getText());
                  file1.createMetadata(metadata);
                }
                                            
                //InputStream inputStream = new ByteArrayInputStream(b1);
                //BoxFolder rootFolder = BoxFolder.getRootFolder(api);
                //rootFolder.uploadFile(inputStream, "mergedpdf.pdf");
                //System.out.println("uploaded file");
                //outStream1.close();
                //inputStream.close();
                //end box upload
                
        }
    }

