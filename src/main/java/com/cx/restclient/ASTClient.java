package com.cx.restclient;

import com.cx.restclient.ast.dto.Upload;
import com.cx.restclient.common.DependencyScanner;
import com.cx.restclient.configuration.CxScanConfig;
import com.cx.restclient.dto.DependencyScanResults;
import com.cx.restclient.dto.LoginSettings;
import com.cx.restclient.dto.PathFilter;
import com.cx.restclient.exception.CxClientException;
import com.cx.restclient.httpClient.CxHttpClient;
import com.cx.restclient.osa.dto.ClientType;
import com.cx.restclient.sast.utils.zip.CxZipUtils;
import com.cx.restclient.sca.dto.SCAConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class ASTClient implements DependencyScanner {


    private static class UrlPaths {
        private static final String RISK_MANAGEMENT_API = "/risk-management/";
        private static final String PROJECTS = RISK_MANAGEMENT_API + "projects";
        private static final String SUMMARY_REPORT = RISK_MANAGEMENT_API + "riskReports/%s/summary";
        private static final String SCAN_STATUS = RISK_MANAGEMENT_API + "scans/%s/status";
        private static final String REPORT_ID = RISK_MANAGEMENT_API + "scans/%s/riskReportId";

        private static final String ZIP_UPLOAD = "/scan-runner/scans/zip";
        private static final String REQUEST_ZIP_UPLOAD_URL = "/uploads";

        private static final String WEB_REPORT = "/#/projects/%s/reports/%s";
    }

    private final Logger log;
    private final CxScanConfig config;

    private String projectId;
    private final CxHttpClient httpClient;

    private SCAClient scaClient;
    private String scanId;

    public ASTClient(CxScanConfig config, Logger log) {
        scaClient = new SCAClient(config, log);
        // need to share the same http client with scaClient
        this.log = log;
        this.config = config;

        //TODO: this is temp code. Make sure the same http client is used in scaClient and here
        SCAConfig scaConfig = config.getScaConfig();
        httpClient = new CxHttpClient(scaConfig.getApiUrl(),
                config.getCxOrigin(),
                config.isDisableCertificateValidation(),
                config.isUseSSOLogin(),
                null,
                config.getProxyConfig(),
                log);

    }

    @Override
    public void init() throws CxClientException {
        scaClient.init();
    }

    @Override
    public String createScan(DependencyScanResults target) throws CxClientException {
        log.info("----------------------------------- Create CxAST Scan:------------------------------------");

        PathFilter filter = new PathFilter(config.getOsaFolderExclusions(), config.getOsaFilterPattern(), log);
        scanId = null;
        File zipFile = null;
        try {
            String sourceDir = config.getEffectiveSourceDirForDependencyScan();
            zipFile = CxZipUtils.getZippedSources(config, filter, sourceDir, log);

            //1. POST /api/uploads  - get upload zip url
            //2. PUT <url> - upload zip to the url
            scanId = uploadZipFile(zipFile);

            //3. POST /api/scans/  - create and start scan, return a scan id
            //TODO


        } catch (IOException e) {
            throw new CxClientException("Error creating CxSCA scan.", e);
        }
        finally {
            // cleanup - delete the zipped file
            if(zipFile != null) {
                CxZipUtils.deleteZippedSources(zipFile, config, log);
            }
        }

        return scanId;
    }

    @Override
    public void waitForScanResults(DependencyScanResults target) throws CxClientException {
        scaClient.waitForScanResults(target);
    }

    @Override
    public DependencyScanResults getLatestScanResults() throws CxClientException {
        return scaClient.getLatestScanResults();
        // TODO: ask Alexey why it is not implemented in scaClient
    }

     void testConnection() throws IOException, CxClientException {
        // The calls below allow to check both access control and API connectivity.
        login();
        //getProjects(); //TODO: uncomment when implemented by AST Client
    }

    private void login() throws IOException, CxClientException {
        log.info("Logging into CxSCA by AST client");
        SCAConfig scaConfig = config.getScaConfig();

        LoginSettings settings = new LoginSettings();
        settings.setAccessControlBaseUrl(scaConfig.getAccessControlUrl());
        settings.setUsername(scaConfig.getUsername());
        settings.setPassword(scaConfig.getPassword());
        settings.setTenant(scaConfig.getTenant());
        settings.setClientTypeForPasswordAuth(ClientType.SCA_CLI);

        httpClient.login(settings);
    }

    private String uploadZipFile(File zipFile) throws IOException, CxClientException {
        log.info("Uploading zipped sources.");

        String url = getPreSignedUrl(zipFile);

        //2. upload the file to the URL
        uploadToUrl(zipFile, url);

        return scanId;
    }

    private String getPreSignedUrl(File zipFile) throws IOException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        InputStream input = new FileInputStream(zipFile.getAbsoluteFile());
        InputStreamBody fileBody = new InputStreamBody(input, ContentType.APPLICATION_OCTET_STREAM, "zippedSource");
        builder.addPart("zipFile", fileBody);

        ContentBody projectIdBody = new StringBody(projectId, ContentType.APPLICATION_FORM_URLENCODED);
        builder.addPart("projectId", projectIdBody);

        HttpEntity entity = builder.build();

        //1. get pre signed URL
        Upload upload = httpClient.postRequest(UrlPaths.REQUEST_ZIP_UPLOAD_URL,
                                                ContentType.MULTIPART_FORM_DATA.toString(),
                                                entity,
                                                Upload.class,
                                                HttpStatus.SC_CREATED,
                                                "upload ZIP file");

        String url = upload.getURL();
        // TODO: add error handling for upload

        log.debug("Upload URL received: {}", url);
        return url;
    }

    private void uploadToUrl(File zipFile, String url) throws IOException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        InputStream input = new FileInputStream(zipFile.getAbsoluteFile());
        InputStreamBody fileBody = new InputStreamBody(input, org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM, "zippedSource");
        builder.addPart("zipFile", fileBody);

        ContentBody projectIdBody = new StringBody(projectId, org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED);
        builder.addPart("projectId", projectIdBody);
        HttpEntity entity = builder.build();

        httpClient.putRequestWithAbsoluteUrl(url,
                ContentType.APPLICATION_OCTET_STREAM.toString(),
                entity,
                String.class,
                HttpStatus.SC_OK,
                "upload ZIP file to pre signed url");

    }
}
