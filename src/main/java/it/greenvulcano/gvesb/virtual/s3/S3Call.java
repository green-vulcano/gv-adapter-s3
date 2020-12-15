/*******************************************************************************
 * Copyright (c) 2009, 2020 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.virtual.s3;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Node;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3Call implements CallOperation {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(S3Call.class);
    
    private OperationKey key = null;
    private String akid;
    private String skid;
    private String dc;
    private String operation;
    private String bucket;
    
    protected Object response;
    
    private AmazonS3 s3 = null;
    
    @Override
    public void init(Node node) throws InitializationException {

        logger.debug("Initializing s3-call...");

        try {
        	
            akid = XMLConfig.get(node, "@akid");
            skid = XMLConfig.get(node, "@skid");
            dc = XMLConfig.get(node, "@region");
            operation = XMLConfig.get(node, "@action");
            bucket = XMLConfig.get(node, "@bucket");
            
            BasicAWSCredentials credentials = new BasicAWSCredentials(akid, skid);
			s3 = AmazonS3ClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(credentials))
					.withRegion(dc)
					.build();
            logger.debug("Configured S3 {} operation ", operation);

        } catch (Exception e) {

            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][] { { "message", e.getMessage() } }, e);

        }

    }

    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException {

        try {
        	
        	logger.debug("Starting S3 call: " + operation);
        	
        	if("list".equals(operation)) {
        		
        		logger.debug("Listing files in the " + bucket + " bucket");
        		logger.debug("Looking in the directory: " + gvBuffer.getProperty("S3_PREFIX"));
        		
				//Example (official): https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/java/example_code/s3/src/main/java/aws/example/s3/ListObjects.java
				String prefix = "", delimiter = "/";
				
				if(gvBuffer.getProperty("S3_PREFIX") != "NULL") {
					prefix = gvBuffer.getProperty("S3_PREFIX");
				}
				
				if(gvBuffer.getProperty("S3_DELIMITER") != "NULL") {
					delimiter = gvBuffer.getProperty("S3_DELIMITER");
				}
				
				ListObjectsV2Request req = new ListObjectsV2Request();
				req.setBucketName(bucket);
				req.setPrefix(prefix);
				req.setDelimiter(delimiter);
				
				ListObjectsV2Result result = s3.listObjectsV2(req);
				
				JSONObject json = new JSONObject();
				
				json.put("name", result.getBucketName());
				json.put("prefix", result.getPrefix());
				json.put("delimiter", result.getDelimiter());
				
				JSONArray objects = new JSONArray(result.getObjectSummaries().toArray());
				JSONArray directories = new JSONArray(result.getCommonPrefixes().toArray());
				
				json.put("files", objects);
				json.put("directories", directories);
				
				gvBuffer.setObject(json.toString(1));
				
				logger.debug("List received from the " + bucket + " bucket");
                
			} else if ("put".equals(operation)) {
				
				logger.debug("Sending " + gvBuffer.getProperty("S3_FILE_NAME") + " to the " + bucket + " bucket");
				
				//Example (official): https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/java/example_code/s3/src/main/java/aws/example/s3/PutObject.java
				InputStream object = new ByteArrayInputStream((byte[]) gvBuffer.getObject());
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(object.available());
				
				PutObjectResult result = s3.putObject(bucket, gvBuffer.getProperty("S3_FILE_NAME"), object, metadata);
				
				gvBuffer.setObject(result.getVersionId());
				
				logger.debug("File " + gvBuffer.getProperty("S3_FILE_NAME") + " sent to the " + bucket + " bucket");
				
			} else if ("get".equals(operation)) {
				
				logger.debug("Getting the " + gvBuffer.getProperty("S3_FILE_NAME") + " object from the " + bucket + " bucket");
				
				//Example (official): https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/java/example_code/s3/src/main/java/aws/example/s3/GetObject.java
				S3Object o = s3.getObject(bucket, gvBuffer.getProperty("S3_FILE_NAME"));
	            S3ObjectInputStream s3is = o.getObjectContent();
	            
				gvBuffer.setObject(s3is.readAllBytes());
				
                s3is.close();
                
                logger.debug("Object " + gvBuffer.getProperty("S3_FILE_NAME") + " received from the " + bucket + " bucket");
				
			} else if ("delete".equals(operation)) {
				
				logger.debug("Deleting the " + gvBuffer.getProperty("S3_FILE_NAME") + " object from the " + bucket + " bucket");
				
				//Example (official): https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/java/example_code/s3/src/main/java/aws/example/s3/DeleteObject.java
				s3.deleteObject(bucket, gvBuffer.getProperty("S3_FILE_NAME"));
				
				gvBuffer.setObject("Object Deleted");
				
				logger.debug("Object " + gvBuffer.getProperty("S3_FILE_NAME") + " deleted from the " + bucket + " bucket");
				
			}
        	
        	logger.debug("End S3 call: " + operation);

        } catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR",
                                    new String[][] { { "service", gvBuffer.getService() },
                                                     { "system", gvBuffer.getSystem() },
                                                     { "tid", gvBuffer.getId().toString() },
                                                     { "message", exc.getMessage() } },
                                    exc);
        }
        return gvBuffer;
    }
    
    @Override
    public void cleanUp() {

        // do nothing
    }

    @Override
    public void destroy() {

        // do nothing
    }

    @Override
    public void setKey(OperationKey operationKey) {

        this.key = operationKey;
    }

    @Override
    public OperationKey getKey() {

        return key;
    }

    @Override
    public String getServiceAlias(GVBuffer gvBuffer) {

        return gvBuffer.getService();
    }

}
