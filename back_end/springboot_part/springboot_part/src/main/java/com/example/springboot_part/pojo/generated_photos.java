package com.example.springboot_part.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString()
public class generated_photos {
    private Integer GeneratedPhotoId;
    private int TaskId;
    private String GeneratedPhotoPath;
    private Timestamp CreatedTime;

    public generated_photos(){}

    public generated_photos(int generatedPhotoId, int taskId, String generatedPhotoPath, Timestamp createdTime) {
        this.GeneratedPhotoId = generatedPhotoId;
        this.TaskId = taskId;
        this.GeneratedPhotoPath = generatedPhotoPath;
        this.CreatedTime = createdTime;
    }
}
