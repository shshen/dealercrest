package com.dealercrest.rest;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONObject;

import com.dealercrest.rest.http.BytesBody;
import com.dealercrest.rest.http.FileUploads;
import com.dealercrest.rest.http.HttpResult;
import com.dealercrest.rest.http.JsonResult;
import com.dealercrest.rest.http.NettyRequest;
import com.dealercrest.router.Handler;
import com.dealercrest.router.MatchResult;
import com.dealercrest.router.PathRouter;
import com.dealercrest.router.RouteInfo;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NettyRouters extends PathRouter {

    private static final Logger logger = Logger.getLogger(NettyRouters.class.getName());

    public HttpResult handle(HttpRequest req, Map<String,Object> attributes) {
        return handle(new RequestContext(req, attributes));
    }

    public HttpResult handle(RequestContext hContext) {
        HttpRequest req = hContext.getRequest();
        String requestPath = req.uri();
        int idx = requestPath.indexOf("?");
        if (idx > 0) {
            requestPath = requestPath.substring(0, idx);
        }
        String methodName = req.method().name().toUpperCase();
        MatchResult matchResult = match(methodName, requestPath);
        if (matchResult != null) {
            RouteInfo handler = matchResult.getRouteInfo();
            Map<String, String> pathParams = matchResult.getParamValues();
            return handleMethod(handler, hContext, pathParams);
        }
        return new JsonResult(new JSONObject().put("code", 404).put("error", requestPath + " is not found"));
    }

    private HttpResult handleMethod(RouteInfo routeInfo, RequestContext httpContext, Map<String, String> pathParams) {
        if (!routeInfo.isRoleAllowed(httpContext)) {
            JSONObject o = new JSONObject().put("code", 401).put("message", "no permission");
            return new JsonResult(o);
        }

        HttpRequest request = httpContext.getRequest();
        Handler httpHandler = routeInfo.getHttpHandler();
        List<ParamEntry> paramList = httpHandler.getParams();
        Object[] argArray = buildArguments(paramList, pathParams, httpContext);
        try {
            return httpHandler.invoke(argArray);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            String message = e.getMessage();
            if (cause != null) {
                message = e.getCause().getMessage();
                if (!(cause instanceof IllegalArgumentException)
                        && (!(cause instanceof StatusException))) {
                    logger.log(Level.SEVERE, "failed to handle " + request.uri(), e);
                }
            }
            int code = 400;
            if (cause instanceof StatusException) {
                StatusException ie = (StatusException) cause;
                code = ie.getCode();
            }
            if (message == null || message.isBlank()) {
                message = "unknown error happens";
            }

            JSONObject obj = new JSONObject();
            obj.put("code", code);
            obj.put("message", message);
            return new JsonResult(obj);
        }
    }

    private Object[] buildArguments(List<ParamEntry> pList, Map<String, String> pathParams, RequestContext context) {
        // List<ParamEntry> pList = routeInfo.getHandlerInfo().getParams();
        List<Object> pValues = new ArrayList<>();
        HttpRequest req = context.getRequest();
        Map<String, List<String>> paramMap = buildParamMap(req);

        for (ParamEntry pe : pList) {
            Annotation p = pe.getParam();
            Object o = null;
            if (p instanceof QueryParam) {
                QueryParam qp = (QueryParam) p;
                List<String> values = paramMap.get(qp.value());
                o = convert(values, pe.getClazz());
            } else if (p instanceof PathParam) {
                PathParam pp = (PathParam) p;
                o = getPathParam(pathParams, pp.value(), pe.getClazz());
            } else if (p instanceof PostParam) {
                o = getPostBody(req);
            } else if (p instanceof MapParam) {
                String queryString = req.uri();
                queryString = queryString.substring(queryString.indexOf("?") + 1);
                o = new MultiValueMap(queryString);
            } else if (p instanceof HeaderParam) {
                HeaderParam hp = (HeaderParam) p;
                o = req.headers().get(hp.value());
            } else if (p instanceof FormParam && req instanceof NettyRequest) {
                NettyRequest nettyRequest = (NettyRequest) req;
                FileUploads fileuploads = (FileUploads)nettyRequest.body();
                o = fileuploads.multipart();
            } else if(p instanceof ContextParam) {
                ContextParam cp = (ContextParam) p;
                o = context.getAttribute(cp.value());
            } else if(p instanceof BodyStream && req instanceof NettyRequest) {
                NettyRequest nettyRequest = (NettyRequest) req;
                BytesBody body = (BytesBody) nettyRequest.body();
                o = body.getInputStream();
            }
            pValues.add(o);
        }
        return pValues.toArray();
    }

    private String getPostBody(HttpRequest request) {
        if (request instanceof HttpContent) {
            HttpContent content = (HttpContent) request;
            return content.content().toString(StandardCharsets.UTF_8);
        } else if ( request instanceof NettyRequest) {
            NettyRequest nettyRequest = (NettyRequest) request;
            BytesBody bytesBody = (BytesBody) nettyRequest.body();
            if (bytesBody != null) {
                return bytesBody.asString();
            }
        }
        return "";
    }

    private Map<String, List<String>> buildParamMap(HttpRequest req) {
        Map<String, List<String>> paramMap = new HashMap<String, List<String>>();
    
        // 1️ Form data: application/x-www-form-urlencoded
        String contentType = req.headers().get("Content-Type");
        if (contentType != null && contentType.contains("application/x-www-form-urlencoded")
                && req instanceof HttpContent) {
            HttpContent content = (HttpContent) req;
            String formData = content.content().toString(StandardCharsets.UTF_8);
            QueryStringDecoder decoder = new QueryStringDecoder("?" + formData, false);
            for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
                String key = entry.getKey();
                List<String> list = entry.getValue();
                List<String> targetList = paramMap.get(key);
                if (targetList == null) {
                    targetList = new ArrayList<String>();
                    paramMap.put(key, targetList);
                }
                targetList.addAll(list);
            }
        }
    
        // 2️ URI query parameters
        QueryStringDecoder queryDecoder = new QueryStringDecoder(req.uri());
        for (Map.Entry<String, List<String>> entry : queryDecoder.parameters().entrySet()) {
            String key = entry.getKey();
            List<String> list = entry.getValue();
            List<String> targetList = paramMap.get(key);
            if (targetList == null) {
                targetList = new ArrayList<String>();
                paramMap.put(key, targetList);
            }
            targetList.addAll(list);
        }
    
        // 3️ Headers
        for (Map.Entry<String, String> header : req.headers()) {
            String key = header.getKey();
            String value = header.getValue();
            List<String> targetList = paramMap.get(key);
            if (targetList == null) {
                targetList = new ArrayList<String>();
                paramMap.put(key, targetList);
            }
            targetList.add(value);
        }
    
        return paramMap;
    }
}
