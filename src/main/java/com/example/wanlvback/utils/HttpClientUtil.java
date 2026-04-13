package com.example.wanlvback.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类
 */
public class HttpClientUtil {

    static final int DEFAULT_TIMEOUT_MSEC = 600 * 1000;

    /**
     * 发送GET方式请求
     * @param url 请求地址
     * @param paramMap 请求参数
     * @return 响应结果
     */
    public static String doGet(String url, Map<String, String> paramMap) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String result = "";

        try {
            URIBuilder builder = new URIBuilder(url);
            if (paramMap != null) {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    builder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            URI uri = builder.build();
            HttpGet httpGet = new HttpGet(uri);
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(response, httpClient);
        }

        return result;
    }

    /**
     * 发送POST方式请求
     * @param url 请求地址
     * @param paramMap 请求参数
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            HttpPost httpPost = new HttpPost(url);
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8));
            }

            httpPost.setConfig(builderRequestConfig(DEFAULT_TIMEOUT_MSEC, DEFAULT_TIMEOUT_MSEC, DEFAULT_TIMEOUT_MSEC));
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw e;
        } finally {
            closeQuietly(response, httpClient);
        }

        return resultString;
    }

    /**
     * 发送POST JSON方式请求
     * @param url 请求地址
     * @param paramMap 请求参数
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        JSONObject jsonObject = new JSONObject();
        if (paramMap != null) {
            for (Map.Entry<String, String> param : paramMap.entrySet()) {
                jsonObject.put(param.getKey(), param.getValue());
            }
        }
        return doPost4Json(url, jsonObject.toJSONString());
    }

    /**
     * 发送POST JSON方式请求
     * @param url 请求地址
     * @param jsonBody JSON请求体
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String doPost4Json(String url, String jsonBody) throws IOException {
        return doPost4Json(url, jsonBody, DEFAULT_TIMEOUT_MSEC, DEFAULT_TIMEOUT_MSEC);
    }

    /**
     * 发送POST JSON方式请求，并允许调用方覆盖超时
     * @param url 请求地址
     * @param jsonBody JSON请求体
     * @param connectTimeoutMsec 连接超时毫秒数
     * @param socketTimeoutMsec 读取超时毫秒数
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String doPost4Json(String url, String jsonBody, int connectTimeoutMsec, int socketTimeoutMsec) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            HttpPost httpPost = new HttpPost(url);
            if (jsonBody != null) {
                StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
                entity.setContentEncoding(StandardCharsets.UTF_8.name());
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig(connectTimeoutMsec, connectTimeoutMsec, socketTimeoutMsec));
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw e;
        } finally {
            closeQuietly(response, httpClient);
        }

        return resultString;
    }

    private static RequestConfig builderRequestConfig(int connectTimeoutMsec, int connectionRequestTimeoutMsec, int socketTimeoutMsec) {
        return RequestConfig.custom()
                .setConnectTimeout(connectTimeoutMsec)
                .setConnectionRequestTimeout(connectionRequestTimeoutMsec)
                .setSocketTimeout(socketTimeoutMsec)
                .build();
    }

    private static void closeQuietly(CloseableHttpResponse response, CloseableHttpClient httpClient) {
        try {
            if (response != null) {
                response.close();
            }
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
