package com.example.springboot_part.service;

import com.example.springboot_part.mapper.*;
import com.example.springboot_part.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PhotosMapper photosMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private CompositePhotosMapper compositePhotosMapper;

    @Autowired
    private GeneratedPhotosMapper generatedPhotosMapper;

    @Autowired
    private CroppedFacesMapper croppedFacesMapper;

    private static final String Register_User = "register_user";
    private static final String USER_KEY = "user:";
    private static final String PHOTO_KEY = "photo:";
    private static final String TASK_KEY = "task:";
    private static final String COMPOSITE_KEY = "compositePhoto:";
    private static final String TASK_QUEUE = "task:queue";
    private static final String PHOTO_QUEUE = "photo:upload:queue";

    /**
     * 🔹 存储注册所有用户信息
     */
    public void saveALL(int user_id){
        redisTemplate.opsForZSet().add(Register_User,user_id,Instant.now().getEpochSecond());
    }

    /**
     * 🔹 查看注册所有用户信息
     */
    public List<Integer> findAllUserId(){
        Set<Object> userIds = redisTemplate.opsForZSet().range(Register_User, 0, -1);
        return userIds != null ? userIds.stream()
                .map(id -> (Integer) id)  // 显式转换 Object -> Integer
                .toList() : List.of();
    }

    /**
     * 🔹 存储用户信息
     */
    public void saveUser(Users user) {
        String key = USER_KEY + user.getUserId();
        redisTemplate.opsForHash().put(key, "UserId", user.getUserId());
        redisTemplate.opsForHash().put(key, "Username", user.getUserName());
        redisTemplate.opsForHash().put(key, "Password", user.getPassword());
        redisTemplate.opsForHash().put(key,"CreatedAt", user.getCreatedAt());
        redisTemplate.opsForHash().put(key, "Avatar", user.getAvatar());
    }

    /**
     * 🔹 获取用户信息
     */
    public Map<Object, Object> getUser(int userId) {
        String key = USER_KEY + userId;
        return redisTemplate.opsForHash().entries(key);
    }


    /**
     * 🔹 存储图片信息
     */
    public void savePhoto(Photos photo) {
        String key = PHOTO_KEY + photo.getPhotoId();
        redisTemplate.opsForHash().put(key, "PhotoId", photo.getPhotoId());
        redisTemplate.opsForHash().put(key, "UserId", photo.getUserId());
        redisTemplate.opsForHash().put(key, "PhotoPath", photo.getPhotoPath());
        redisTemplate.opsForHash().put(key, "UploadTime", photo.getUploadTime());
        redisTemplate.opsForHash().put(key, "Status", photo.getStatus());
        redisTemplate.opsForHash().put(key, "PhotosHash", photo.getPhotosHash());
    }

    /**
     * 🔹 查询图片信息
     */
    public Photos getPhoto(int photoId) {
        Map<Object, Object> photoMap = redisTemplate.opsForHash().entries(PHOTO_KEY + photoId);
        return mapToPhotos(photoMap);
    }

    private Photos mapToPhotos(Map<Object, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Photos photo = new Photos();
        photo.setPhotoId((Integer) map.get("PhotoId"));
        photo.setUserId((Integer) map.get("UserId"));
        photo.setPhotoPath((String) map.get("PhotoPath"));
        photo.setUploadTime((Timestamp) map.get("UploadTime"));
        photo.setStatus((Photos.Status)map.get("Status"));
        photo.setPhotosHash((String) map.get("PhotosHash"));
        return photo;
    }

    /**
     * 🔹 存储任务信息
     */
    public void saveTask(Tasks task,CroppedFaces croppedFaces,int face_id,int user_id,int compositephoto_id,int generatedphoto_id,int photo_id) {
        String key = TASK_KEY + task.getTaskId();
        redisTemplate.opsForHash().put(key, "TaskId", task.getTaskId()); //原有的
        redisTemplate.opsForHash().put(key, "FaceId", face_id); //原有的
        redisTemplate.opsForHash().put(key, "UserId", user_id);  //Users
        redisTemplate.opsForHash().put(key, "PhotoId", photo_id); //Photos
        redisTemplate.opsForHash().put(key, "GeneratedPhotoId", generatedphoto_id); //Gen
        redisTemplate.opsForHash().put(key, "CompositePhotoId", compositephoto_id);  //Com
        redisTemplate.opsForHash().put(key, "Status", task.getStatus()); //原有的
        redisTemplate.opsForHash().put(key, "TaskType", task.getTaskType());//原有的
        redisTemplate.opsForHash().put(key, "TaskModel", task.getTaskModel());//原有的
        redisTemplate.opsForHash().put(key,"FacePath",croppedFaces.getFacePath()); //Crop(Mask图片)
        redisTemplate.opsForHash().put(key,"x",croppedFaces.getX()); //Crop
        redisTemplate.opsForHash().put(key,"y",croppedFaces.getY());//Crop
        redisTemplate.opsForHash().put(key,"width",croppedFaces.getWidth());//Crop
        redisTemplate.opsForHash().put(key,"height",croppedFaces.getHeight());//Crop
        System.out.println("redisTasks已经存储成功");
    }

    /**
     * 🔹 查询任务信息
     */
    public Map<Object, Object> getTask(int taskId) {
        return redisTemplate.opsForHash().entries(TASK_KEY + taskId);
    }

    /**
     * 🔹 更新任务状态
     */
    public void updateStatus(int taskId, Tasks.Status newStatus) {
        String key = TASK_KEY + taskId;

        // 检查任务是否存在
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.FALSE.equals(exists)) {
            System.out.println("任务不存在，无法更新状态");
            return;
        }

        // 更新状态
        redisTemplate.opsForHash().put(key, "Status", newStatus.toString());
        System.out.println("任务 " + taskId + " 状态更新为: " + newStatus);
    }

    /**
     * 🔹 推送任务到队列（FIFO）
     */
    public void pushTaskToQueue(int taskId) {
        redisTemplate.opsForList().rightPush(TASK_QUEUE, taskId);
    }

    /**
     * 🔹 获取当前任务队列长度（线程安全）
     * @return 队列元素数量（键不存在时返回0）
     */
    public int getTaskQueueSize() {
        try {
            Long size = redisTemplate.opsForList().size(TASK_QUEUE);
            return size != null ? size.intValue() : 0;
        } catch (Exception e) {
            System.out.println("Redis连接异常: " + e.getMessage());
            return 0; // 连接异常时返回安全值
        }
    }


    /**
     * 🔹 取出任务（FIFO）
     */
    public int popTaskFromQueue() {
        Integer taskId = (Integer) redisTemplate.opsForList().leftPop(TASK_QUEUE);
        return (taskId != null) ? taskId : -1;
    }

    /**
     * 🔹 存储合成照片信息
     */
    public void saveCompositePhoto(CompositePhotos compositePhoto) {
        String key = COMPOSITE_KEY + compositePhoto.getCompositePhotoId();
        redisTemplate.opsForHash().put(key, "CompositedPhotoId", compositePhoto.getCompositePhotoId());
        redisTemplate.opsForHash().put(key, "PhotoId", compositePhoto.getPhotoId());
        redisTemplate.opsForHash().put(key, "CompositedPhotoPath", compositePhoto.getCompositePhotoPath());
    }

    /**
     * 🔹 查询合成照片信息
     */
    public Map<Object, Object> getCompositePhoto(Long compositedPhotoId) {
        return redisTemplate.opsForHash().entries(COMPOSITE_KEY + compositedPhotoId);
    }

    /**
     * 🔹 记录用户所有上传的照片（ZSet，按时间排序）
     */
    public void addUserPhoto(int userId,int photoId,Timestamp  timestamp) {
        long time = timestamp.getTime()/1000;
        redisTemplate.opsForZSet().add(PHOTO_QUEUE + userId,photoId,time);
        redisTemplate.opsForZSet().add("temp_list" + userId,photoId,time);
    }

    /**
     * 🔹 查询用户照片列表（分页）
     */
    public List<Map<String, Object>> getUserPhotos(int userId, int start, int end) {
        Set<Object> photoIds = redisTemplate.opsForZSet().reverseRange(PHOTO_QUEUE + userId, start, end);
        List<Map<String, Object>> photoDetails = new ArrayList<>();
        if (photoIds != null) {
            for (Object photoId : photoIds) {
                String key = PHOTO_KEY + photoId;
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

                Map<String, Object> photoData = new HashMap<>();
                if (entries.containsKey("PhotoPath")){
                    photoData.put("PhotoPath",entries.get("PhotoPath"));
                }
                if (entries.containsKey("Status")){
                    photoData.put("Status",entries.get("Status"));
                }
                if (entries.containsKey("UploadTime")){
                    photoData.put("UploadTime",entries.get("UploadTime"));
                }
                photoDetails.add(photoData);
            }
        }
        return photoDetails;
    }

    /**
     * 🔹 获取用户上传的照片总数
     */
    public long countUserPhotos(int userId) {
        String key = PHOTO_QUEUE + userId;
        Long count = redisTemplate.opsForZSet().size(key);
        return count != null ? count : 0;
    }

    /**
     * 🔹 记录原图生成的所有合成照片（Set）
     */
    public void addCompositeToPhoto(String compositedPhotoPath,String PhotoPath,int userId) {
        String key = String.format("User:%d:source:%s", userId, PhotoPath);
        redisTemplate.opsForSet().add(key, compositedPhotoPath);
    }

    /**
     * 🔹 查询某张原图的所有合成照片
     */
    public Set<Object> getCompositePhotosBySource(int userId,String PhotoPath) {
        String key = String.format("User:%d:source:%s", userId, PhotoPath);
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 🔹 查询用户上传所有原图对应所有合成照片
     */
    public Map<String, List<String>> getUserPhotosBySource(int userId) {
        Set<Object> photoIds = redisTemplate.opsForZSet().reverseRange(PHOTO_QUEUE + userId, 0, -1);
        return getResultsMaps(userId, photoIds);
    }

    /**
     * 🔹 查询最近7天上传原图对应所有合成照片
     */
    public Map<String, List<String>> getUserTempBySource(int userId) {
        long expireTime = Instant.now().getEpochSecond() - 7 * 86400;
        Set<Object> photoIds = redisTemplate.opsForZSet().reverseRangeByScore("temp_list" + userId, expireTime,Double.MAX_VALUE);
        System.out.println("在redis上传7天历史的信息："+photoIds);
        return getResultsMaps(userId, photoIds);
    }

    private Map<String, List<String>> getResultsMaps(int userId, Set<Object> photoIds) {
        // 使用 Map<String, Set<String>> 避免 photoPath 重复，并去重 compositedPhotoPath
        Map<String, Set<String>> photoMap = new HashMap<>();

        if (photoIds != null) {
            for (Object photoId : photoIds) {
                int id = Integer.parseInt(photoId.toString());
                String photoPath = this.getPhoto(id).getPhotoPath();
                Set<Object> compositePhotos = this.getCompositePhotosBySource(userId, photoPath);

                if (compositePhotos != null && !compositePhotos.isEmpty()) {
                    // 确保 photoPath 只存储一次，并去重 compositedPhotoPath
                    photoMap.computeIfAbsent(photoPath, k -> new LinkedHashSet<>());

                    // 添加 compositedPhotoPath
                    for (Object compositePhoto : compositePhotos) {
                        photoMap.get(photoPath).add(compositePhoto.toString());
                    }
                }
            }
        }

        // 最终转换为 Map<String, List<String>>，确保 JSON 格式符合要求
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : photoMap.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return result;
    }

    /**
     * 🔹 清理 7天前的过期数据
     */
    public void cleanOldRecords(int userId) {
        long expireTime = Instant.now().getEpochSecond() - 7 * 86400;
        redisTemplate.opsForZSet().removeRangeByScore("temp_list" + userId, 0, expireTime);
    }

    public void deletePhoto(int photoId) {

        Map<Object, Object> photoMap = redisTemplate.opsForHash().entries(PHOTO_KEY + photoId);
        Object userIdObj = photoMap.get("UserId");
        int userId = userIdObj != null ? Integer.parseInt(userIdObj.toString()) : -1; // 默认值 -1 代表获取失败

        // 删除单个照片信息
        redisTemplate.delete(PHOTO_KEY + photoId);

        // 从用户的照片列表中移除
        redisTemplate.opsForZSet().remove(PHOTO_QUEUE + userId, photoId);

        // 可选：减少上传计数
        redisTemplate.opsForValue().decrement("photo:upload:counter:" + userId);
    }
}
