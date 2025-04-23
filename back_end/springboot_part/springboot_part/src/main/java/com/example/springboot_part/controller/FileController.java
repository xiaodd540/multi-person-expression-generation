package com.example.springboot_part.controller;

import com.example.springboot_part.pojo.CroppedFaces;
import com.example.springboot_part.pojo.Photos;
import com.example.springboot_part.pojo.Tasks;
import com.example.springboot_part.service.*;
import com.example.springboot_part.utils.ObsUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file") //RequestMapping PostMapping 区别 以及里面相关的注解参数!!!!!!
public class FileController {
    
    boolean isOk = false;

    boolean isLast=false;

    String message = "";

    int status = 0;
    
    @Value("${app.BaseURL}")
    private String baseUrl;
    @Autowired
    private ObsUtils obsUtils;
    @Autowired
    private PhotosService photosService;
    @Autowired
    private CroppedFacesService croppedFacesService;
    @Autowired
    private TasksService tasksService;
    @Autowired
    private TaskProcessingService taskProcessingService;
    @Autowired
    private RedisService redisService;

    @PostMapping("/upload/{user_id}")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file, @PathVariable int user_id) {
        System.out.println("相对完好的");
        try {
            String sha256Hash=FileHashUtil.calculateSHA256(file);
            String obs_url = photosService.IsExistPhoto(sha256Hash)?photosService.obsUploadPhoto(sha256Hash):obsUtils.uploadFile(file);
            int photo_id=photosService.uploadPhoto(obs_url,user_id,sha256Hash);
            Map<String, Object> response = new HashMap<>();
            response.put("photo_id", photo_id);
            response.put("obs_url", obs_url);
            response.put("message", "Upload successful");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.out.println("出现的问题："+e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Upload failed", "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Unexpected error"));
        }
    }
    /*已经测试成功*/

    @PostMapping("/auto/{photo_id}")
    public ResponseEntity<Map<String, Object>> uploadAutoPhoto(
            @RequestBody List<Map<String,Object>> cropRequest,
//            @RequestParam("x") int x,
//            @RequestParam("y") int y,
//            @RequestParam("width") int width,
//            @RequestParam("height") int height,
//            @RequestParam("taskType") Tasks.TaskType taskType,
//            @RequestParam("taskModel") Tasks.TaskModel taskModel,
            @PathVariable int photo_id) {

        for (Map<String, Object> data : cropRequest) {
            double x = ((Number)data.get("x")).doubleValue();
            double y = ((Number)data.get("y")).doubleValue();
            double width = ((Number)data.get("width")).doubleValue();
            double height = ((Number)data.get("height")).doubleValue();
            Tasks.TaskType taskType = Tasks.TaskType.valueOf(data.get("taskType").toString());
            Tasks.TaskModel taskModel = Tasks.TaskModel.valueOf(data.get("taskModel").toString());

            // 1. 将数据存入 MySQL
            CroppedFaces faces = new CroppedFaces();
            faces.setPhotoId(photo_id);
            faces.setX(x);
            faces.setY(y);
            faces.setWidth(width);
            faces.setHeight(height);
            faces.setFacePath("auto");

            croppedFacesService.insert(faces);
            int face_id = faces.getFaceId();
            Tasks tasks = new Tasks();
            tasks.setFaceId(face_id);
            tasks.setTaskModel(taskModel);
            tasks.setTaskType(taskType);
            tasksService.uploadTask(tasks, photo_id); //redis已出现task信息，task队列

        }
        Map<String,Object> response =taskProcessingService.startTaskProcessing();
        return getMapResponseEntity(response);
    }


    @PostMapping("/manual/{photo_id}")
    public ResponseEntity<Map<String, Object>> uploadManualPhoto(
            @RequestBody List<Map<String, Object>> cropRequest,
            @PathVariable int photo_id) throws IOException {

        for (Map<String, Object> data : cropRequest) {
//            double x = (Double)data.get("x");
//            double y = (Double)data.get("y");
//            double width = (Double)data.get("width");
//            double height = (Double)data.get("height"); 避免出现Integer类型传化成Double的类型错误
            double x = ((Number)data.get("x")).doubleValue();
            double y = ((Number)data.get("y")).doubleValue();
            double width = ((Number)data.get("width")).doubleValue();
            double height = ((Number)data.get("height")).doubleValue();
            Tasks.TaskType taskType = Tasks.TaskType.valueOf(data.get("taskType").toString());
            Tasks.TaskModel taskModel = Tasks.TaskModel.valueOf(data.get("taskModel").toString());
            String file = data.get("mask").toString();
            System.out.println("mask的数据是："+file);

            // 1. 将数据存入 MySQL
            CroppedFaces faces = new CroppedFaces();
            faces.setPhotoId(photo_id);
            faces.setX(x);
            faces.setY(y);
            faces.setWidth(width);
            faces.setHeight(height);
            croppedFacesService.insert(faces);
            int face_id = faces.getFaceId();

            Tasks tasks = new Tasks();
            tasks.setFaceId(face_id);
            tasks.setTaskModel(taskModel);
            tasks.setTaskType(taskType);
            tasksService.uploadTaskMask(tasks, photo_id,file); //redis已出现task信息，task队列
        }
        Map<String,Object> response =taskProcessingService.startTaskProcessing();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{photo_id}")
    public ResponseEntity<?> getStatus(@PathVariable int photo_id) {
        Photos.Status status=redisService.getPhoto(photo_id).getStatus();
        if (status==Photos.Status.Completed){
            return new ResponseEntity<>(HttpStatus.OK);
        }else if (status == Photos.Status.Processing){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else {
            return new ResponseEntity<>(HttpStatus.CONTINUE);
        }
    }

    @PostMapping("/SR/{photo_id}")
    public ResponseEntity<Map<String, Object>> uploadSRPhoto(@PathVariable int photo_id){
        CroppedFaces faces = new CroppedFaces();
        faces.setPhotoId(photo_id);
        faces.setX(0);
        faces.setY(0);
        faces.setWidth(0);
        faces.setHeight(0);
        faces.setFacePath("SR");
        croppedFacesService.insert(faces);
        int face_id = faces.getFaceId();

        Tasks tasks = new Tasks();
        tasks.setFaceId(face_id);
        tasks.setTaskModel(Tasks.TaskModel.Automatic);
        tasks.setTaskType(Tasks.TaskType.SuperResolution);
        tasksService.uploadTask(tasks, photo_id);

        Map<String,Object> response =taskProcessingService.startTaskProcessing();//修改一下啊
        return ResponseEntity.ok(response);
    }

    @NotNull
    private ResponseEntity<Map<String, Object>> getMapResponseEntity(Map<String,Object> response) {
        System.out.println("回复的响应体的内容：\n"+response);
        if (response.get("message").equals("success")) {
            return ResponseEntity.ok(response);
        } else if (response.get("message").equals("no found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    public static class FileHashUtil {
        public static String calculateSHA256(MultipartFile file) throws Exception {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }



//    // 轮询函数，每10秒执行一次，可以根据需求调整时间间隔
//    @Scheduled(fixedRate = 10000) // 每10秒执行一次
//    public void pollTaskQueue(int taskId,boolean isLast) {
//        if (isOk) {
//            return;
//        }
//        taskProcessingService.processTaskQueue(isLast).thenAccept(response -> {
//            Integer task_id = (Integer) response.getBody().get("taskId");
//            if (task_id.equals(taskId)) {
//                isOk = true;
//            }
//            System.out.println("当前的isOK:" + isOk);
//            message = (String) response.getBody().get("message");
//            status=response.getStatusCode().value();
//        });
//    }


}
