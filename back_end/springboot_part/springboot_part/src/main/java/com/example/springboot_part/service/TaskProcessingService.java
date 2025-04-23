package com.example.springboot_part.service;

import com.example.springboot_part.mapper.*;
import com.example.springboot_part.pojo.Photos;
import com.example.springboot_part.pojo.Tasks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class TaskProcessingService {

    @Value("${app.BaseURL}")
    private String baseUrl;

    @Autowired
    private RedisService redisService;
    @Autowired
    private PhotosMapper photosMapper;
    @Autowired
    private TasksMapper tasksMapper;
    @Autowired
    private TasksService tasksService;
    @Autowired
    private PhotosService photosService;
    @Autowired
    private GeneratedPhotosService generatedPhotosService;
    @Autowired
    private CompositePhotosService compositePhotosService;
    @Autowired
    private CroppedFacesMapper croppedFacesMapper;
    @Autowired
    private CompositePhotosMapper compositePhotosMapper;
    @Autowired
    private GeneratedPhotosMapper generatedPhotosMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean isRunning = true; // 增加运行状态控制
    private AtomicInteger completedTasks;
    private int totalTasks = 0; // 总任务数
    private String final_photos;
    private String message;

    // 线程池：支持动态扩缩容
    private ThreadPoolExecutor executorService;
//    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
//            2, 10, 60L, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<>(100),
//            Executors.defaultThreadFactory(),
//            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
//    );

    // 任务处理入口
    public Map<String, Object> startTaskProcessing() {
        executorService = new ThreadPoolExecutor(
                2, 10, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        completedTasks = new AtomicInteger(0);
        totalTasks = redisService.getTaskQueueSize();
        long startTime = System.currentTimeMillis();
        long maxWaitTime = 180L * 1000 * totalTasks; // 最长运行时间 60 秒，防止无限循环
        isRunning = true;

        while (isRunning) {
            int taskId = redisService.popTaskFromQueue();
            // 任务队列为空，等待 200ms 并检查是否超时
            if (taskId == -1) {
                if (System.currentTimeMillis() - startTime > maxWaitTime) {
                    System.out.println("任务队列长时间为空，自动退出");
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }
            startTime = System.currentTimeMillis(); // 取到任务，重置计时
            executorService.submit(() -> processTaskQueue(taskId));
        }

        // 任务完成后返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("path_url", final_photos);
        return result;
    }



    public void processTaskQueue(int taskId) {
        System.out.println(Thread.currentThread().getName() + " -> 处理任务 ID: " + taskId);
        Map<Object, Object> taskData = readRedis(taskId);

        if (taskData == null || taskData.isEmpty()) {
            System.out.println("任务数据为空，跳过任务 ID: " + taskId);
            return;
        }

        Tasks task = tasksMapper.findById(taskId);
        int photoId = (int) taskData.get("PhotoId");
        Photos photo = redisService.getPhoto(photoId);
        if (photo == null) {
            photo = photosMapper.findById(photoId);
            redisService.savePhoto(photo);
        }

        String obsUrl = photo.getPhotoPath();
        String taskModel = taskData.get("TaskModel").toString();
        String taskType = taskData.get("TaskType").toString();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("photoUrl", obsUrl);
        requestBody.put("taskId", taskId);
        String fastApiUrl = baseUrl + (taskModel.equals("Automatic") ? "/auto" : "/manual");
        if(taskType.equals("SuperResolution")){
            fastApiUrl = baseUrl + "/SR";
            requestBody.put("photoId", photoId);
        }
        else {
            requestBody.put("taskType", taskType);
            requestBody.put("crop", Map.of(
                    "x", (double) taskData.get("x"),
                    "y", (double) taskData.get("y"),
                    "width", (double) taskData.get("width"),
                    "height", (double) taskData.get("height")
            ));
            if ("Manual".equals(taskModel)) {
                requestBody.put("maskUrl", taskData.get("FacePath").toString());
            }
        }

        System.out.println("向 FastAPI 发送请求: " + fastApiUrl);
        System.out.println("向 FastAPi 发送请求体的内容：" + requestBody);

        ResponseEntity<String> fastApiResponse = sendRequestToFastAPI(fastApiUrl, requestBody);

        if (fastApiResponse != null && fastApiResponse.getStatusCode().is2xxSuccessful()) {
            if (taskType.equals("SuperResolution")){
                handleSuperResponse(taskId, fastApiResponse.getBody(), task, photo);
            }
            else {
                completedTasks.incrementAndGet();
                handleFastAPIResponse(taskId, fastApiResponse.getBody(), task, photo);

                if (completedTasks.get() == totalTasks) {
                    sendFinalComposeRequest(photo, taskId); // 任务全部完成后发送最终合成请求
                }
            }
        } else {
            System.out.println("FastAPI 请求失败，任务 ID: " + taskId);
            markTaskAsFailed(task, photo);

            // **新逻辑：失败任务重新入队，防止死循环**
            redisService.pushTaskToQueue(taskId);
        }
    }


    private void sendFinalComposeRequest(Photos photo,int taskId) {
        String composeUrl = baseUrl + "/merge-images";
        ResponseEntity<String> response = sendRequestToFastAPI(composeUrl, Map.of("photoUrl", photo.getPhotoPath(),"photoId",photo.getPhotoId()));
        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            try {
                System.out.println("收到的合成结果：" + response.getBody());

                // 解析 JSON 响应
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                String compositedUrl = rootNode.path("mergedImagePath").asText().trim(); // 修正字段名

                System.out.println("compositedUrl: " + compositedUrl);

                if (!compositedUrl.isEmpty()) { // 防止空字符串
                    System.out.println("开始进行最后的插入数据");
                    final_photos = compositedUrl; // 修正变量拼写
                    String photoUrl = photo.getPhotoPath();
                    int userId = photo.getUserId();

                    // 更新 Redis
                    redisService.addCompositeToPhoto(compositedUrl, photoUrl, userId);

                    // 获取合成照片 ID 并更新数据库
                    int compositePhotoId = Integer.parseInt(redisService.getTask(taskId).get("CompositePhotoId").toString());
                    compositePhotosService.update(compositePhotoId, compositedUrl);

                    // 更新照片状态
                    photo.setStatus(Photos.Status.Completed);
                    photosService.update(photo);
                    redisService.savePhoto(photo);

                    System.out.println("最终合成任务成功！");
                    message = "success";
                } else {
                    // 如果合成 URL 为空，则标记照片仍在处理中
                    photo.setStatus(Photos.Status.Processing);
                    photosService.update(photo);
                    redisService.savePhoto(photo);

                    System.out.println("合成图丢失");
                    message = "no found";
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 解析失败：" + e.getMessage(), e);
            }
        } else {
            System.out.println("最终合成任务失败！");
            message="failed";
        }
        this.shutdown();
    }

    private ResponseEntity<String> sendRequestToFastAPI(String url, Map<String, Object> requestBody) {
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
                return restTemplate.postForEntity(url, requestEntity, String.class);
            } catch (Exception e) {
                retryCount++;
                System.out.println("FastAPI 请求失败 (重试 " + retryCount + "/3): " + e.getMessage());
                try {
                    Thread.sleep(1000); // 失败后等待 1s 再重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null; // 最终失败
    }


    private void handleFastAPIResponse(int taskId, String jsonResponse, Tasks task, Photos photo) {
//        Map<String, Object> responseData = new HashMap<>();
//        responseData.put("taskId", taskId);
        try {
            System.out.println("taskId"+taskId+"收到的comfyUI的结果数据："+jsonResponse);
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            String generatedUrl = rootNode.path("processedImagePath").asText();
//          String compositedUrl = rootNode.path("composited_url").asText();
            if (generatedUrl != null ) {
                task.setEndTime(Timestamp.from(Instant.now()));
                task.setStatus(Tasks.Status.Success);
                tasksService.update(task);
                redisService.updateStatus(task.getTaskId(), Tasks.Status.Success);

//                photo.setStatus(Photos.Status.Completed);
//                photosService.update(photo);
//                redisService.savePhoto(photo);

                updateGenerated(taskId, generatedUrl);
//                String photoUrl=photo.getPhotoPath();
//                int userId=photo.getUserId();
//                redisService.addCompositeToPhoto( photoUrl,userId);
                System.out.println("任务 " + taskId + " 处理成功");
//                responseData.put("message","任务处理成功");
//                new ResponseEntity<>(responseData, HttpStatus.OK);
            } else {
                markTaskAsFailed(task,photo);
                System.out.println("FastAPI 返回值不完整，任务 ID: " + taskId);
//                responseData.put("message","FastAPI 返回值不完整");
//                new ResponseEntity<>(responseData, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("解析 FastAPI 响应失败: " + e.getMessage());
//            responseData.put("message","解析 FastAPI 响应失败: "+e.getMessage());
//            new ResponseEntity<>(responseData, HttpStatus.FORBIDDEN);
        }
    }

    private void handleSuperResponse(int taskId, String jsonResponse, Tasks task, Photos photo) {
//        Map<String, Object> responseData = new HashMap<>();
//        responseData.put("taskId", taskId);
        try {
            System.out.println("taskId"+taskId+"收到的comfyUI的结果数据："+jsonResponse);
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            String generatedUrl = rootNode.path("processedImagePath").asText();
            if (generatedUrl != null ) {
                task.setEndTime(Timestamp.from(Instant.now()));
                task.setStatus(Tasks.Status.Success);
                tasksService.update(task);
                redisService.updateStatus(task.getTaskId(), Tasks.Status.Success);
                updateSuperGenerated(taskId, generatedUrl);
                System.out.println("任务 " + taskId + " 处理成功");
                String photoUrl = photo.getPhotoPath();
                int userId = photo.getUserId();
                // 更新 Redis
                redisService.addCompositeToPhoto(generatedUrl, photoUrl, userId);

                // 获取合成照片 ID 并更新数据库
                int compositePhotoId = Integer.parseInt(redisService.getTask(taskId).get("CompositePhotoId").toString());
                compositePhotosService.update(compositePhotoId, generatedUrl);

                // 更新照片状态
                photo.setStatus(Photos.Status.Completed);
                photosService.update(photo);
                redisService.savePhoto(photo);

                System.out.println("最终合成任务成功！");
                message = "success";
                final_photos = generatedUrl;
//                responseData.put("message","任务处理成功");

            }
            else {
                markTaskAsFailed(task,photo);
                photo.setStatus(Photos.Status.Processing);
                photosService.update(photo);
                redisService.savePhoto(photo);
                message = "no found";
            }
        } catch (Exception e) {
            System.out.println("解析 FastAPI 响应失败: " + e.getMessage());
            message = "failed";
//            responseData.put("message","解析 FastAPI 响应失败: "+e.getMessage());
        }
        this.shutdown();
    }


    private void updateGenerated(int taskId, String generatedUrl) {
        Map<Object, Object> task = readRedis(taskId);
        Integer generatedPhotoId = (Integer) task.get("GeneratedPhotoId");
        Integer compositePhotoId = (Integer) task.get("CompositePhotoId");
        if (generatedPhotoId != null && compositePhotoId != null) {
            generatedPhotosService.update(generatedPhotoId, generatedUrl);
//            compositePhotosService.update(compositePhotoId, compositedUrl);
        } else {
            System.out.println("任务存储 ID 丢失，TaskId: " + taskId);
        }
    }

    private void updateSuperGenerated(int taskId, String generatedUrl) {
        Map<Object, Object> task = readRedis(taskId);
        Integer generatedPhotoId = (Integer) task.get("GeneratedPhotoId");
        Integer compositePhotoId = (Integer) task.get("CompositePhotoId");
        if (generatedPhotoId != null && compositePhotoId != null) {
            generatedPhotosService.update(generatedPhotoId, generatedUrl);
            compositePhotosService.update(compositePhotoId, generatedUrl);
        } else {
            System.out.println("任务存储 ID 丢失，TaskId: " + taskId);
        }
    }

    private void markTaskAsFailed(Tasks task,Photos photo) {
        task.setStatus(Tasks.Status.Failed);
        tasksService.update(task);
        redisService.updateStatus(task.getTaskId(),task.getStatus()); //更换一个函数实现部分内容更新

        photo.setStatus(Photos.Status.Processing);
        photosService.update(photo);
        redisService.savePhoto(photo);
    }

   private Map<Object,Object> readRedis(int taskId) {
       System.out.println("开始读取redis"+taskId);
       Map<Object, Object> taskData = redisService.getTask(taskId);
       System.out.println("taskData的只是"+taskData);
       if (taskData == null || taskData.isEmpty()) {
           System.out.println("Redis 中找不到 TaskId: " + taskId);
           tasksService.restoreTask(taskId);
           taskData = redisService.getTask(taskId);
       }
       return taskData;
   }

    // 添加优雅关闭方法
    public void shutdown() {
        System.out.println("开始关闭了");
        isRunning = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

}
