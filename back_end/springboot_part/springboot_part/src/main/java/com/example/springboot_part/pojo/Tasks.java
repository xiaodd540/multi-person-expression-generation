package com.example.springboot_part.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Setter
@Getter  // 为所有字段自动生成 getter 方法
@EqualsAndHashCode(callSuper = false)
@ToString()
public class Tasks {
    private Integer TaskId;
    private int FaceId;
    private Timestamp StartTime;
    private Timestamp EndTime;
    private Status Status;
    private TaskType TaskType;  // Lombok 会自动为这个字段生成 getter
    private TaskModel TaskModel;  // Lombok 会自动为这个字段生成 getter

    public enum Status {
        InProgress, Success, Failed
    }
    public enum TaskType{
        SmileAdjustment, EyeOpening, SuperResolution
    }
    public enum TaskModel{
        Automatic, Manual
    }

    public Tasks() {}

    public Tasks(int taskId, int faceId, Timestamp startTime, Timestamp endTime, Status status, TaskType taskType, TaskModel taskModel) {
        this.TaskId = taskId;
        this.FaceId = faceId;
        this.StartTime = startTime;
        this.EndTime = endTime;
        this.Status = status;
        this.TaskType = taskType;
        this.TaskModel = taskModel;
    }
}

