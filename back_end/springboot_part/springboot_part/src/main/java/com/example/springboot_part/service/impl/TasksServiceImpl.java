package com.example.springboot_part.service.impl;

import com.example.springboot_part.mapper.*;
import com.example.springboot_part.pojo.CompositePhotos;
import com.example.springboot_part.pojo.CroppedFaces;
import com.example.springboot_part.pojo.Tasks;
import com.example.springboot_part.pojo.generated_photos;
import com.example.springboot_part.service.CompositePhotosService;
import com.example.springboot_part.service.GeneratedPhotosService;
import com.example.springboot_part.service.RedisService;
import com.example.springboot_part.service.TasksService;
import com.example.springboot_part.utils.ObsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class TasksServiceImpl implements TasksService {

    @Autowired
    private TasksMapper tasksMapper;
    @Autowired
    private GeneratedPhotosService generatedPhotosService;
    @Autowired
    private CompositePhotosService compositePhotosService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private CroppedFacesMapper croppedFacesMapper;
    @Autowired
    private PhotosMapper photosMapper;
    @Autowired
    private CompositePhotosMapper compositePhotosMapper;
    @Autowired
    private GeneratedPhotosMapper generatedPhotosMapper;
    @Autowired
    private ObsUtils obsUtils;

    @Override
    public Tasks findById(int TaskId) {
        return tasksMapper.findById(TaskId);
    }

    @Override
    public List<Tasks> findAll() {
        return tasksMapper.findAll();
    }

    @Override
    public int insert(Tasks task) {
        return tasksMapper.insert(task);
    }

    @Override
    public void update(Tasks task) {
        tasksMapper.update(task);
    }

    @Override
    public void delete(int TaskId) {
        tasksMapper.delete(TaskId);
    }

    @Override
    public void uploadTask(Tasks task, int photo_id) {
        this.insert(task);
        int task_id= task.getTaskId();
        System.out.println("task的信息：\n"+task);
        generated_photos generatedPhotos=new generated_photos();
        generatedPhotos.setTaskId(task_id);
        generatedPhotosService.insert(generatedPhotos);
        Integer compositephoto_id=compositePhotosMapper.findCompositedPhotoId(photo_id);
        if (compositephoto_id ==null){
            CompositePhotos compositePhotos=new CompositePhotos();
            compositePhotos.setPhotoId(photo_id);
            compositePhotosService.insert(compositePhotos);
        }
        int face_id=task.getFaceId();
        CroppedFaces croppedFaces = croppedFacesMapper.findById(face_id);
        int user_id =  photosMapper.findUserId(photo_id);
        int generatedphoto_id=generatedPhotosMapper.findGeneratedPhotoId(task_id);
        int final_photo_id=compositePhotosMapper.findCompositedPhotoId(photo_id);
        redisService.saveTask(task,croppedFaces,face_id,user_id,final_photo_id,generatedphoto_id,photo_id);
        redisService.pushTaskToQueue(task_id);
    }

    /**
     * @param task
     * @param photo_id
     * @param mask
     */
    @Override
    public void uploadTaskMask(Tasks task, int photo_id, String mask) throws IOException {
        this.insert(task);
        int task_id= task.getTaskId();
        System.out.println("task的信息：\n"+task);
        generated_photos generatedPhotos=new generated_photos();
        generatedPhotos.setTaskId(task_id);
        generatedPhotosService.insert(generatedPhotos);
        Integer compositephoto_id=compositePhotosMapper.findCompositedPhotoId(photo_id);
        if (compositephoto_id ==null){
            CompositePhotos compositePhotos=new CompositePhotos();
            compositePhotos.setPhotoId(photo_id);
            compositePhotosService.insert(compositePhotos);
        }
        int face_id=task.getFaceId();
        mask=obsUtils.uploadMaskbase64(mask,task_id);
        croppedFacesMapper.update(face_id,mask);
        CroppedFaces croppedFaces = croppedFacesMapper.findById(face_id);
        int user_id =  photosMapper.findUserId(photo_id);
        int generatedphoto_id=generatedPhotosMapper.findGeneratedPhotoId(task_id);
        int final_photo_id=compositePhotosMapper.findCompositedPhotoId(photo_id);
        redisService.saveTask(task,croppedFaces,face_id,user_id,final_photo_id,generatedphoto_id,photo_id);
        redisService.pushTaskToQueue(task_id);
    }

    /**
     * 实现redis过期恢复
     */
    @Override
    public void restoreTask(int taskId) {
        Tasks tasks = tasksMapper.findById(taskId);
        int face_id=tasks.getFaceId();
        CroppedFaces croppedFaces = croppedFacesMapper.findById(face_id);
        int photo_id= croppedFaces.getPhotoId();
        int user_id =  photosMapper.findUserId(photo_id);
        int generatedphoto_id=generatedPhotosMapper.findGeneratedPhotoId(tasks.getTaskId());
        int compositephoto_id=compositePhotosMapper.findCompositedPhotoId(photo_id);
        redisService.saveTask(tasks,croppedFaces,face_id,user_id,compositephoto_id,generatedphoto_id,photo_id);
    }
}
