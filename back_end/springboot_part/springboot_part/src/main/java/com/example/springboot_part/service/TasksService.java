package com.example.springboot_part.service;

import com.example.springboot_part.pojo.Tasks;

import java.io.IOException;
import java.util.List;

public interface TasksService {
    Tasks findById(int TaskId);
    List<Tasks> findAll();
    int insert(Tasks task);
    void update(Tasks task);
    void delete(int TaskId);
    void uploadTask(Tasks task, int photoId);
    void uploadTaskMask(Tasks task, int photoId, String mask) throws IOException;
    void restoreTask(int taskId);
}
